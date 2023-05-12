package org.tkit.document.management.rs.v1.models;

import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.tkit.document.management.domain.models.enums.LifeCycleState;

import lombok.Getter;
import lombok.Setter;

/**
 * Class used to create or update document.
 */
@Getter
@Setter
public class DocumentCreateUpdateDTO {

    private String id;

    @NotBlank
    private String name;

    private String description;

    private LifeCycleState lifeCycleState;

    private String documentVersion;

    private Set<String> tags;

    @NotNull
    private String typeId;

    private DocumentSpecificationCreateUpdateDTO specification;

    @NotNull
    private ChannelCreateUpdateDTO channel;

    private Set<DocumentRelationshipCreateUpdateDTO> documentRelationships;

    private Set<DocumentCharacteristicCreateUpdateDTO> characteristics;

    private Set<RelatedPartyRefCreateUpdateDTO> relatedParties;

    private RelatedObjectRefCreateUpdateDTO relatedObject;

    private Set<CategoryCreateUpdateDTO> categories;

    private List<AttachmentCreateUpdateDTO> attachments;
}
