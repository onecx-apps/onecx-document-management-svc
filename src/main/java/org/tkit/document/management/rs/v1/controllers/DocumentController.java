package org.tkit.document.management.rs.v1.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.FileTime;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.tkit.document.management.domain.criteria.DocumentSearchCriteria;
import org.tkit.document.management.domain.daos.AttachmentDAO;
import org.tkit.document.management.domain.daos.ChannelDAO;
import org.tkit.document.management.domain.daos.DocumentDAO;
import org.tkit.document.management.domain.models.entities.Attachment;
import org.tkit.document.management.domain.models.entities.Channel;
import org.tkit.document.management.domain.models.entities.Document;
import org.tkit.document.management.rs.v1.mappers.DocumentMapper;
import org.tkit.document.management.rs.v1.models.*;
import org.tkit.document.management.rs.v1.services.DocumentService;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.exceptions.RestException;
import org.tkit.quarkus.rs.models.PageResultDTO;

import io.minio.errors.*;
import io.quarkus.logging.Log;

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
    DocumentMapper documentMapper;

    @Inject
    DocumentService documentService;

    public static final DateTimeFormatter CUSTOM_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // The response from the download attachment zip API will have this as the Content-Disposition header value.
    public static final String ATTACHMENT_ZIP_CONTENT_DISPOSITION_HEADER = "attachment; filename=\"attachments.zip\"";

    @GET
    @Path("/{id}")
    @RolesAllowed({ "document-admin", "document-responsible", "document-user" })
    @Operation(operationId = "getDocumentById", description = "Gets Document by Id")
    @APIResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DocumentDetailDTO.class)))
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    public Response getDocumentById(@PathParam("id") String id) {
        Document document = documentDAO.findDocumentById(id);
        if (Objects.isNull(document)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND, getDocumentNotFoundMsg(id));
        }
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
        DocumentSearchCriteria criteria = documentMapper.map(criteriaDTO);
        if (criteriaDTO.getStartDate() != null && !criteriaDTO.getStartDate().isEmpty()) { //added this for date search

            criteria.setStartDate(LocalDateTime.parse(criteriaDTO.getStartDate(), CUSTOM_DATE_TIME_FORMATTER));
        }
        if (criteriaDTO.getEndDate() != null && !criteriaDTO.getEndDate().isEmpty()) {

            criteria.setEndDate(LocalDateTime.parse(criteriaDTO.getEndDate(), CUSTOM_DATE_TIME_FORMATTER));
        }
        PageResult<Document> documents = documentDAO.findBySearchCriteria(criteria);
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
        Document document = documentDAO.findById(id);
        if (Objects.isNull(document)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND, getDocumentNotFoundMsg(id));
        }
        documentService.deleteFilesInDocument(document);
        documentDAO.delete(document);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @RolesAllowed({ "document-admin", "document-responsible" })
    @Operation(operationId = "createDocument", description = "Create Document")
    @APIResponse(responseCode = "201", description = "Created Document resource", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DocumentDetailDTO.class)), headers = {
            @Header(name = "Location", description = "URL for the create Document resource")
    })
    @APIResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    public Response createDocument(@Valid DocumentCreateUpdateDTO documentDTO) {
        Document document = documentService.createDocument(documentDTO);
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
    @APIResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    public Response multipleFileUploads(@PathParam("documentId") String documentId, @MultipartForm MultipartFormDataInput input)
            throws IOException {
        HashMap<String, Integer> map = documentService.uploadAttachment(documentId, input);
        DocumentResponseDTO responseDTO = new DocumentResponseDTO();
        responseDTO.setAttachmentResponse(map);
        return Response.status(Response.Status.CREATED)
                .entity(responseDTO)
                .build();
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
        Document document = documentDAO.findDocumentById(id);
        if (Objects.isNull(document)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND, getDocumentNotFoundMsg(id));
        }
        document = documentService.updateDocument(document, dto);

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

        //List of unique alphabetically sorted channel names ignoring cases
        List<Channel> uniqueSortedChannelNames = channelDAO.findAllSortedByNameAsc()
                .filter(distinctByKey(c -> c.getName().toLowerCase(Locale.ROOT)))
                .toList();

        return Response.status(Response.Status.OK)
                .entity(documentMapper.mapChannels(uniqueSortedChannelNames))
                .build();
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = Collections.newSetFromMap(new ConcurrentHashMap<>());
        return t -> seen.add(keyExtractor.apply(t));
    }

    @GET
    @Path("/file/{attachmentId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(operationId = "getFile", description = "Get attachment's file")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(implementation = InputStream.class)))
    @APIResponse(responseCode = "404", description = "Not found")
    @APIResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    public Response getFile(@PathParam("attachmentId") String attachmentId)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        Attachment attachment = attachmentDAO.findById(attachmentId);
        if (attachment == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        InputStream object = documentService.getObjectFromObjectStore(attachmentId);
        return Response.ok(object)
                .header("Content-Disposition", String.format("attachment;filename=%s", attachment.getFileName())).build();
    }

    @GET
    @Path("/file/{documentId}/attachments")
    @RolesAllowed({ "document-admin", "document-responsible", "document-user" })
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(operationId = "getAllDocumentAttachmentsAsZip", description = "Get all the attachments of the document packaged in a zip file")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(implementation = InputStream.class)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "204", description = "No content")
    @APIResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    public Response getAllDocumentAttachmentsAsZip(@PathParam("documentId") String documentId,
            @HeaderParam("client-timezone") String clientTimezone) {
        try {
            /* Retrieve the document by its ID */
            Document document = documentDAO.findById(documentId);

            /*
             * Return a bad request response if the document is not found because a document
             * should exist for this request to have come in
             */
            if (document == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            /* Retrieve the attachment details of this document */
            Set<Attachment> documentAttachmentSet = document.getAttachments();

            /*
             * If the document has no attachments return a 204 error because there is no
             * content to return.
             */
            if (documentAttachmentSet == null || documentAttachmentSet.isEmpty())
                return Response.status(Response.Status.NO_CONTENT).build();

            /* Code to create a zip file containing all the attachment files */
            StreamingOutput stream = output -> {

                /*
                 * Use ZipOutputStream to create the zip and compress the its contents. This
                 * reduces the size of the zip file and saves bandwidth and data while
                 * transmitting over the internet. We are using the default compression level
                 * because it is a good balance between file size and compression speed.
                 */
                try (ZipOutputStream zip = new ZipOutputStream(output)) {

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
                                    ZipEntry entry = new ZipEntry(
                                            attachment.getFileName());
                                    entry.setSize(object.available());
                                    ZoneId clientZoneId = clientTimezone != null ? ZoneId.of(clientTimezone)
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
    @Path("/file/{attachmentId}")
    @RolesAllowed({ "document-admin", "document-responsible" })
    @Transactional
    @Operation(operationId = "deleteFile", description = "Delete attachment's file")
    @APIResponse(responseCode = "204", description = "No Content")
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "404", description = "Not found")
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    public Response deleteFile(@PathParam("attachmentId") String attachmentId)
            throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException,
            ErrorResponseException, XmlParserException, InsufficientDataException, InternalException {
        Attachment attachment = attachmentDAO.findById(attachmentId);
        if (attachment == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        documentService.deleteFileInAttachment(attachment);
        attachmentDAO.delete(attachment);

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
        Iterator<DocumentCreateUpdateDTO> it = dto.listIterator();
        List<Document> document1 = new ArrayList<>();
        while (it.hasNext()) {
            DocumentCreateUpdateDTO dto1 = it.next();
            Document document = documentDAO.findDocumentById(dto1.getId());
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
        Iterator<String> itr = ids.iterator();
        while (itr.hasNext()) {
            String currentDocId = itr.next();
            Document document = documentDAO.findById(currentDocId);
            if (Objects.isNull(document)) {
                throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                        getDocumentNotFoundMsg(currentDocId));
            }
            documentService.deleteFilesInDocument(document);
            documentDAO.delete(document);
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private String getDocumentNotFoundMsg(String id) {
        return "Document with id " + id + " was not found.";
    }

}
