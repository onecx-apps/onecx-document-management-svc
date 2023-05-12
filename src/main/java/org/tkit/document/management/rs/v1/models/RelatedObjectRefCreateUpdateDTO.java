package org.tkit.document.management.rs.v1.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Class used to create or update related object reference.
 */
@Getter
@Setter
public class RelatedObjectRefCreateUpdateDTO implements IdentifiableTraceableDTO {

    private String id;

    private String involvement;

    private String objectReferenceType;

    private String objectReferenceId;
}
