package org.onecx.document.management.rs.v1.mappers;

import java.util.List;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.onecx.document.management.domain.models.entities.DocumentTypes;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.onecx.document.management.rs.v1.model.DocumentType;
import gen.org.onecx.document.management.rs.v1.model.DocumentTypeCreateUpdate;

@Mapper(componentModel = "cdi", uses = OffsetDateTimeMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface DocumentTypeMapper {
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "activeStatus", ignore = true)
    DocumentTypes map(DocumentTypeCreateUpdate createUpdateDTO);

    DocumentType mapDocumentType(DocumentTypes documentTypes);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "activeStatus", ignore = true)
    void update(DocumentTypeCreateUpdate dto, @MappingTarget DocumentTypes documentTypes);

    List<DocumentType> findAllDocumentType(List<DocumentTypes> documents);
}
