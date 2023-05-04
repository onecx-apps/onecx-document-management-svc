package org.tkit.document.management.rs.v1.mappers;

import java.util.List;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.tkit.document.management.domain.models.entities.DocumentSpecification;
import org.tkit.document.management.rs.v1.models.DocumentSpecificationCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.DocumentSpecificationDTO;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

@Mapper(componentModel = "cdi", uses = OffsetDateTimeMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface DocumentSpecificationMapper {

    DocumentSpecification map(DocumentSpecificationCreateUpdateDTO dto);

    DocumentSpecificationDTO mapToDTO(DocumentSpecification documentSpecification);

    void update(DocumentSpecificationCreateUpdateDTO dto, @MappingTarget DocumentSpecification documentSpecification);

    List<DocumentSpecificationDTO> findAllDocumentSpecifications(List<DocumentSpecification> documentSpecifications);
}
