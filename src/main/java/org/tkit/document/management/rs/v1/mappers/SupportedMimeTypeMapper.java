package org.tkit.document.management.rs.v1.mappers;

import java.util.List;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.tkit.document.management.domain.models.entities.SupportedMimeType;
import org.tkit.document.management.rs.v1.models.SupportedMimeTypeCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.SupportedMimeTypeDTO;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

@Mapper(componentModel = "cdi", uses = OffsetDateTimeMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface SupportedMimeTypeMapper {

    SupportedMimeType map(SupportedMimeTypeCreateUpdateDTO dto);

    SupportedMimeTypeDTO mapToDTO(SupportedMimeType supportedMimeType);

    void update(SupportedMimeTypeCreateUpdateDTO dto, @MappingTarget SupportedMimeType supportedMimeType);

    List<SupportedMimeTypeDTO> findAllSupportedMimeTypes(List<SupportedMimeType> supportedMimeTypes);
}
