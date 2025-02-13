package org.onecx.document.management.domain.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The Category entity.
 */
@Getter
@Setter
@Entity
@Table(name = "DM_CATEGORY")
public class Category extends TraceableEntity {
    /**
     * Name of the category.
     */
    @Column(name = "NAME")
    private String name;
    /**
     * Version of the category.
     */
    @Column(name = "VERSION")
    private String categoryVersion;

}
