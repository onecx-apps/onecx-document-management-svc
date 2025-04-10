package org.onecx.document.management.rs.v1.mappers;

import java.util.List;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.onecx.document.management.domain.models.entities.SupportedMimeTypes;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.onecx.document.management.rs.v1.model.SupportedMimeType;
import gen.org.onecx.document.management.rs.v1.model.SupportedMimeTypeCreateUpdate;

@Mapper(componentModel = "cdi", uses = OffsetDateTimeMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface SupportedMimeTypeMapper {

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    SupportedMimeTypes map(SupportedMimeTypeCreateUpdate dto);

    SupportedMimeType mapToDTO(SupportedMimeTypes supportedMimeType);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    void update(SupportedMimeTypeCreateUpdate dto, @MappingTarget SupportedMimeTypes supportedMimeType);

    List<SupportedMimeType> findAllSupportedMimeTypes(List<SupportedMimeTypes> supportedMimeTypes);
}
