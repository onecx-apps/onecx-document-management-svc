package org.onecx.document.management.rs.v1.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;
import org.onecx.document.management.domain.daos.AttachmentDAO;
import org.onecx.document.management.domain.daos.DocumentDAO;
import org.onecx.document.management.domain.daos.DocumentTypeDAO;
import org.onecx.document.management.domain.daos.MinioAuditLogDAO;
import org.onecx.document.management.domain.daos.StorageUploadAuditDAO;
import org.onecx.document.management.domain.daos.SupportedMimeTypeDAO;
import org.onecx.document.management.domain.models.entities.Attachment;
import org.onecx.document.management.domain.models.entities.Category;
import org.onecx.document.management.domain.models.entities.Channel;
import org.onecx.document.management.domain.models.entities.Document;
import org.onecx.document.management.domain.models.entities.DocumentCharacteristic;
import org.onecx.document.management.domain.models.entities.DocumentRelationship;
import org.onecx.document.management.domain.models.entities.DocumentSpecifications;
import org.onecx.document.management.domain.models.entities.DocumentTypes;
import org.onecx.document.management.domain.models.entities.MinioAuditLog;
import org.onecx.document.management.domain.models.entities.RelatedObjectRef;
import org.onecx.document.management.domain.models.entities.RelatedPartyRef;
import org.onecx.document.management.domain.models.entities.StorageUploadAudit;
import org.onecx.document.management.domain.models.entities.SupportedMimeTypes;
import org.onecx.document.management.domain.models.enums.AttachmentUnit;
import org.onecx.document.management.rs.v1.CustomException;
import org.onecx.document.management.rs.v1.RestException;
import org.onecx.document.management.rs.v1.mappers.DocumentMapper;
import org.onecx.document.management.rs.v1.mappers.DocumentSpecificationMapper;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import gen.org.onecx.document.management.rs.v1.model.AttachmentCreateUpdate;
import gen.org.onecx.document.management.rs.v1.model.DocumentCreateUpdate;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.extern.slf4j.Slf4j;

/**
 * Document service.
 */
@Slf4j
@ApplicationScoped
public class DocumentService {

    @Inject
    DocumentDAO documentDAO;

    @Inject
    DocumentMapper documentMapper;

    @Inject
    DocumentTypeDAO typeDAO;

    @Inject
    DocumentSpecificationMapper documentSpecificationMapper;

    @Inject
    SupportedMimeTypeDAO mimeTypeDAO;

    @Inject
    AttachmentDAO attachmentDAO;

    @Inject
    StorageUploadAuditDAO storageUploadAuditDAO;

    @Inject
    MinioAuditLogDAO minioAuditLogDAO;

    @Inject
    MinioClient minioClient;

    @ConfigProperty(name = "minio.bucket.folder")
    String bucketFolder;

    @ConfigProperty(name = "quarkus.minio.url")
    String minioUrl;

    private static final Pattern FILENAME_PATTERN = Pattern.compile("filename=\\\"(.*)\\\"");

    private static final String SLASH = "/";

    private static final String ATTACHMENT_ID_LIST_MEDIA_TYPE = "text/plain";

    private static final String FORM_DATA_MAP_KEY = "file";

    private static final String HEADER_KEY = "Content-Disposition";

    private static final String STRING_TOKEN_DELIMITER = ",";

    private static final String CLASS_NAME = "DocumentService";

    public Document createDocument(DocumentCreateUpdate dto) {
        Log.info(CLASS_NAME, "Entered createDocument method", null);
        var document = documentMapper.map(dto);
        setType(dto, document);
        setSpecification(dto, document);
        setAttachments(dto, document);
        Log.info(CLASS_NAME, "Exited createDocument method", null);
        return documentDAO.create(document);
    }

    @Transactional
    public Map<String, Integer> uploadAttachment(String documentId, MultipartFormDataInput input)
            throws IOException {
        Log.info(CLASS_NAME, "Entered uploadAttachment method", null);
        HashMap<String, Integer> map = new HashMap<>();
        Set<Attachment> newAttachmentSet = new HashSet<>();
        var document = documentDAO.findDocumentById(documentId);
        if (Objects.isNull(document)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getDocumentNotFoundMsg(documentId));
        }
        String mediaType = "";
        for (Map.Entry<String, Collection<FormValue>> attribute : input.getValues().entrySet()) {
            for (FormValue fv : attribute.getValue()) {
                if (fv.isFileItem()) {
                    mediaType = fv.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
                }
            }
        }
        Map<String, Collection<FormValue>> uploadForm = input.getValues();

        Collection<FormValue> inputParts = uploadForm.get(FORM_DATA_MAP_KEY);
        if (String.valueOf(MediaType.valueOf(mediaType))
                .equals(ATTACHMENT_ID_LIST_MEDIA_TYPE)) {
            List<String> attachmentIdList = getAttachmentIdList(inputParts.stream().toList());
            inputParts.remove(0);
            if (!attachmentIdList.isEmpty()) {
                attachmentIdList.stream().forEach(attachmentId -> {
                    Optional<Attachment> matchedAttachment = document.getAttachments().stream()
                            .filter(attachment -> attachmentId.equals(attachment.getId())).findFirst();
                    matchedAttachment.ifPresent(newAttachmentSet::add);
                });
            }
        } else {
            newAttachmentSet.addAll(document.getAttachments());
        }
        if (!newAttachmentSet.isEmpty()) {
            newAttachmentSet.stream()
                    .forEach(attachment -> {
                        Optional<FormValue> matchedInputPart = inputParts.stream().filter(inputPart -> {
                            String filename = inputPart.getFileName();
                            return attachment.getFileName().equals(filename);
                        }).findFirst();
                        String strFilenameFileId = attachment.getId() + SLASH + attachment.getName();
                        try {
                            if (matchedInputPart.isPresent()) {
                                InputStream inputPartBody = matchedInputPart.get().getFileItem().getInputStream();
                                byte[] fileBytes = IOUtils.toByteArray(inputPartBody);
                                InputStream fileByteArrayInputStream = new ByteArrayInputStream(fileBytes);
                                String contentType = URLConnection.guessContentTypeFromStream(fileByteArrayInputStream);
                                uploadFileToObjectStorage(fileBytes, attachment.getId());
                                map.put(strFilenameFileId, Response.Status.CREATED.getStatusCode());
                                updateAttachmentAfterUpload(attachment, new BigDecimal(fileBytes.length), contentType);
                            }
                        } catch (Exception e) {
                            map.put(strFilenameFileId, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                            createStorageUploadAuditRecords(documentId, document, attachment);
                            log.error("Error Message: ", e);
                        }
                    });
        } else {
            Log.info(CLASS_NAME, "Exited uploadAttachment method", null);
            return map;
        }
        Log.info(CLASS_NAME, "Exited uploadAttachment method", null);
        return map;
    }

    private List<String> getAttachmentIdList(List<FormValue> inputPartList) throws IOException {
        List<String> attachmentIdList = new ArrayList<>();
        var stringTokenizer = new StringTokenizer(String.valueOf(inputPartList.get(0).getFileItem()),
                STRING_TOKEN_DELIMITER);
        while (stringTokenizer.hasMoreTokens()) {
            attachmentIdList.add(stringTokenizer.nextToken());
        }
        return attachmentIdList;
    }

    public void createStorageUploadAuditRecords(String documentId, Document document, Attachment attachment) {
        Log.info(CLASS_NAME, "Entered createStorageUploadAuditRecords method", null);
        var storageUploadAudit = new StorageUploadAudit();
        storageUploadAudit.setDocumentId(documentId);
        storageUploadAudit.setDocumentName(document.getName());
        storageUploadAudit.setDocumentDescription(document.getDescription());
        storageUploadAudit.setDocumentVersion(document.getDocumentVersion());
        storageUploadAudit.setLifeCycleState(document.getLifeCycleState().toString());
        storageUploadAudit.setChannelId(document.getChannel().getId());
        storageUploadAudit.setChannelName(document.getChannel().getName());
        storageUploadAudit.setDocumentTypeId(document.getType().getId());
        storageUploadAudit.setDocumentTypeName(document.getType().getName());
        storageUploadAudit.setAttachmentId(attachment.getId());
        storageUploadAudit.setFileName(attachment.getFileName());
        storageUploadAudit.setName(attachment.getName());
        storageUploadAudit.setAttachmentDescription(attachment.getDescription());
        storageUploadAudit.setMimeTypeId(attachment.getMimeType().getId());
        storageUploadAudit.setMimeTypeName(attachment.getMimeType().getName());
        if (Objects.nonNull(document.getSpecification())) {
            storageUploadAudit.setSpecificationId(document.getSpecification().getId());
            storageUploadAudit.setSpecificationName(document.getSpecification().getName());
        }
        storageUploadAudit.setRelatedObjectId(document.getRelatedObject().getId());
        storageUploadAudit.setInvolvement(document.getRelatedObject().getInvolvement());
        storageUploadAudit.setObjectReferenceType(document.getRelatedObject().getObjectReferenceType());
        storageUploadAudit.setObjectReferenceId(document.getRelatedObject().getObjectReferenceId());
        storageUploadAuditDAO.create(storageUploadAudit);
        Log.info(CLASS_NAME, "Exited createStorageUploadAuditRecords method", null);

    }

    /**
     * Updates the basic fields in {@link Document}, updates collections and
     * elements in collections: {@link Attachment},
     * {@link DocumentCharacteristic}, {@link DocumentRelationship},
     * {@link RelatedPartyRef}, {@link Category},
     * updates objects: {@link Channel}, {@link RelatedObjectRef} and sets
     * {@link DocumentTypes}
     * and {@link DocumentSpecifications}.
     *
     * @param dto a {@link DocumentCreateUpdate}
     * @return a {@link Document}
     */
    @Transactional
    public Document updateDocument(Document document, DocumentCreateUpdate dto) {
        Log.info(CLASS_NAME, "Entered updateDocument method", null);
        documentMapper.update(dto, document);
        setType(dto, document);
        setSpecification(dto, document);
        updateChannelInDocument(document, dto);
        updateRelatedObjectRefInDocument(document, dto);
        documentMapper.updateTraceableCollectionsInDocument(document, dto);
        updateAttachmentsInDocument(document, dto);
        Log.info(CLASS_NAME, "Exited updateDocument method", null);
        return document;
    }

    public InputStream getObjectFromObjectStore(String objectId)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        Log.info(CLASS_NAME, "Entered getObjectFromObjectStore method", null);
        var getObjectArgs = GetObjectArgs.builder()
                .bucket(bucketFolder)
                .object(objectId)
                .build();
        Log.info(CLASS_NAME, "Exited getObjectFromObjectStore method", null);
        return minioClient.getObject(getObjectArgs);
    }

    @Transactional
    public void updateAttachmentStatusInBulk(List<String> attachmentIds) {
        Log.info(CLASS_NAME, "Entered updateAttachmentStatusInBulk method", null);
        attachmentIds.stream().forEach(attachmentId -> {
            var attachment = attachmentDAO.findById(attachmentId);
            if (Objects.isNull(attachment)) {
                throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                        getAttachmentNotFoundMsg(attachmentId));
            }
            attachment.setStorageUploadStatus(false);
        });
        Log.info(CLASS_NAME, "Exited updateAttachmentStatusInBulk method", null);
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public void asyncDeleteForAttachments(String attachmentId) {
        Log.info(CLASS_NAME, "Entered asyncDeleteForAttachments method", null);
        Uni.createFrom().item(attachmentId).emitOn(Infrastructure.getDefaultWorkerPool()).subscribe().with(
                this::deleteFileInAttachmentAsync, Throwable::printStackTrace);
        Log.info(CLASS_NAME, "Entered asyncDeleteForAttachments method", null);
    }

    public void deleteFileInAttachmentAsync(String attachmentId) {
        Log.info(CLASS_NAME, "Entered deleteFileInAttachmentAsync method", null);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketFolder)
                            .object(attachmentId)
                            .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException
                | InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException
                | XmlParserException e) {
            var minioAuditLog = new MinioAuditLog();
            minioAuditLog.setAttachmentId(attachmentId);
            minioAuditLogDAO.create(minioAuditLog);
            throw new CustomException("An error occurred while deleting the attachment file.", e);
        }
        Log.info(CLASS_NAME, "Exited deleteFileInAttachmentAsync method", null);
    }

    @Transactional
    public List<String> getFilesIdToBeDeletedInDocument(Document document) {
        Log.info(CLASS_NAME, "Entered getFilesIdToBeDeletedInDocument method", null);
        if (!Objects.isNull(document.getAttachments())) {
            List<String> attachmentIds = document.getAttachments().stream().map(TraceableEntity::getId)
                    .toList();
            updateAttachmentStatusInBulk(attachmentIds);
            Log.info(CLASS_NAME, "Exited getFilesIdToBeDeletedInDocument method", null);
            return attachmentIds;
        }
        return Collections.emptyList();
    }

    /**
     * Finds the type {@link DocumentTypes} by id and sets the type in the document
     * entity {@link Document}.
     *
     * @param dto a {@link DocumentCreateUpdate}
     * @param document a {@link Document}
     */
    private void setType(DocumentCreateUpdate dto, Document document) {

        Log.info(CLASS_NAME, "Entered setType method", null);
        var documentType = typeDAO.findById(dto.getTypeId());
        if (Objects.isNull(documentType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getDocumentNotFoundMsg(dto.getTypeId()));
        }
        document.setType(documentType);
        Log.info(CLASS_NAME, "Exited setType method", null);

    }

    /**
     * Finds the specification {@link DocumentSpecification} by id and sets the
     * specification
     * in the document entity {@link Document}.
     *
     * @param dto a {@link DocumentCreateUpdate}
     * @param document a {@link Document}
     */
    private void setSpecification(DocumentCreateUpdate dto, Document document) {
        Log.info(CLASS_NAME, "Entered setSpecification method", null);
        if (Objects.isNull(dto.getSpecification())) {
            document.setSpecification(null);
        } else {
            gen.org.onecx.document.management.rs.v1.model.DocumentSpecificationCreateUpdate docSpecDto = dto
                    .getSpecification();
            var documentSpecification = documentSpecificationMapper.map(docSpecDto);
            document.setSpecification(documentSpecification);
        }
        Log.info(CLASS_NAME, "Exited setSpecification method", null);
    }

    /**
     * Finds the {@link SupportedMimeTypes} by the given id.
     *
     * @param dto a {@link AttachmentCreateUpdate}
     * @return a {@link SupportedMimeTypes}
     *         or it throws an error if it can't find a {@link SupportedMimeTypes}
     *         given id.
     */
    private SupportedMimeTypes getSupportedMimeType(AttachmentCreateUpdate dto) {
        Log.info(CLASS_NAME, "Entered getSupportedMimeType method", null);
        SupportedMimeTypes mimeType = mimeTypeDAO.findById(dto.getMimeTypeId());
        if (Objects.isNull(mimeType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getSupportedMimeTypeNotFoundMsg(dto.getMimeTypeId()));
        }
        Log.info(CLASS_NAME, "Exited getSupportedMimeType method", null);
        return mimeType;
    }

    /**
     * Finds attachment's mimeType {@link SupportedMimeTypes} by id
     * and sets it in the attachment entity {@link Attachment}, then add
     * {@link Attachment}
     * in document entity {@link Document}.
     *
     * @param dto a {@link DocumentCreateUpdate}
     * @param document a {@link Document}
     */
    private void setAttachments(DocumentCreateUpdate dto, Document document) {
        Log.info(CLASS_NAME, "Entered setAttachments method", null);
        if (Objects.isNull(dto.getAttachments())) {
            document.setAttachments(null);
        } else {
            for (AttachmentCreateUpdate attachmentDTO : dto.getAttachments()) {
                if (Objects.isNull(attachmentDTO.getId()) || attachmentDTO.getId().isEmpty()) {
                    var mimeType = getSupportedMimeType(attachmentDTO);
                    var attachment = documentMapper.mapAttachment(attachmentDTO);
                    attachment.setMimeType(mimeType);
                    attachment.setStorageUploadStatus(false);
                    document.getAttachments().add(attachment);
                }
            }
        }
        Log.info(CLASS_NAME, "Exited setAttachments method", null);
    }

    /**
     * Updates {@link Channel} in {@link Document} or creates new {@link Channel}
     * and sets in {@link Document}.
     *
     * @param document a {@link Document}
     * @param updateDTO a {@link DocumentCreateUpdate}
     */
    private void updateChannelInDocument(Document document, DocumentCreateUpdate updateDTO) {
        Log.info(CLASS_NAME, "Entered updateChannelInDocument method", null);
        if (Objects.isNull(updateDTO.getChannel().getId()) || updateDTO.getChannel().getId().isEmpty()) {
            var channel = documentMapper.mapChannel(updateDTO.getChannel());
            document.setChannel(channel);
        } else {
            documentMapper.updateChannel(updateDTO.getChannel(), document.getChannel());
        }
        Log.info(CLASS_NAME, "Exited updateChannelInDocument method", null);
    }

    /**
     * Updates {@link RelatedObjectRef} in {@link Document} or creates new
     * {@link RelatedObjectRef} and sets in {@link Document}.
     *
     * @param document a {@link Document}
     * @param updateDTO a {@link DocumentCreateUpdate}
     */
    private void updateRelatedObjectRefInDocument(Document document, DocumentCreateUpdate updateDTO) {
        Log.info(CLASS_NAME, "Entered updateRelatedObjectRefInDocument method", null);
        if (Objects.isNull(updateDTO.getRelatedObject())) {
            document.setRelatedObject(null);
        } else {
            if (Objects.isNull(updateDTO.getRelatedObject().getId())
                    || updateDTO.getRelatedObject().getId().isEmpty()) {
                var relatedObjectRef = documentMapper.mapRelatedObjectRef(updateDTO.getRelatedObject());
                document.setRelatedObject(relatedObjectRef);
            } else {
                documentMapper.updateRelatedObjectRef(updateDTO.getRelatedObject(), document.getRelatedObject());
            }
        }
        Log.info(CLASS_NAME, "Exited updateRelatedObjectRefInDocument method", null);
    }

    /**
     * Updates {@link Attachment} in collection in {@link Document}
     * or creates {@link Attachment} sets {@link SupportedMimeTypes} in
     * {@link Attachment}
     * and add to collection or remove {@link Attachment} from collection.
     *
     * @param document a {@link Document}
     * @param updateDTO a {@link DocumentCreateUpdate}
     */
    private void updateAttachmentsInDocument(Document document, DocumentCreateUpdate updateDTO) {
        Log.info(CLASS_NAME, "Entered updateAttachmentsInDocument method", null);
        if (Objects.nonNull(updateDTO.getAttachments())) {
            for (Iterator<Attachment> i = document.getAttachments().iterator(); i.hasNext();) {
                Attachment entity = i.next();
                Optional<AttachmentCreateUpdate> dtoOptional = updateDTO.getAttachments().stream()
                        .filter(dto -> dto.getId() != null)
                        .filter(dto -> entity.getId().equals(dto.getId()))
                        .findFirst();
                if (dtoOptional.isPresent()) {
                    var mimeType = getSupportedMimeType(dtoOptional.get());
                    documentMapper.updateAttachment(dtoOptional.get(), entity);
                    entity.setMimeType(mimeType);
                }
            }
            setAttachments(updateDTO, document);
        }
        Log.info(CLASS_NAME, "Exited updateAttachmentsInDocument method", null);
    }

    private void uploadFileToObjectStorage(byte[] fileBytes, String id)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        Log.info(CLASS_NAME, "Entered uploadFileToObjectStorage method", null);
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketFolder)
                .object(id)
                .stream(new ByteArrayInputStream(fileBytes), fileBytes.length, -1)
                .build());
        Log.info(CLASS_NAME, "Exited uploadFileToObjectStorage method", null);

    }

    private void updateAttachmentAfterUpload(Attachment attachment, BigDecimal size, String contentType) {
        Log.info(CLASS_NAME, "Entered updateAttachmentAfterUpload method", null);
        attachment.setSize(size);
        attachment.setSizeUnit(AttachmentUnit.BYTES);
        attachment.setStorage("MinIO");
        attachment.setType(contentType);
        attachment.setExternalStorageURL(minioUrl);
        attachment.setStorageUploadStatus(true);
        Log.info(CLASS_NAME, "Exited updateAttachmentAfterUpload method", null);
    }

    private String getAttachmentNotFoundMsg(String attachmentId) {
        Log.info(CLASS_NAME, "Entered getAttachmentNotFoundMsg method", null);
        Log.info(CLASS_NAME, "Exited getAttachmentNotFoundMsg method", null);
        return String.format("The attachment with ID %s was not found.", attachmentId);
    }

    private String getDocumentNotFoundMsg(String documentId) {
        Log.info(CLASS_NAME, "Entered getDocumentNotFoundMsg method", null);
        Log.info(CLASS_NAME, "Exited getDocumentNotFoundMsg method", null);
        return String.format("The document with ID %s was not found.", documentId);
    }

    private String getSupportedMimeTypeNotFoundMsg(String mimeTypeId) {
        Log.info(CLASS_NAME, "Entered getSupportedMimeTypeNotFoundMsg method", null);
        Log.info(CLASS_NAME, "Exited getSupportedMimeTypeNotFoundMsg method", null);
        return String.format("The supported mime type with ID %s was not found.", mimeTypeId);
    }

}
