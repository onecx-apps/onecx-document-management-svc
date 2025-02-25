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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;
import org.onecx.document.management.domain.criteria.DocumentSearchCriteria;
import org.onecx.document.management.domain.daos.AttachmentDAO;
import org.onecx.document.management.domain.daos.ChannelDAO;
import org.onecx.document.management.domain.daos.DocumentDAO;
import org.onecx.document.management.domain.daos.MinioAuditLogDAO;
import org.onecx.document.management.domain.daos.StorageUploadAuditDAO;
import org.onecx.document.management.domain.models.entities.*;
import org.onecx.document.management.rs.v1.RestException;
import org.onecx.document.management.rs.v1.mappers.DocumentMapper;
import org.onecx.document.management.rs.v1.services.DocumentService;
import org.tkit.quarkus.jpa.daos.PageResult;

import gen.org.onecx.document.management.rs.v1.DocumentControllerV1Api;
import gen.org.onecx.document.management.rs.v1.model.DocumentCreateUpdateDTO;
import gen.org.onecx.document.management.rs.v1.model.DocumentResponseDTO;
import gen.org.onecx.document.management.rs.v1.model.DocumentSearchCriteriaDTO;
import gen.org.onecx.document.management.rs.v1.model.LifeCycleState;
import io.minio.errors.*;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class DocumentController implements DocumentControllerV1Api {

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

    @Override
    public Response getDocumentById(String id) {
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

    @Override
    @Transactional
    public Response getDocumentByCriteria(String channelName, String createdBy, String endDate, String id, String name,
            String objectReferenceId, String objectReferenceType, Integer page, Integer size, String startDate,
            List<LifeCycleState> state, List<String> typeId) {
        Log.info(CLASS_NAME, "Entered getDocumentByCriteria method", null);
        DocumentSearchCriteriaDTO criteriaDTO = new DocumentSearchCriteriaDTO();
        criteriaDTO.setChannelName(channelName);
        criteriaDTO.setCreateBy(createdBy);
        criteriaDTO.setEndDate(endDate);
        criteriaDTO.setId(id);
        criteriaDTO.setName(name);
        criteriaDTO.setObjectReferenceId(objectReferenceId);
        criteriaDTO.setObjectReferenceType(objectReferenceType);
        Optional.ofNullable(page).ifPresent(criteriaDTO::setPageNumber);
        Optional.ofNullable(size).ifPresent(criteriaDTO::setPageSize);
        criteriaDTO.setStartDate(startDate);
        criteriaDTO.setLifeCycleState(state);
        criteriaDTO.setDocumentTypeId(typeId);
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

    @Override
    @Transactional
    public Response deleteDocumentById(String id) {
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

    @Override
    public Response createDocument(DocumentCreateUpdateDTO documentCreateUpdateDTO) {
        Log.info(CLASS_NAME, "Entered createDocument method", null);
        var document = documentService.createDocument(documentCreateUpdateDTO);
        Log.info(CLASS_NAME, "Exited createDocument method", null);
        return Response.status(Response.Status.CREATED)
                .entity(documentMapper.mapDetail(document))
                .build();
    }

    @Override
    public Response uploadAllFiles(String documentId, MultipartFormDataInput input) {
        Log.info(CLASS_NAME, "Entered multipleFileUploads method", null);
        Map<String, Integer> map = null;
        try {
            map = documentService.uploadAttachment(documentId, input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var responseDTO = new DocumentResponseDTO();
        responseDTO.setAttachmentResponse(map);
        Log.info(CLASS_NAME, "Exited multipleFileUploads method", null);
        return Response.status(Response.Status.CREATED)
                .entity(responseDTO)
                .build();
    }

    @Override
    @Transactional
    public Response getFailedAttachmentData(String documentId) {
        Log.info(CLASS_NAME, "Entered getFailedAttachmentById method", null);
        List<StorageUploadAudit> failedAttachmentList = storageUploadAuditDAO
                .findFailedAttachmentsByDocumentId(documentId);
        Log.info(CLASS_NAME, "Exited getFailedAttachmentById method", null);
        return Response.status(Response.Status.OK)
                .entity(documentMapper.mapStorageUploadAudit(failedAttachmentList))
                .build();
    }

    @Override
    @Transactional
    public Response updateDocument(String id, DocumentCreateUpdateDTO documentCreateUpdateDTO) {
        Log.info(CLASS_NAME, "Entered updateDocument method", null);
        var document = documentDAO.findDocumentById(id);
        if (Objects.isNull(document)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND, getDocumentNotFoundMsg(id));
        }
        document = documentService.updateDocument(document, documentCreateUpdateDTO);
        Log.info(CLASS_NAME, "Exited updateDocument method", null);
        return Response.status(Response.Status.CREATED)
                .entity(documentMapper.mapDetail(documentDAO.update(document)))
                .build();
    }

    @Override
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

    @Override
    public Response getFile(String attachmentId) {
        Log.info(CLASS_NAME, "Entered getFile method", null);
        var attachment = attachmentDAO.findById(attachmentId);
        if (Objects.isNull(attachment)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try (InputStream object = documentService.getObjectFromObjectStore(attachmentId)) {
            Log.info(CLASS_NAME, "Exited getFile method", null);
            return Response.ok(object)
                    .header("Content-Disposition", String.format("attachment;filename=%s", attachment.getFileName()))
                    .build();
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException | NoSuchAlgorithmException
                | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR,
                    e.getMessage());
        }
    }

    @Override
    public Response getAllDocumentAttachmentsAsZip(String documentId, String clientTimezone) {
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

    @Override
    @Transactional
    public Response deleteFilesInBulk(List<String> attachmentIds) {
        Log.info(CLASS_NAME, "Entered deleteFilesInBulk method", null);
        documentService.updateAttachmentStatusInBulk(attachmentIds);
        attachmentIds.stream().forEach(attachmentId -> documentService.asyncDeleteForAttachments(attachmentId));
        Log.info(CLASS_NAME, "Exited deleteFilesInBulk method", null);
        return Response.noContent().build();
    }

    @Override
    @Transactional
    public Response bulkUpdateDocument(List<DocumentCreateUpdateDTO> documentCreateUpdateDTO) {
        Log.info(CLASS_NAME, "Entered bulkUpdateDocument method", null);
        Iterator<DocumentCreateUpdateDTO> it = documentCreateUpdateDTO.listIterator();
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

    @Override
    @Transactional
    public Response deleteBulkDocuments(List<String> requestBody) {
        Log.info(CLASS_NAME, "Entered deleteBulkDocuments method", null);
        List<String> listOfFilesIdToBeDeleted = new ArrayList<>();
        Iterator<String> itr = requestBody.iterator();
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

    @Override
    @Transactional
    public Response showAllDocumentsByCriteria(String channelName, String createdBy, String endDate, String id, String name,
            String objectReferenceId, String objectReferenceType, Integer page, Integer size, String startDate,
            List<LifeCycleState> state, List<String> typeId) {
        Log.info(CLASS_NAME, "Entered showAllDocumentsByCriteria method", null);
        DocumentSearchCriteriaDTO criteriaDTO = new DocumentSearchCriteriaDTO();
        criteriaDTO.setChannelName(channelName);
        criteriaDTO.setCreateBy(createdBy);
        criteriaDTO.setEndDate(endDate);
        criteriaDTO.setId(id);
        criteriaDTO.setName(name);
        criteriaDTO.setObjectReferenceId(objectReferenceId);
        criteriaDTO.setObjectReferenceType(objectReferenceType);
        Optional.ofNullable(page).ifPresent(criteriaDTO::setPageNumber);
        Optional.ofNullable(size).ifPresent(criteriaDTO::setPageSize);
        criteriaDTO.setStartDate(startDate);
        criteriaDTO.setLifeCycleState(state);
        criteriaDTO.setDocumentTypeId(typeId);
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
