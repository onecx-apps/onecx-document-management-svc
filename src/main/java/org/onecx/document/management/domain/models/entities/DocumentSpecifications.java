package org.onecx.document.management.domain.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The DocumentSpecification entity.
 */
@Getter
@Setter
@Entity
@Table(name = "DM_DOCUMENT_SPECIFICATION")
public class DocumentSpecifications extends TraceableEntity {
    /**
     * Name of the document specification.
     */
    @Column(name = "NAME")
    private String name;
    /**
     * Service specification version.
     */
    @Column(name = "VERSION")
    private String specificationVersion;

}
