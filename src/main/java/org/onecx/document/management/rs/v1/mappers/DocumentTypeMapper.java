package org.onecx.document.management.rs.v1.mappers;

import java.util.List;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.onecx.document.management.domain.models.entities.DocumentType;
import org.onecx.document.management.rs.v1.models.DocumentTypeCreateUpdateDTO;
import org.onecx.document.management.rs.v1.models.DocumentTypeDTO;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

@Mapper(componentModel = "cdi", uses = OffsetDateTimeMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface DocumentTypeMapper {

    DocumentType map(DocumentTypeCreateUpdateDTO createUpdateDTO);

    DocumentTypeDTO mapDocumentType(DocumentType documentType);

    void update(DocumentTypeCreateUpdateDTO dto, @MappingTarget DocumentType documentType);

    List<DocumentTypeDTO> findAllDocumentType(List<DocumentType> documents);
}
