package org.tkit.document.management.rs.v1.models;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tkit.document.management.domain.models.enums.LifeCycleState;
import org.tkit.quarkus.rs.models.TraceableDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentDetailDTO extends TraceableDTO {

    private String name;

    private String description;

    private LifeCycleState lifeCycleState;

    private String documentVersion;

    private ChannelDTO channel;

    private Set<String> tags = new HashSet<>();

    private DocumentTypeDTO type;

    private DocumentSpecificationDTO specification;

    private Set<DocumentRelationshipDTO> documentRelationships;

    private Set<DocumentCharacteristicDTO> characteristics;

    private Set<RelatedPartyRefDTO> relatedParties;

    private RelatedObjectRefDTO relatedObject;

    private Set<CategoryDTO> categories;

    private List<AttachmentDTO> attachments;
}
