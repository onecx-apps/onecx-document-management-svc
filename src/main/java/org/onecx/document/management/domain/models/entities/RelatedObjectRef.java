package org.onecx.document.management.domain.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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
