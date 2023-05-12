package org.tkit.document.management.domain.models.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The SupportedMimeType entity.
 */
@Getter
@Setter
@Entity
@Table(name = "DM_SUPPORTED_MIME_TYPE")
public class SupportedMimeType extends TraceableEntity {
    /**
     * Name of the supported mime-type.
     */
    @Column(name = "NAME")
    private String name;
    /**
     * Description of the supported mime-type.
     */
    @Column(name = "DESCRIPTION")
    private String description;
}
