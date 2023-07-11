package org.tkit.document.management.domain.models.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

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
public class DocumentSpecification extends TraceableEntity {
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
