package org.tkit.document.management.rs.v1.models;

import org.tkit.quarkus.rs.models.TraceableDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@java.lang.SuppressWarnings("java:S2160")
public class RelatedObjectRefDTO extends TraceableDTO {

    private String involvement;

    private String objectReferenceType;

    private String objectReferenceId;
}
