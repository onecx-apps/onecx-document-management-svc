package org.onecx.document.management.rs.v1.mappers;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.onecx.document.management.domain.criteria.DocumentSearchCriteria;
import org.onecx.document.management.domain.models.entities.Attachment;
import org.onecx.document.management.domain.models.entities.Category;
import org.onecx.document.management.domain.models.entities.Channel;
import org.onecx.document.management.domain.models.entities.Document;
import org.onecx.document.management.domain.models.entities.DocumentCharacteristic;
import org.onecx.document.management.domain.models.entities.DocumentRelationship;
import org.onecx.document.management.domain.models.entities.RelatedObjectRef;
import org.onecx.document.management.domain.models.entities.RelatedPartyRef;
import org.onecx.document.management.domain.models.entities.StorageUploadAudit;
import org.onecx.document.management.rs.v1.models.PageResultDTO;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.onecx.document.management.rs.v1.model.*;

@Mapper(componentModel = "cdi", uses = OffsetDateTimeMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface DocumentMapper {
    @Mapping(target = "removeTagsItem", ignore = true)
    @Mapping(target = "removeDocumentRelationshipsItem", ignore = true)
    @Mapping(target = "removeCharacteristicsItem", ignore = true)
    @Mapping(target = "removeRelatedPartiesItem", ignore = true)
    @Mapping(target = "removeCategoriesItem", ignore = true)
    @Mapping(target = "removeAttachmentsItem", ignore = true)
    DocumentDetailDTO mapDetail(Document document);

    List<StorageUploadAuditDTO> mapStorageUploadAudit(List<StorageUploadAudit> audit);

    Stream<DocumentDetailDTO> mapDetailBulk(Stream<Document> document);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "specification", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    Document map(DocumentCreateUpdateDTO createUpdateDTO);

    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    DocumentSearchCriteria map(DocumentSearchCriteriaDTO searchCriteriaDTO);

    PageResultDTO<DocumentDetailDTO> mapToPageResultDTO(PageResult<Document> page);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "specification", ignore = true)
    @Mapping(target = "channel", ignore = true)
    @Mapping(target = "relatedObject", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "documentRelationships", ignore = true)
    @Mapping(target = "characteristics", ignore = true)
    @Mapping(target = "relatedParties", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    Document update(DocumentCreateUpdateDTO dto,
            @MappingTarget Document document);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "specification", ignore = true)
    @Mapping(target = "channel", ignore = true)
    @Mapping(target = "relatedObject", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "documentRelationships", ignore = true)
    @Mapping(target = "characteristics", ignore = true)
    @Mapping(target = "relatedParties", ignore = true)
    @Mapping(target = "categories", ignore = true)
    List<Document> updateBulk(List<DocumentCreateUpdateDTO> dto, @MappingTarget List<Document> document);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    DocumentRelationship updateDocumentRelationship(DocumentRelationshipCreateUpdateDTO dto,
            @MappingTarget DocumentRelationship entity);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    DocumentRelationship mapDocumentRelationship(DocumentRelationshipCreateUpdateDTO dto);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    DocumentCharacteristic updateDocumentCharacteristic(DocumentCharacteristicCreateUpdateDTO dto,
            @MappingTarget DocumentCharacteristic entity);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    DocumentCharacteristic mapDocumentCharacteristic(DocumentCharacteristicCreateUpdateDTO dto);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    RelatedPartyRef updateRelatedPartyRef(RelatedPartyRefCreateUpdateDTO dto, @MappingTarget RelatedPartyRef entity);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    RelatedPartyRef mapRelatedPartyRef(RelatedPartyRefCreateUpdateDTO dto);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    Category updateCategory(CategoryCreateUpdateDTO dto, @MappingTarget Category entity);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    Category mapCategory(CategoryCreateUpdateDTO dto);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mimeType", ignore = true)
    @Mapping(target = "size", ignore = true)
    @Mapping(target = "sizeUnit", ignore = true)
    @Mapping(target = "storage", ignore = true)
    @Mapping(target = "externalStorageURL", ignore = true)
    @Mapping(target = "storageUploadStatus", ignore = true)
    Attachment updateAttachment(AttachmentCreateUpdateDTO dto, @MappingTarget Attachment entity);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "size", ignore = true)
    @Mapping(target = "sizeUnit", ignore = true)
    @Mapping(target = "storage", ignore = true)
    @Mapping(target = "externalStorageURL", ignore = true)
    @Mapping(target = "storageUploadStatus", ignore = true)
    @Mapping(target = "mimeType", ignore = true)
    Attachment mapAttachment(AttachmentCreateUpdateDTO dto);

    AttachmentDTO mapAttachment(Attachment attachment);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    Channel updateChannel(ChannelCreateUpdateDTO dto,
            @MappingTarget Channel entity);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    Channel mapChannel(ChannelCreateUpdateDTO dto);

    List<ChannelDTO> mapChannels(List<Channel> channel);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateRelatedObjectRef(RelatedObjectRefCreateUpdateDTO dto,
            @MappingTarget RelatedObjectRef entity);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    RelatedObjectRef mapRelatedObjectRef(RelatedObjectRefCreateUpdateDTO dto);

    default void updateTraceableCollectionsInDocument(Document document,
            DocumentCreateUpdateDTO updateDTO) {
        updateTraceableCollection(document.getDocumentRelationships(),
                updateDTO.getDocumentRelationships(),
                DocumentRelationship::getId,
                DocumentRelationshipCreateUpdateDTO::getId,
                this::updateDocumentRelationship,
                this::mapDocumentRelationship);
        updateTraceableCollection(document.getCharacteristics(),
                updateDTO.getCharacteristics(),
                DocumentCharacteristic::getId,
                DocumentCharacteristicCreateUpdateDTO::getId,
                this::updateDocumentCharacteristic,
                this::mapDocumentCharacteristic);
        updateTraceableCollection(document.getRelatedParties(),
                updateDTO.getRelatedParties(),
                RelatedPartyRef::getId,
                RelatedPartyRefCreateUpdateDTO::getId,
                this::updateRelatedPartyRef,
                this::mapRelatedPartyRef);
        updateTraceableCollection(document.getCategories(),
                updateDTO.getCategories(),
                Category::getId,
                CategoryCreateUpdateDTO::getId,
                this::updateCategory, this::mapCategory);
    }

    /**
     * Updates a collection of objects based on their IDs.
     * - Updates existing objects if they match by ID
     * - Removes objects that don't exist in the new collection
     * - Adds new objects that don't have IDs
     *
     * @param collection The existing collection to update
     * @param newItems New items to use for updating
     * @param getId Function to get ID from an entity
     * @param getDtoId Function to get ID from a DTO
     * @param updateFunction Function to update existing entity with DTO data
     * @param mapFunction Function to create new entity from DTO
     * @param <T> Entity type
     * @param <S> DTO type
     * @param <I> ID type
     */
    default <T, S, I> void updateTraceableCollection(
            Set<T> collection,
            Set<S> newItems,
            Function<T, I> getId,
            Function<S, I> getDtoId,
            BiFunction<S, T, T> updateFunction,
            Function<S, T> mapFunction) {
        if (newItems != null) {
            // Remove or update existing items
            for (Iterator<T> i = collection.iterator(); i.hasNext();) {
                T entity = i.next();
                I entityId = getId.apply(entity);
                Optional<S> dtoOptional = newItems.stream()
                        .filter(dto -> getDtoId.apply(dto) != null)
                        .filter(dto -> entityId.equals(getDtoId.apply(dto)))
                        .findFirst();
                if (dtoOptional.isEmpty()) {
                    i.remove();
                } else {
                    updateFunction.apply(dtoOptional.get(), entity);
                }
            }
            for (S dto : newItems) {
                if (getDtoId.apply(dto) == null) {
                    T entity = mapFunction.apply(dto);
                    collection.add(entity);
                }
            }
        }
    }

    default void updateBulkTraceableCollectionsInDocument(List<Document> document,
            List<DocumentCreateUpdateDTO> updateDTO) {
        for (Document documentbulk : document) {
            for (DocumentCreateUpdateDTO updatebulk : updateDTO) {
                updateTraceableCollection(documentbulk.getDocumentRelationships(),
                        updatebulk.getDocumentRelationships(),
                        DocumentRelationship::getId,
                        DocumentRelationshipCreateUpdateDTO::getId,
                        this::updateDocumentRelationship,
                        this::mapDocumentRelationship);
                updateTraceableCollection(documentbulk.getCharacteristics(),
                        updatebulk.getCharacteristics(),
                        DocumentCharacteristic::getId,
                        DocumentCharacteristicCreateUpdateDTO::getId,
                        this::updateDocumentCharacteristic,
                        this::mapDocumentCharacteristic);
                updateTraceableCollection(documentbulk.getRelatedParties(),
                        updatebulk.getRelatedParties(),
                        RelatedPartyRef::getId,
                        RelatedPartyRefCreateUpdateDTO::getId,
                        this::updateRelatedPartyRef,
                        this::mapRelatedPartyRef);
                updateTraceableCollection(documentbulk.getCategories(),
                        updatebulk.getCategories(),
                        Category::getId,
                        CategoryCreateUpdateDTO::getId,
                        this::updateCategory, this::mapCategory);
            }
        }
    }

    List<DocumentDetailDTO> mapDocuments(List<Document> documents);
}
