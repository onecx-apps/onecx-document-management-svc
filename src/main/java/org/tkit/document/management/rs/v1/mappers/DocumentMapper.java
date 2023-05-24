package org.tkit.document.management.rs.v1.mappers;

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
import org.tkit.document.management.domain.criteria.DocumentSearchCriteria;
import org.tkit.document.management.domain.models.entities.Attachment;
import org.tkit.document.management.domain.models.entities.Category;
import org.tkit.document.management.domain.models.entities.Channel;
import org.tkit.document.management.domain.models.entities.Document;
import org.tkit.document.management.domain.models.entities.DocumentCharacteristic;
import org.tkit.document.management.domain.models.entities.DocumentRelationship;
import org.tkit.document.management.domain.models.entities.RelatedObjectRef;
import org.tkit.document.management.domain.models.entities.RelatedPartyRef;
import org.tkit.document.management.rs.v1.models.AttachmentCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.AttachmentDTO;
import org.tkit.document.management.rs.v1.models.CategoryCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.ChannelCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.ChannelDTO;
import org.tkit.document.management.rs.v1.models.DocumentCharacteristicCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.DocumentCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.DocumentDetailDTO;
import org.tkit.document.management.rs.v1.models.DocumentRelationshipCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.DocumentSearchCriteriaDTO;
import org.tkit.document.management.rs.v1.models.IdentifiableTraceableDTO;
import org.tkit.document.management.rs.v1.models.RelatedObjectRefCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.RelatedPartyRefCreateUpdateDTO;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.models.TraceableEntity;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;
import org.tkit.quarkus.rs.models.PageResultDTO;

@Mapper(componentModel = "cdi", uses = OffsetDateTimeMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface DocumentMapper {

    DocumentDetailDTO mapDetail(Document document);

    Stream<DocumentDetailDTO> mapDetailBulk(Stream<Document> document);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "specification", ignore = true)
    @Mapping(target = "attachments", ignore = true)
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
    Document update(DocumentCreateUpdateDTO dto, @MappingTarget Document document);

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
    DocumentRelationship updateDocumentRelationship(DocumentRelationshipCreateUpdateDTO dto,
            @MappingTarget DocumentRelationship entity);

    @Mapping(target = "id", ignore = true)
    DocumentRelationship mapDocumentRelationship(DocumentRelationshipCreateUpdateDTO dto);

    @Mapping(target = "id", ignore = true)
    DocumentCharacteristic updateDocumentCharacteristic(DocumentCharacteristicCreateUpdateDTO dto,
            @MappingTarget DocumentCharacteristic entity);

    @Mapping(target = "id", ignore = true)
    DocumentCharacteristic mapDocumentCharacteristic(DocumentCharacteristicCreateUpdateDTO dto);

    @Mapping(target = "id", ignore = true)
    RelatedPartyRef updateRelatedPartyRef(RelatedPartyRefCreateUpdateDTO dto, @MappingTarget RelatedPartyRef entity);

    @Mapping(target = "id", ignore = true)
    RelatedPartyRef mapRelatedPartyRef(RelatedPartyRefCreateUpdateDTO dto);

    @Mapping(target = "id", ignore = true)
    Category updateCategory(CategoryCreateUpdateDTO dto, @MappingTarget Category entity);

    @Mapping(target = "id", ignore = true)
    Category mapCategory(CategoryCreateUpdateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mimeType", ignore = true)
    Attachment updateAttachment(AttachmentCreateUpdateDTO dto, @MappingTarget Attachment entity);

    @Mapping(target = "id", ignore = true)
    Attachment mapAttachment(AttachmentCreateUpdateDTO dto);

    AttachmentDTO mapAttachment(Attachment attachment);

    @Mapping(target = "id", ignore = true)
    Channel updateChannel(ChannelCreateUpdateDTO dto, @MappingTarget Channel entity);

    @Mapping(target = "id", ignore = true)
    Channel mapChannel(ChannelCreateUpdateDTO dto);

    List<ChannelDTO> mapChannels(List<Channel> channel);

    @Mapping(target = "id", ignore = true)
    void updateRelatedObjectRef(RelatedObjectRefCreateUpdateDTO dto, @MappingTarget RelatedObjectRef entity);

    @Mapping(target = "id", ignore = true)
    RelatedObjectRef mapRelatedObjectRef(RelatedObjectRefCreateUpdateDTO dto);

    default void updateTraceableCollectionsInDocument(Document document, DocumentCreateUpdateDTO updateDTO) {
        updateTraceableCollection(document.getDocumentRelationships(),
                updateDTO.getDocumentRelationships(),
                this::updateDocumentRelationship,
                this::mapDocumentRelationship);
        updateTraceableCollection(document.getCharacteristics(),
                updateDTO.getCharacteristics(),
                this::updateDocumentCharacteristic,
                this::mapDocumentCharacteristic);
        updateTraceableCollection(document.getRelatedParties(),
                updateDTO.getRelatedParties(),
                this::updateRelatedPartyRef,
                this::mapRelatedPartyRef);
        updateTraceableCollection(document.getCategories(),
                updateDTO.getCategories(),
                this::updateCategory, this::mapCategory);
    }

    /**
     * Updates collection of objects extends {@link TraceableEntity} in
     * {@link Document}
     * or creates new object extends {@link TraceableEntity} and add to collection
     * or remove object extends {@link TraceableEntity} from collection.
     *
     * @param collection of objects extends {@link TraceableEntity}
     * @param collectionDTO of objects extends {@link IdentifiableTraceableDTO}
     * @param updateFunction update entity.
     * @param mapFunction map from DTO to entity.
     * @param <T> extends {@link TraceableEntity}
     * @param <S> extends {@link IdentifiableTraceableDTO}
     */
    default <T extends TraceableEntity, S extends IdentifiableTraceableDTO> void updateTraceableCollection(
            Set<T> collection,
            Set<S> collectionDTO,
            BiFunction<S, T, T> updateFunction,
            Function<S, T> mapFunction) {
        if (collectionDTO != null) {
            for (Iterator<T> i = collection.iterator(); i.hasNext();) {
                T entity = i.next();
                Optional<S> dtoOptional = collectionDTO.stream()
                        .filter(dto -> dto.getId() != null)
                        .filter(dto -> entity.getId().equals(dto.getId()))
                        .findFirst();
                if (dtoOptional.isEmpty()) {
                    i.remove();
                } else {
                    updateFunction.apply(dtoOptional.get(), entity);
                }
            }
            for (S dto : collectionDTO) {
                if (dto.getId() == null || dto.getId().isEmpty()) {
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
                        this::updateDocumentRelationship,
                        this::mapDocumentRelationship);
                updateTraceableCollection(documentbulk.getCharacteristics(),
                        updatebulk.getCharacteristics(),
                        this::updateDocumentCharacteristic,
                        this::mapDocumentCharacteristic);
                updateTraceableCollection(documentbulk.getRelatedParties(),
                        updatebulk.getRelatedParties(),
                        this::updateRelatedPartyRef,
                        this::mapRelatedPartyRef);
                updateTraceableCollection(documentbulk.getCategories(),
                        updatebulk.getCategories(),
                        this::updateCategory, this::mapCategory);
            }
        }
    }

    List<DocumentDetailDTO> mapDocuments(List<Document> documents);
}
