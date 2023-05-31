package org.tkit.document.management.domain.models.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The DocumentRelationship entity.
 */
@Getter
@Setter
@Entity
@Table(name = "DM_DOCUMENT_RELATIONSHIP")
@java.lang.SuppressWarnings("java:S2160")
public class DocumentRelationship extends TraceableEntity {
    /**
     * Type of the relationship.
     */
    @Column(name = "TYPE")
    private String type;
    /**
     * Id of the referenced document.
     */
    @Column(name = "DOCUMENT_REF_ID")
    private String documentRefId;

}
