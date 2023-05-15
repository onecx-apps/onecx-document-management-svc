package org.tkit.document.management.rs.v1.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
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
import org.tkit.document.management.domain.daos.*;
import org.tkit.document.management.domain.models.entities.*;
import org.tkit.document.management.domain.models.enums.AttachmentUnit;
import org.tkit.document.management.rs.v1.mappers.DocumentMapper;
import org.tkit.document.management.rs.v1.mappers.DocumentSpecificationMapper;
import org.tkit.document.management.rs.v1.models.AttachmentCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.DocumentCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.DocumentSpecificationCreateUpdateDTO;
import org.tkit.quarkus.rs.exceptions.RestException;

import io.minio.*;
import io.minio.errors.*;
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
        Document document = documentMapper.map(dto);
        setType(dto, document);
        setSpecification(dto, document);
        setAttachments(dto, document);
        return documentDAO.create(document);
    }

    private String extractFileName(InputPart inputPart) {
        MultivaluedMap<String, String> headers = inputPart.getHeaders();
        Matcher matcher = FILENAME_PATTERN.matcher(headers.getFirst(HEADER_KEY));
        String filename = null;
        if (matcher.find()) {
            filename = matcher.group(1);
        }
        return filename;
    }

    @Transactional
    public HashMap<String, Integer> uploadAttachment(String documentId, MultipartFormDataInput input) throws IOException {
        HashMap<String, Integer> map = new HashMap<>();
        Set<Attachment> newAttachmentSet = new HashSet<>();
        Document document = documentDAO.findDocumentById(documentId);
        if (document == null) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND);
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
            if (attachmentIdList.isEmpty()) {
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
        if (!newAttachmentSet.isEmpty()) {
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
                            log.error("Error Message: ", e);
                        }
                    });
        } else {
            return map;
        }
        return map;
    }

    /**
     * Updates the basic fields in {@link Document}, updates collections and elements in collections: {@link Attachment},
     * {@link DocumentCharacteristic}, {@link DocumentRelationship}, {@link RelatedPartyRef}, {@link Category},
     * updates objects: {@link Channel}, {@link RelatedObjectRef} and sets {@link DocumentType}
     * and {@link DocumentSpecification}.
     *
     * @param dto a {@link DocumentCreateUpdateDTO}
     * @return a {@link Document}
     */
    @Transactional
    public Document updateDocument(@Valid Document document, DocumentCreateUpdateDTO dto) {
        documentMapper.update(dto, document);
        setType(dto, document);
        setSpecification(dto, document);
        updateChannelInDocument(document, dto);
        updateRelatedObjectRefInDocument(document, dto);
        documentMapper.updateTraceableCollectionsInDocument(document, dto);
        updateAttachmentsInDocument(document, dto);
        return document;
    }

    public InputStream getObjectFromObjectStore(String objectId)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(prefix + bucket)
                .object(objectId)
                .build();
        return minioClient.getObject(getObjectArgs);
    }

    @Transactional
    public void deleteFileInAttachment(Attachment attachment)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
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
    }

    @Transactional
    public void deleteFilesInDocument(Document document) {
        document.getAttachments().forEach(att -> {
            try {
                deleteFileInAttachment(att);
            } catch (IOException | ErrorResponseException | XmlParserException | InternalException | ServerException
                    | NoSuchAlgorithmException | InsufficientDataException | InvalidResponseException | InvalidKeyException e) {
                Log.error(e);
            }
        });
    }

    /**
     * Finds the type {@link DocumentType} by id and sets the type in the document entity {@link Document}.
     *
     * @param dto a {@link DocumentCreateUpdateDTO}
     * @param document a {@link Document}
     */
    private void setType(DocumentCreateUpdateDTO dto, Document document) {
        DocumentType documentType = typeDAO.findById(dto.getTypeId());
        if (Objects.isNull(documentType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    "Document type of id " + dto.getTypeId() + " does not exist.");
        }
        document.setType(documentType);
    }

    /**
     * Finds the specification {@link DocumentSpecification} by id and sets the specification
     * in the document entity {@link Document}.
     *
     * @param dto a {@link DocumentCreateUpdateDTO}
     * @param document a {@link Document}
     */
    private void setSpecification(DocumentCreateUpdateDTO dto, Document document) {
        if (Objects.isNull(dto.getSpecification())) {
            document.setSpecification(null);
        } else {
            DocumentSpecificationCreateUpdateDTO docSpecDto = dto.getSpecification();
            DocumentSpecification documentSpecification = documentSpecificationMapper.map(docSpecDto);
            document.setSpecification(documentSpecification);
        }
    }

    /**
     * Finds the {@link SupportedMimeType} by the given id.
     *
     * @param dto a {@link AttachmentCreateUpdateDTO}
     * @return a {@link SupportedMimeType}
     *         or it throws an error if it can't find a {@link SupportedMimeType} given id.
     */
    private SupportedMimeType getSupportedMimeType(AttachmentCreateUpdateDTO dto) {
        SupportedMimeType mimeType = mimeTypeDAO.findById(dto.getMimeTypeId());
        if (Objects.isNull(mimeType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    "Supported mime type of id " + dto.getMimeTypeId() + " does not exist.");
        }
        return mimeType;
    }

    /**
     * Finds attachment's mimeType {@link SupportedMimeType} by id
     * and sets it in the attachment entity {@link Attachment}, then add {@link Attachment}
     * in document entity {@link Document}.
     *
     * @param dto a {@link DocumentCreateUpdateDTO}
     * @param document a {@link Document}
     */
    private void setAttachments(DocumentCreateUpdateDTO dto, Document document) {
        if (Objects.isNull(dto.getAttachments())) {
            document.setAttachments(null);
        } else {
            for (AttachmentCreateUpdateDTO attachmentDTO : dto.getAttachments()) {
                if (attachmentDTO.getId() == null || attachmentDTO.getId().isEmpty()) {
                    SupportedMimeType mimeType = getSupportedMimeType(attachmentDTO);
                    Attachment attachment = documentMapper.mapAttachment(attachmentDTO);
                    attachment.setMimeType(mimeType);
                    document.getAttachments().add(attachment);
                }
            }
        }
    }

    /**
     * Updates {@link Channel} in {@link Document} or creates new {@link Channel} and sets in {@link Document}.
     *
     * @param document a {@link Document}
     * @param updateDTO a {@link DocumentCreateUpdateDTO}
     */
    private void updateChannelInDocument(Document document, DocumentCreateUpdateDTO updateDTO) {
        if (updateDTO.getChannel().getId() == null || updateDTO.getChannel().getId().isEmpty()) {
            Channel channel = documentMapper.mapChannel(updateDTO.getChannel());
            document.setChannel(channel);
        } else {
            documentMapper.updateChannel(updateDTO.getChannel(), document.getChannel());
        }
    }

    /**
     * Updates {@link RelatedObjectRef} in {@link Document} or creates new {@link RelatedObjectRef} and sets in
     * {@link Document}.
     *
     * @param document a {@link Document}
     * @param updateDTO a {@link DocumentCreateUpdateDTO}
     */
    private void updateRelatedObjectRefInDocument(Document document, DocumentCreateUpdateDTO updateDTO) {
        if (updateDTO.getRelatedObject() == null) {
            document.setRelatedObject(null);
        } else {
            if (updateDTO.getRelatedObject().getId() == null || updateDTO.getRelatedObject().getId().isEmpty()) {
                RelatedObjectRef relatedObjectRef = documentMapper.mapRelatedObjectRef(updateDTO.getRelatedObject());
                document.setRelatedObject(relatedObjectRef);
            } else {
                documentMapper.updateRelatedObjectRef(updateDTO.getRelatedObject(), document.getRelatedObject());
            }
        }
    }

    /**
     * Updates {@link Attachment} in collection in {@link Document}
     * or creates {@link Attachment} sets {@link SupportedMimeType} in {@link Attachment}
     * and add to collection or remove {@link Attachment} from collection.
     *
     * @param document a {@link Document}
     * @param updateDTO a {@link DocumentCreateUpdateDTO}
     */
    private void updateAttachmentsInDocument(Document document, DocumentCreateUpdateDTO updateDTO) {
        if (Objects.nonNull(updateDTO.getAttachments())) {
            for (Iterator<Attachment> i = document.getAttachments().iterator(); i.hasNext();) {
                Attachment entity = i.next();
                Optional<AttachmentCreateUpdateDTO> dtoOptional = updateDTO.getAttachments().stream()
                        .filter(dto -> dto.getId() != null)
                        .filter(dto -> entity.getId().equals(dto.getId()))
                        .findFirst();
                if (dtoOptional.isPresent()) {
                    SupportedMimeType mimeType = getSupportedMimeType(dtoOptional.get());
                    documentMapper.updateAttachment(dtoOptional.get(), entity);
                    entity.setMimeType(mimeType);
                }
            }
            setAttachments(updateDTO, document);
        }
    }

    private void uploadFileToObjectStorage(byte[] fileBytes, String id)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(prefix + bucket)
                .object(id)
                .stream(new ByteArrayInputStream(fileBytes), fileBytes.length, -1)
                .build());
    }

    private void updateAttachmentAfterUpload(Attachment attachment, BigDecimal size, String contentType) {
        attachment.setSize(size);
        attachment.setSizeUnit(AttachmentUnit.BYTES);
        attachment.setStorage("MinIO");
        attachment.setType(contentType);
        attachment.setExternalStorageURL(url);
    }

}
