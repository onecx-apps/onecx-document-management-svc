package org.tkit.document.management.rs.v1.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.tkit.document.management.domain.daos.AttachmentDAO;
import org.tkit.document.management.domain.daos.DocumentDAO;
import org.tkit.document.management.domain.daos.DocumentSpecificationDAO;
import org.tkit.document.management.domain.daos.DocumentTypeDAO;
import org.tkit.document.management.domain.daos.StorageUploadAuditDAO;
import org.tkit.document.management.domain.daos.SupportedMimeTypeDAO;
import org.tkit.document.management.domain.models.entities.Attachment;
import org.tkit.document.management.domain.models.entities.Category;
import org.tkit.document.management.domain.models.entities.Channel;
import org.tkit.document.management.domain.models.entities.Document;
import org.tkit.document.management.domain.models.entities.DocumentCharacteristic;
import org.tkit.document.management.domain.models.entities.DocumentRelationship;
import org.tkit.document.management.domain.models.entities.DocumentSpecification;
import org.tkit.document.management.domain.models.entities.DocumentType;
import org.tkit.document.management.domain.models.entities.RelatedObjectRef;
import org.tkit.document.management.domain.models.entities.RelatedPartyRef;
import org.tkit.document.management.domain.models.entities.StorageUploadAudit;
import org.tkit.document.management.domain.models.entities.SupportedMimeType;
import org.tkit.document.management.domain.models.enums.AttachmentUnit;
import org.tkit.document.management.rs.v1.mappers.DocumentMapper;
import org.tkit.document.management.rs.v1.mappers.DocumentSpecificationMapper;
import org.tkit.document.management.rs.v1.models.AttachmentCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.DocumentCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.DocumentSpecificationCreateUpdateDTO;
import org.tkit.quarkus.rs.exceptions.RestException;

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
import lombok.extern.slf4j.Slf4j;

/**
 * Document service.
 */
@Slf4j
@ApplicationScoped
@SuppressWarnings("java:S3776")
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
    MinioClient minioClient;

    @ConfigProperty(name = "minio.bucket")
    String bucket;

    @ConfigProperty(name = "bucketNamePrefix")
    String prefix;

    @ConfigProperty(name = "quarkus.minio.url")
    String url;

    private static final Pattern FILENAME_PATTERN = Pattern.compile("filename=\\\"(.*)\\\"");

    private static final String SLASH = "/";

    private static final String COMPLATE_MEDIA_TYPE = "text/plain;charset=us-ascii";

    private static final String FORM_DATA_MAP_KEY = "file";

    private static final String HEADER_KEY = "Content-Disposition";

    private static final String STRING_TOKEN_DELIMITER = ",";

    public Document createDocument(@Valid DocumentCreateUpdateDTO dto) {
        Log.info("DocumentService", "Entered createDocument method", null);
        Document document = documentMapper.map(dto);
        setType(dto, document);
        setSpecification(dto, document);
        setAttachments(dto, document);
        Log.info("DocumentService", "Exited createDocument method", null);
        return documentDAO.create(document);
    }

    private String extractFileName(InputPart inputPart) {
        Log.info("DocumentService", "Entered extractFileName method", null);
        MultivaluedMap<String, String> headers = inputPart.getHeaders();
        Matcher matcher = FILENAME_PATTERN.matcher(headers.getFirst(HEADER_KEY));
        String filename = null;
        if (matcher.find()) {
            filename = matcher.group(1);
        }
        Log.info("DocumentService", "Exited extractFileName method", null);
        return filename;
    }

    @Transactional
    public HashMap<String, Integer> uploadAttachment(String documentId, MultipartFormDataInput input)
            throws IOException {
        Log.info("DocumentService", "Entered uploadAttachment method", null);
        HashMap<String, Integer> map = new HashMap<>();
        Set<Attachment> newAttachmentSet = new HashSet<>();
        Document document = documentDAO.findDocumentById(documentId);
        if (Objects.isNull(document)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    "The document with ID " + documentId + " does not exist.");
        }
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get(FORM_DATA_MAP_KEY);
        if (String.valueOf(inputParts.get(0).getMediaType()).equals(COMPLATE_MEDIA_TYPE)) {
            List<String> attachmentIdList = new ArrayList<>();
            StringTokenizer stringTokenizer = new StringTokenizer(String.valueOf(inputParts.get(0).getBodyAsString()),
                    STRING_TOKEN_DELIMITER);
            while (stringTokenizer.hasMoreTokens()) {
                attachmentIdList.add(stringTokenizer.nextToken());
            }
            inputParts.remove(0);
            if (attachmentIdList.size() > 0) {
                attachmentIdList.stream().forEach(attachmentId -> {
                    Optional<Attachment> matchedAttachment = document.getAttachments().stream().filter(attachment -> {
                        if (attachmentId.equals(attachment.getId())) {
                            return true;
                        }
                        return false;
                    }).findFirst();
                    if (matchedAttachment.isPresent()) {
                        newAttachmentSet.add(matchedAttachment.get());
                    }
                });
            }
        } else {
            newAttachmentSet.addAll(document.getAttachments());
        }
        if (!Objects.isNull(newAttachmentSet)) {
            newAttachmentSet.stream()
                    .forEach(attachment -> {
                        Optional<InputPart> matchedInputPart = inputParts.stream().filter(inputPart -> {
                            String filename = extractFileName(inputPart);
                            if (attachment.getFileName().equals(filename)) {
                                return true;
                            }
                            return false;
                        }).findFirst();
                        String strFilenameFileId = attachment.getId() + SLASH + attachment.getName();
                        try {
                            InputStream inputPartBody = matchedInputPart.get().getBody(InputStream.class, null);
                            byte[] fileBytes = IOUtils.toByteArray(inputPartBody);
                            InputStream fileByteArrayInputStream = new ByteArrayInputStream(fileBytes);
                            String contentType = URLConnection.guessContentTypeFromStream(fileByteArrayInputStream);
                            uploadFileToObjectStorage(fileBytes, attachment.getId());
                            map.put(strFilenameFileId, Response.Status.CREATED.getStatusCode());
                            updateAttachmentAfterUpload(attachment, new BigDecimal(fileBytes.length), contentType);
                        } catch (Exception e) {
                            map.put(strFilenameFileId, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                            createStorageUploadAuditRecords(documentId, document, attachment);
                            log.error("Error Message: ", e);
                        }
                    });
        } else {
            Log.info("DocumentService", "Exited uploadAttachment method", null);
            return map;
        }
        Log.info("DocumentService", "Exited uploadAttachment method", null);
        return map;
    }

    public void createStorageUploadAuditRecords(String documentId, Document document, Attachment attachment) {
        Log.info("DocumentService", "Entered createStorageUploadAuditRecords method", null);
        StorageUploadAudit storageUploadAudit = new StorageUploadAudit();
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
        Log.info("DocumentService", "Exited createStorageUploadAuditRecords method", null);

    }

    /**
     * Updates the basic fields in {@link Document}, updates collections and
     * elements in collections: {@link Attachment},
     * {@link DocumentCharacteristic}, {@link DocumentRelationship},
     * {@link RelatedPartyRef}, {@link Category},
     * updates objects: {@link Channel}, {@link RelatedObjectRef} and sets
     * {@link DocumentType}
     * and {@link DocumentSpecification}.
     *
     * @param dto a {@link DocumentCreateUpdateDTO}
     * @return a {@link Document}
     */
    @Transactional
    public Document updateDocument(@Valid Document document, DocumentCreateUpdateDTO dto)
            throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException,
            ServerException, InternalException, XmlParserException, InsufficientDataException, ErrorResponseException {
        Log.info("DocumentService", "Entered updateDocument method", null);
        documentMapper.update(dto, document);
        setType(dto, document);
        setSpecification(dto, document);
        updateChannelInDocument(document, dto);
        updateRelatedObjectRefInDocument(document, dto);
        documentMapper.updateTraceableCollectionsInDocument(document, dto);
        updateAttachmentsInDocument(document, dto);
        Log.info("DocumentService", "Exited updateDocument method", null);
        return document;
    }

    public InputStream getObjectFromObjectStore(String objectId)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        Log.info("DocumentService", "Entered getObjectFromObjectStore method", null);
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(prefix + bucket)
                .object(objectId)
                .build();
        Log.info("DocumentService", "Exited getObjectFromObjectStore method", null);
        return minioClient.getObject(getObjectArgs);
    }

    @Transactional
    public void deleteFileInAttachment(Attachment attachment)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        Log.info("DocumentService", "Entered deleteFileInAttachment method", null);
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(prefix + bucket)
                        .object(attachment.getId())
                        .build());
        attachment.setSize(null);
        attachment.setType(null);
        attachment.setStorage(null);
        attachment.setSizeUnit(null);
        attachment.setExternalStorageURL(null);
        Log.info("DocumentService", "Exited deleteFileInAttachment method", null);
    }

    @Transactional
    public void deleteAttachmentInBulk(List<String> attachmentIds) {
        Log.info("DocumentService", "Entered deleteAttachmentInBulk method", null);
        attachmentIds.stream().forEach((attachmentId) -> {
            Attachment attachment = attachmentDAO.findById(attachmentId);
            if (Objects.isNull(attachment)) {
                throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                        getAttachmentNotFoundMsg(attachmentId));
            }
            try {
                deleteFileInAttachment(attachment);
            } catch (IOException | InvalidKeyException | InvalidResponseException | InsufficientDataException
                    | NoSuchAlgorithmException | ServerException | InternalException | XmlParserException
                    | ErrorResponseException e) {
                throw new RuntimeException(e);
            }
            attachmentDAO.delete(attachment);
        });
        Log.info("DocumentService", "Exited deleteAttachmentInBulk method", null);
    }

    @Transactional
    public void deleteFilesInDocument(Document document) {
        Log.info("DocumentService", "Entered deleteFilesInDocument method", null);
        document.getAttachments().forEach(att -> {
            try {
                deleteFileInAttachment(att);
            } catch (IOException | ErrorResponseException | XmlParserException | InternalException | ServerException
                    | NoSuchAlgorithmException | InsufficientDataException | InvalidResponseException
                    | InvalidKeyException e) {
                Log.error(e);
            }
        });
        Log.info("DocumentService", "Exited deleteFilesInDocument method", null);
    }

    /**
     * Finds the type {@link DocumentType} by id and sets the type in the document
     * entity {@link Document}.
     *
     * @param dto      a {@link DocumentCreateUpdateDTO}
     * @param document a {@link Document}
     */
    private void setType(@Valid DocumentCreateUpdateDTO dto, Document document) {

        Log.info("DocumentService", "Entered setType method", null);
        DocumentType documentType = typeDAO.findById(dto.getTypeId());
        if (Objects.isNull(documentType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    "Document type of id " + dto.getTypeId() + " does not exist.");
        }
        document.setType(documentType);
        Log.info("DocumentService", "Exited setType method", null);

    }

    /**
     * Finds the specification {@link DocumentSpecification} by id and sets the
     * specification
     * in the document entity {@link Document}.
     *
     * @param dto      a {@link DocumentCreateUpdateDTO}
     * @param document a {@link Document}
     */
    private void setSpecification(@Valid DocumentCreateUpdateDTO dto, Document document) {
        Log.info("DocumentService", "Entered setSpecification method", null);
        if (Objects.isNull(dto.getSpecification())) {
            document.setSpecification(null);
        } else {
            DocumentSpecificationCreateUpdateDTO docSpecDto = dto.getSpecification();
            DocumentSpecification documentSpecification = documentSpecificationMapper.map(docSpecDto);
            document.setSpecification(documentSpecification);
        }
        Log.info("DocumentService", "Exited setSpecification method", null);
    }

    /**
     * Finds the {@link SupportedMimeType} by the given id.
     *
     * @param dto a {@link AttachmentCreateUpdateDTO}
     * @return a {@link SupportedMimeType}
     *         or it throws an error if it can't find a {@link SupportedMimeType}
     *         given id.
     */
    private SupportedMimeType getSupportedMimeType(AttachmentCreateUpdateDTO dto) {
        Log.info("DocumentService", "Entered getSupportedMimeType method", null);
        SupportedMimeType mimeType = mimeTypeDAO.findById(dto.getMimeTypeId());
        if (Objects.isNull(mimeType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    "Supported mime type of id " + dto.getMimeTypeId() + " does not exist.");
        }
        Log.info("DocumentService", "Exited getSupportedMimeType method", null);
        return mimeType;
    }

    /**
     * Finds attachment's mimeType {@link SupportedMimeType} by id
     * and sets it in the attachment entity {@link Attachment}, then add
     * {@link Attachment}
     * in document entity {@link Document}.
     *
     * @param dto      a {@link DocumentCreateUpdateDTO}
     * @param document a {@link Document}
     */
    private void setAttachments(@Valid DocumentCreateUpdateDTO dto, Document document) {
        Log.info("DocumentService", "Entered setAttachments method", null);
        if (Objects.isNull(dto.getAttachments())) {
            document.setAttachments(null);
        } else {
            for (AttachmentCreateUpdateDTO attachmentDTO : dto.getAttachments()) {
                if (Objects.isNull(attachmentDTO.getId()) || attachmentDTO.getId().isEmpty()) {
                    SupportedMimeType mimeType = getSupportedMimeType(attachmentDTO);
                    Attachment attachment = documentMapper.mapAttachment(attachmentDTO);
                    attachment.setMimeType(mimeType);
                    attachment.setStorageUploadStatus(false);
                    document.getAttachments().add(attachment);
                }
            }
        }
        Log.info("DocumentService", "Exited setAttachments method", null);
    }

    /**
     * Updates {@link Channel} in {@link Document} or creates new {@link Channel}
     * and sets in {@link Document}.
     *
     * @param document  a {@link Document}
     * @param updateDTO a {@link DocumentCreateUpdateDTO}
     */
    private void updateChannelInDocument(Document document, DocumentCreateUpdateDTO updateDTO) {
        Log.info("DocumentService", "Entered updateChannelInDocument method", null);
        if (Objects.isNull(updateDTO.getChannel().getId()) || updateDTO.getChannel().getId().isEmpty()) {
            Channel channel = documentMapper.mapChannel(updateDTO.getChannel());
            document.setChannel(channel);
        } else {
            documentMapper.updateChannel(updateDTO.getChannel(), document.getChannel());
        }
        Log.info("DocumentService", "Exited updateChannelInDocument method", null);
    }

    /**
     * Updates {@link RelatedObjectRef} in {@link Document} or creates new
     * {@link RelatedObjectRef} and sets in {@link Document}.
     *
     * @param document  a {@link Document}
     * @param updateDTO a {@link DocumentCreateUpdateDTO}
     */
    private void updateRelatedObjectRefInDocument(Document document, DocumentCreateUpdateDTO updateDTO) {
        Log.info("DocumentService", "Entered updateRelatedObjectRefInDocument method", null);
        if (Objects.isNull(updateDTO.getRelatedObject())) {
            document.setRelatedObject(null);
        } else {
            if (Objects.isNull(updateDTO.getRelatedObject().getId())
                    || updateDTO.getRelatedObject().getId().isEmpty()) {
                RelatedObjectRef relatedObjectRef = documentMapper.mapRelatedObjectRef(updateDTO.getRelatedObject());
                document.setRelatedObject(relatedObjectRef);
            } else {
                documentMapper.updateRelatedObjectRef(updateDTO.getRelatedObject(), document.getRelatedObject());
            }
        }
        Log.info("DocumentService", "Exited updateRelatedObjectRefInDocument method", null);
    }

    /**
     * Updates {@link Attachment} in collection in {@link Document}
     * or creates {@link Attachment} sets {@link SupportedMimeType} in
     * {@link Attachment}
     * and add to collection or remove {@link Attachment} from collection.
     *
     * @param document  a {@link Document}
     * @param updateDTO a {@link DocumentCreateUpdateDTO}
     */
    private void updateAttachmentsInDocument(Document document, DocumentCreateUpdateDTO updateDTO) {
        Log.info("DocumentService", "Entered updateAttachmentsInDocument method", null);
        if (Objects.nonNull(updateDTO.getAttachments())) {
            for (Iterator<Attachment> i = document.getAttachments().iterator(); i.hasNext();) {
                Attachment entity = i.next();
                Optional<AttachmentCreateUpdateDTO> dtoOptional = updateDTO.getAttachments().stream()
                        .filter(dto -> dto.getId() != null)
                        .filter(dto -> entity.getId().equals(dto.getId()))
                        .findFirst();
                if (dtoOptional.isPresent() && !dtoOptional.isEmpty()) {
                    SupportedMimeType mimeType = getSupportedMimeType(dtoOptional.get());
                    documentMapper.updateAttachment(dtoOptional.get(), entity);
                    entity.setMimeType(mimeType);
                }
            }
            setAttachments(updateDTO, document);
        }
        Log.info("DocumentService", "Exited updateAttachmentsInDocument method", null);
    }

    private void uploadFileToObjectStorage(byte[] fileBytes, String id)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        Log.info("DocumentService", "Entered uploadFileToObjectStorage method", null);
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(prefix + bucket)
                .object(id)
                .stream(new ByteArrayInputStream(fileBytes), fileBytes.length, -1)
                .build());
        Log.info("DocumentService", "Exited uploadFileToObjectStorage method", null);

    }

    private void updateAttachmentAfterUpload(Attachment attachment, BigDecimal size, String contentType) {
        Log.info("DocumentService", "Entered updateAttachmentAfterUpload method", null);
        attachment.setSize(size);
        attachment.setSizeUnit(AttachmentUnit.BYTES);
        attachment.setStorage("MinIO");
        attachment.setType(contentType);
        attachment.setExternalStorageURL(url);
        attachment.setStorageUploadStatus(true);
        Log.info("DocumentService", "Exited updateAttachmentAfterUpload method", null);
    }

    private String getAttachmentNotFoundMsg(String id) {
        Log.info("DocumentService", "Entered getAttachmentNotFoundMsg method", null);
        Log.info("DocumentService", "Exited getAttachmentNotFoundMsg method", null);
        return "Attachment with id " + id + " was not found.";
    }

}
