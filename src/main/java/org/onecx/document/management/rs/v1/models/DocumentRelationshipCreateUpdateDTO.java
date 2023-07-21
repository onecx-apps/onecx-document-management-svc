package org.onecx.document.management.rs.v1.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Class used to create or update document relationship.
 */
@Getter
@Setter
public class DocumentRelationshipCreateUpdateDTO implements IdentifiableTraceableDTO {

    private String id;

    private String type;

    private String documentRefId;
}
