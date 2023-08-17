package org.onecx.document.management.rs.v1.controllers;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.PROCEED;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.FileTime;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.onecx.document.management.domain.criteria.DocumentSearchCriteria;
import org.onecx.document.management.domain.daos.AttachmentDAO;
import org.onecx.document.management.domain.daos.ChannelDAO;
import org.onecx.document.management.domain.daos.DocumentDAO;
import org.onecx.document.management.domain.daos.MinioAuditLogDAO;
import org.onecx.document.management.domain.daos.StorageUploadAuditDAO;
import org.onecx.document.management.domain.models.entities.Attachment;
import org.onecx.document.management.domain.models.entities.Channel;
import org.onecx.document.management.domain.models.entities.Document;
import org.onecx.document.management.domain.models.entities.MinioAuditLog;
import org.onecx.document.management.domain.models.entities.StorageUploadAudit;
import org.onecx.document.management.rs.v1.mappers.DocumentMapper;
import org.onecx.document.management.rs.v1.models.AttachmentDTO;
import org.onecx.document.management.rs.v1.models.ChannelDTO;
import org.onecx.document.management.rs.v1.models.DocumentCreateUpdateDTO;
import org.onecx.document.management.rs.v1.models.DocumentDetailDTO;
import org.onecx.document.management.rs.v1.models.DocumentResponseDTO;
import org.onecx.document.management.rs.v1.models.DocumentSearchCriteriaDTO;
import org.onecx.document.management.rs.v1.models.RFCProblemDTO;
import org.onecx.document.management.rs.v1.services.DocumentService;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.exceptions.RestException;
import org.tkit.quarkus.rs.models.PageResultDTO;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;

@Path("/v1/document")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "DocumentControllerV1")
@ApplicationScoped
public class DocumentController {

    @Inject
    DocumentDAO documentDAO;

    @Inject
    ChannelDAO channelDAO;

    @Inject
    AttachmentDAO attachmentDAO;

    @Inject
    StorageUploadAuditDAO storageUploadAuditDAO;

    @Inject
    MinioAuditLogDAO minioAuditLogDAO;

    @Inject
    DocumentMapper documentMapper;

    @Inject
    DocumentService documentService;

    public static final DateTimeFormatter CUSTOM_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // The response from the download attachment zip API will have this as the
    // Content-Disposition header value.
    public static final String ATTACHMENT_ZIP_CONTENT_DISPOSITION_HEADER = "attachment; filename=\"attachments.zip\"";

    private static final String CLASS_NAME = "DocumentController";

    /**
     * This scheduler gets triggered at every Saturday at 23:00 hours
     * This scheduler deletes all the records from the "dm_attachment" table
     * when the value of "storage_upload_status" column is "false"
     */
    @Transactional
    @Scheduled(cron = "0 0 23 ? * SAT", concurrentExecution = PROCEED)
    public void clearFailedFilesFromDBPeriodically() {
        attachmentDAO.deleteAttachmentsBasedOnFileUploadStatus();
    }

    /**
     * This scheduler gets triggered at every Sunday at 23:00 hours
     * It calls the getAllRecords method of MinioAuditLogDAO class
     * If the returned list is not null then all objects are iterated over a loop
     * and
     * it calls the deleteFileInAttachmentAsync method to delete object from Minio
     * storage
     * it then deletes that specific record from the MinioAuditLog table
     */
    @Transactional
    @Scheduled(cron = "0 0 23 ? * SUN", concurrentExecution = PROCEED)
    public void deleteAllRecordsFromMinioAuditLog() {
        List<MinioAuditLog> minioAuditLogAllRecords = minioAuditLogDAO.getAllRecords();
        if (!Objects.isNull(minioAuditLogAllRecords)) {
            minioAuditLogAllRecords.stream().forEach(auditRecord -> {
                documentService.deleteFileInAttachmentAsync(auditRecord.getAttachmentId());
                minioAuditLogDAO.delete(auditRecord);
            });
        }
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ "document-admin", "document-responsible", "document-user" })
    @Operation(operationId = "getDocumentById", description = "Gets Document by Id")
    @APIResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DocumentDetailDTO.class)))
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    public Response getDocumentById(@PathParam("id") String id) {
        Log.info(CLASS_NAME, "Entered getDocumentById method", null);
        var document = documentDAO.findDocumentById(id);
        if (Objects.isNull(document)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND, getDocumentNotFoundMsg(id));
        }
        Log.info(CLASS_NAME, "Exited getDocumentById method", null);
        return Response.status(Response.Status.OK)
                .entity(documentMapper.mapDetail(document))
                .build();
    }

    @GET
    @Transactional
    @RolesAllowed({ "document-admin", "document-responsible", "document-user" })
    @Operation(operationId = "getDocumentByCriteria", description = "Gets documents by criteria")
    @APIResponse(responseCode = "200", description = "The corresponding documents resource", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PageResultDTO.class)))
    @APIResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    public Response getDocumentByCriteria(@BeanParam DocumentSearchCriteriaDTO criteriaDTO) {
        Log.info(CLASS_NAME, "Entered getDocumentByCriteria method", null);
        DocumentSearchCriteria criteria = documentMapper.map(criteriaDTO);
        if (Objects.nonNull(criteriaDTO.getStartDate()) && !criteriaDTO.getStartDate().isEmpty()) { // added this for
                                                                                                    // date search

            criteria.setStartDate(LocalDateTime.parse(criteriaDTO.getStartDate(), CUSTOM_DATE_TIME_FORMATTER));
        }
        if (Objects.nonNull(criteriaDTO.getEndDate()) && !criteriaDTO.getEndDate().isEmpty()) {

            criteria.setEndDate(LocalDateTime.parse(criteriaDTO.getEndDate(), CUSTOM_DATE_TIME_FORMATTER));
        }
        PageResult<Document> documents = documentDAO.findBySearchCriteria(criteria);
        Log.info(CLASS_NAME, "Exited getDocumentByCriteria method", null);
        return Response.ok(documentMapper.mapToPageResultDTO(documents))
                .build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ "document-admin", "document-responsible" })
    @Transactional
    @Operation(operationId = "deleteDocumentById", description = "Delete Document by Id")
    @APIResponse(responseCode = "204", description = "Deleted Document by id")
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    public Response deleteDocumentById(@PathParam("id") String id) {
        Log.info(CLASS_NAME, "Entered deleteDocumentById method", null);
        List<String> listOfFilesIdToBeDeleted = new ArrayList<>();
        var document = documentDAO.findById(id);
        if (Objects.isNull(document)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND, getDocumentNotFoundMsg(id));
        }
        listOfFilesIdToBeDeleted.addAll(documentService.getFilesIdToBeDeletedInDocument(document));
        documentDAO.delete(document);
        listOfFilesIdToBeDeleted.stream().forEach(eachFileId -> documentService.asyncDeleteForAttachments(eachFileId));
        Log.info(CLASS_NAME, "Exited deleteDocumentById method", null);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @RolesAllowed({ "document-admin", "document-responsible" })
    @Operation(operationId = "createDocument", description = "Create Document")
    @APIResponse(responseCode = "201", description = "Created Document resource", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DocumentDetailDTO.class)), headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(type = SchemaType.STRING), description = "URL of the entity created"))
    @APIResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    public Response createDocument(@Valid DocumentCreateUpdateDTO documentDTO) {
        Log.info(CLASS_NAME, "Entered createDocument method", null);
        var document = documentService.createDocument(documentDTO);
        Log.info(CLASS_NAME, "Exited createDocument method", null);
        return Response.status(Response.Status.CREATED)
                .entity(documentMapper.mapDetail(document))
                .build();
    }

    @POST
    @Path("/files/upload/{documentId}")
    @Transactional
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({ "document-admin", "document-responsible" })
    @Operation(operationId = "uploadAllFiles", description = "uploads all the files")
    @APIResponse(responseCode = "201", description = "Created", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AttachmentDTO.class)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "404", description = "Not found")
    @APIResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestException.class)))
    public Response multipleFileUploads(@PathParam("documentId") String documentId,
            @MultipartForm MultipartFormDataInput input) throws IOException {
        Log.info(CLASS_NAME, "Entered multipleFileUploads method", null);
        Map<String, Integer> map = documentService.uploadAttachment(documentId, input);
        var responseDTO = new DocumentResponseDTO();
        responseDTO.setAttachmentResponse(map);
        Log.info(CLASS_NAME, "Exited multipleFileUploads method", null);
        return Response.status(Response.Status.CREATED)
                .entity(responseDTO)
                .build();
    }

    @GET
    @Path("/files/upload/failed/{id}")
    @RolesAllowed({ "document-admin", "document-responsible", "document-user" })
    @Transactional
    @Operation(operationId = "getFailedAttachmentData", description = "Get data of all the failed attachment based on document ID")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    public Response getFailedAttachmentById(@PathParam("id") String documentId) {
        Log.info(CLASS_NAME, "Entered getFailedAttachmentById method", null);
        List<StorageUploadAudit> failedAttachmentList = storageUploadAuditDAO
                .findFailedAttachmentsByDocumentId(documentId);
        Log.info(CLASS_NAME, "Exited getFailedAttachmentById method", null);
        return Response.status(Response.Status.OK).entity(failedAttachmentList).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ "document-admin", "document-responsible" })
    @Transactional
    @Operation(operationId = "updateDocument", description = "Update an document")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DocumentDetailDTO.class)))
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    public Response updateDocument(@PathParam("id") String id, @Valid DocumentCreateUpdateDTO dto) {
        Log.info(CLASS_NAME, "Entered updateDocument method", null);
        var document = documentDAO.findDocumentById(id);
        if (Objects.isNull(document)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND, getDocumentNotFoundMsg(id));
        }
        document = documentService.updateDocument(document, dto);
        Log.info(CLASS_NAME, "Exited updateDocument method", null);
        return Response.status(Response.Status.CREATED)
                .entity(documentMapper.mapDetail(documentDAO.update(document)))
                .build();
    }

    @GET
    @Path("/channels")
    @Operation(operationId = "getAllChannels", description = "Gets all channels")
    @APIResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ChannelDTO[].class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    public Response getAllChannels() {

        Log.info(CLASS_NAME, "Entered getAllChannels method", null);
        // List of unique alphabetically sorted channel names ignoring cases
        List<Channel> uniqueSortedChannelNames = channelDAO.findAllSortedByNameAsc()
                .filter(distinctByKey(c -> c.getName().toLowerCase(Locale.ROOT)))
                .toList();
        Log.info(CLASS_NAME, "Exited getAllChannels method", null);
        return Response.status(Response.Status.OK)
                .entity(documentMapper.mapChannels(uniqueSortedChannelNames))
                .build();
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Log.info(CLASS_NAME, "Entered distinctByKey method", null);
        Set<Object> seen = Collections.newSetFromMap(new ConcurrentHashMap<>());
        Log.info(CLASS_NAME, "Exited distinctByKey method", null);
        return t -> seen.add(keyExtractor.apply(t));
    }

    @GET
    @Path("/file/{attachmentId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(operationId = "getFile", description = "Get attachment's file")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(implementation = InputStream.class)))
    @APIResponse(responseCode = "404", description = "Not found")
    @APIResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestException.class)))
    public Response getFile(@PathParam("attachmentId") String attachmentId)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        Log.info(CLASS_NAME, "Entered getFile method", null);
        var attachment = attachmentDAO.findById(attachmentId);
        if (Objects.isNull(attachment)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        InputStream object = documentService.getObjectFromObjectStore(attachmentId);
        Log.info(CLASS_NAME, "Exited getFile method", null);
        return Response.ok(object)
                .header("Content-Disposition", String.format("attachment;filename=%s", attachment.getFileName()))
                .build();
    }

    @GET
    @Path("/file/{documentId}/attachments")
    @RolesAllowed({ "document-admin", "document-responsible", "document-user" })
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(operationId = "getAllDocumentAttachmentsAsZip", description = "Get all the attachments of the document packaged in a zip file")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(implementation = InputStream.class)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "204", description = "No content")
    @APIResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestException.class)))
    public Response getAllDocumentAttachmentsAsZip(@PathParam("documentId") String documentId,
            @HeaderParam("client-timezone") String clientTimezone) {
        Log.info(CLASS_NAME, "Entered getAllDocumentAttachmentsAsZip method", null);
        try {
            /* Retrieve the document by its ID */
            var document = documentDAO.findById(documentId);

            /*
             * Return a bad request response if the document is not found because a document
             * should exist for this request to have come in
             */
            if (Objects.isNull(document))
                return Response.status(Response.Status.BAD_REQUEST).build();

            /* Retrieve the attachment details of this document */
            Set<Attachment> documentAttachmentSet = document.getAttachments().stream()
                    .filter(Attachment::getStorageUploadStatus).collect(Collectors.toSet());

            /*
             * If the document has no attachments return a 204 error because there is no
             * content to return.
             */
            if (Objects.isNull(documentAttachmentSet) || documentAttachmentSet.isEmpty())
                return Response.status(Response.Status.NO_CONTENT).build();

            /* Code to create a zip file containing all the attachment files */
            StreamingOutput stream = output -> {

                /*
                 * Use ZipOutputStream to create the zip and compress the its contents. This
                 * reduces the size of the zip file and saves bandwidth and data while
                 * transmitting over the internet. We are using the default compression level
                 * because it is a good balance between file size and compression speed.
                 */
                try (var zip = new ZipOutputStream(output)) {

                    /* Iterate over the set of attachments of the document using Java Streams */
                    documentAttachmentSet.stream()
                            .filter(Objects::nonNull)
                            .forEach(attachment -> {
                                try {

                                    /* Download the attachment file from minio */
                                    InputStream object = documentService
                                            .getObjectFromObjectStore(
                                                    attachment.getId());

                                    /*
                                     * Add the attachment file into the zip with the
                                     * same filename
                                     */
                                    var entry = new ZipEntry(
                                            attachment.getFileName());
                                    entry.setSize(object.available());
                                    ZoneId clientZoneId = (clientTimezone != null && !clientTimezone.isEmpty())
                                            ? ZoneId.of(clientTimezone)
                                            : ZoneId.of("UTC");
                                    LocalDateTime attachmentDateTime = attachment.getCreationDate();
                                    FileTime fileTime;
                                    if (attachmentDateTime != null)
                                        fileTime = FileTime.from(attachmentDateTime.atZone(clientZoneId).toInstant());
                                    else
                                        fileTime = FileTime.from(LocalDateTime.now().atZone(clientZoneId).toInstant());
                                    entry.setCreationTime(fileTime);
                                    entry.setLastModifiedTime(fileTime);
                                    zip.putNextEntry(entry);
                                    IOUtils.copy(object, zip);
                                    zip.closeEntry();
                                } catch (Exception e) {
                                    /*
                                     * If the attachment file could not be retrieved,
                                     * throw an
                                     * interal server error RestException.
                                     */
                                    throw new RestException(
                                            Response.Status.INTERNAL_SERVER_ERROR,
                                            Response.Status.INTERNAL_SERVER_ERROR,
                                            "Failed to download file", e);
                                }
                            });
                    zip.finish();
                }
            };
            Log.info(CLASS_NAME, "Exited getAllDocumentAttachmentsAsZip method", null);
            return Response.ok(stream)
                    .header("Content-Disposition", ATTACHMENT_ZIP_CONTENT_DISPOSITION_HEADER)
                    .type("application/zip")
                    .build();
        } catch (Exception e) {
            /* Return an internal server error to the client if any issue occurs */
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

    }

    @DELETE
    @Path("/file/delete-bulk-attachment")
    @RolesAllowed({ "document-admin", "document-responsible" })
    @Transactional
    @Operation(operationId = "deleteFilesInBulk", description = "Delete attachment's files in bulk")
    @APIResponse(responseCode = "204", description = "No Content")
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "404", description = "Not found")
    @APIResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestException.class)))
    public Response deleteFilesInBulk(List<String> attachmentIds) {
        Log.info(CLASS_NAME, "Entered deleteFilesInBulk method", null);
        documentService.updateAttachmentStatusInBulk(attachmentIds);
        attachmentIds.stream().forEach(attachmentId -> documentService.asyncDeleteForAttachments(attachmentId));
        Log.info(CLASS_NAME, "Exited deleteFilesInBulk method", null);
        return Response.noContent().build();
    }

    @PUT
    @Path("/bulkupdate")
    @RolesAllowed("document-admin")
    @Transactional
    @Operation(operationId = "bulkUpdateDocument", description = "Bulk Update an document")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DocumentDetailDTO[].class)))
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    public Response bulkUpdateDocument(List<DocumentCreateUpdateDTO> dto) {
        Log.info(CLASS_NAME, "Entered bulkUpdateDocument method", null);
        Iterator<DocumentCreateUpdateDTO> it = dto.listIterator();
        List<Document> document1 = new ArrayList<>();
        while (it.hasNext()) {
            DocumentCreateUpdateDTO dto1 = it.next();
            var document = documentDAO.findDocumentById(dto1.getId());
            if (Objects.isNull(document)) {
                throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                        getDocumentNotFoundMsg(dto1.getId()));
            }
            try {
                document = documentService.updateDocument(document, dto1);
            } catch (Exception e) {
                Log.error(e);
            }
            document1.add(document);
        }
        Log.info(CLASS_NAME, "Exited bulkUpdateDocument method", null);
        return Response.status(Response.Status.CREATED)
                .entity(documentMapper.mapDetailBulk(documentDAO.update(document1.stream())))
                .build();
    }

    @DELETE
    @Path("/delete-bulk-documents")
    @RolesAllowed("document-admin")
    @Transactional
    @Operation(operationId = "deleteBulkDocuments", description = "Delete Multiple Document by Ids")
    @APIResponse(responseCode = "204", description = "Delete Documents by ids")
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    public Response deleteBulkDocuments(List<String> ids) {
        Log.info(CLASS_NAME, "Entered deleteBulkDocuments method", null);
        List<String> listOfFilesIdToBeDeleted = new ArrayList<>();
        Iterator<String> itr = ids.iterator();
        while (itr.hasNext()) {
            String currentDocId = itr.next();
            var document = documentDAO.findById(currentDocId);
            if (Objects.isNull(document)) {
                throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                        getDocumentNotFoundMsg(currentDocId));
            }
            listOfFilesIdToBeDeleted.addAll(documentService.getFilesIdToBeDeletedInDocument(document));
            documentDAO.delete(document);
            listOfFilesIdToBeDeleted.stream().forEach(eachFileId -> documentService.asyncDeleteForAttachments(eachFileId));
        }
        Log.info(CLASS_NAME, "Exited deleteBulkDocuments method", null);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/show-all-documents")
    @Transactional
    @RolesAllowed({ "document-admin", "document-responsible", "document-user" })
    @Operation(operationId = "showAllDocumentsByCriteria", description = "Gets all documents by criteria")
    @APIResponse(responseCode = "200", description = "The corresponding documents resource", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DocumentDetailDTO[].class)))
    @APIResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    public Response showAllDocumentsByCriteria(@BeanParam DocumentSearchCriteriaDTO criteriaDTO) {
        Log.info(CLASS_NAME, "Entered showAllDocumentsByCriteria method", null);
        DocumentSearchCriteria criteria = documentMapper.map(criteriaDTO);
        if (Objects.nonNull(criteriaDTO.getStartDate()) && !criteriaDTO.getStartDate().isEmpty()) {

            criteria.setStartDate(LocalDateTime.parse(criteriaDTO.getStartDate(), CUSTOM_DATE_TIME_FORMATTER));
        }
        if (Objects.nonNull(criteriaDTO.getEndDate()) && !criteriaDTO.getEndDate().isEmpty()) {

            criteria.setEndDate(LocalDateTime.parse(criteriaDTO.getEndDate(), CUSTOM_DATE_TIME_FORMATTER));
        }
        List<Document> documents = documentDAO.findAllDocumentsBySearchCriteria(criteria);
        Log.info(CLASS_NAME, "Exited showAllDocumentsByCriteria method", null);
        return Response.ok(documentMapper.mapDocuments(documents))
                .build();
    }

    private String getDocumentNotFoundMsg(String id) {
        Log.info(CLASS_NAME, "Entered getDocumentNotFoundMsg method", null);
        Log.info(CLASS_NAME, "Exited getDocumentNotFoundMsg method", null);
        return "Document with id " + id + " was not found.";

    }
}
