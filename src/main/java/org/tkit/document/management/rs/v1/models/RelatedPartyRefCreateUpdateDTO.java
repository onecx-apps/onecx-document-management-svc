package org.tkit.document.management.rs.v1.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Class used to create or update related party reference.
 */
@Getter
@Setter
public class RelatedPartyRefCreateUpdateDTO implements IdentifiableTraceableDTO {

    private String id;

    private String name;

    private String role;

    private TimePeriodDTO validFor;
}
