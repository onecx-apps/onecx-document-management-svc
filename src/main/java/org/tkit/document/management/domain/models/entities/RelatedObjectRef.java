package org.tkit.document.management.domain.models.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The RelatedObject entity.
 */
@Getter
@Setter
@Entity
@Table(name = "DM_RELATED_OBJECT")
@java.lang.SuppressWarnings("java:S2160")
public class RelatedObjectRef extends TraceableEntity {
    /**
     * Describes the involvement to the related object.
     */
    @Column(name = "INVOLVEMENT")
    private String involvement;
    /**
     * Type of the related object .
     */
    @Column(name = "RO_TYPE")
    private String objectReferenceType;
    /**
     * Id of the related object.
     */
    @Column(name = "RO_ID")
    private String objectReferenceId;

}
