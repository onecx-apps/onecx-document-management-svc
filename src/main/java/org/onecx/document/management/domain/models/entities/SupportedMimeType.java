package org.onecx.document.management.domain.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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
