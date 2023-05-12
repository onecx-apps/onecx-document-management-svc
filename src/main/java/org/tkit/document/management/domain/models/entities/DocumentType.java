package org.tkit.document.management.domain.models.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The DocumentType entity.
 */
@Getter
@Setter
@Entity
@Table(name = "DM_DOCUMENT_TYPE")
@java.lang.SuppressWarnings("java:S2160")
public class DocumentType extends TraceableEntity {
    /**
     * Name of the document type.
     */
    @Column(name = "NAME")
    private String name;
}
