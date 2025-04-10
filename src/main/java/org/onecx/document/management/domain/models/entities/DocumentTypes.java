package org.onecx.document.management.domain.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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
public class DocumentTypes extends TraceableEntity {
    /**
     * Name of the document type.
     */
    @Column(name = "NAME")
    private String name;

    /**
     * Description of the document type
     */
    @Column(name = "DESCRIPTION")
    private String description;

    /**
     * Document type active status
     */
    @Column(name = "ACTIVE_STATUS")
    private Boolean activeStatus;
}
