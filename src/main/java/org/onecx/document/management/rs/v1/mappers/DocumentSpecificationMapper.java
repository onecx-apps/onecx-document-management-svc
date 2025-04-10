package org.onecx.document.management.rs.v1.mappers;

import java.util.List;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.onecx.document.management.domain.models.entities.DocumentSpecifications;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.onecx.document.management.rs.v1.model.DocumentSpecification;
import gen.org.onecx.document.management.rs.v1.model.DocumentSpecificationCreateUpdate;

@Mapper(componentModel = "cdi", uses = OffsetDateTimeMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface DocumentSpecificationMapper {
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    DocumentSpecifications map(DocumentSpecificationCreateUpdate dto);

    DocumentSpecification mapToDTO(DocumentSpecifications documentSpecifications);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    void update(DocumentSpecificationCreateUpdate dto, @MappingTarget DocumentSpecifications documentSpecifications);

    List<DocumentSpecification> findAllDocumentSpecifications(List<DocumentSpecifications> documentSpecifications);
}
