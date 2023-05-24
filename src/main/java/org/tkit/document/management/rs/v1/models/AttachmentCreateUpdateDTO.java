package org.tkit.document.management.rs.v1.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Class used to create or update attachment.
 */
@Getter
@Setter
public class AttachmentCreateUpdateDTO implements IdentifiableTraceableDTO {

    private String id;

    private String name;

    private String description;

    private String type;

    private TimePeriodDTO validFor;

    private String mimeTypeId;

    private String file;

    private String fileName;
}

