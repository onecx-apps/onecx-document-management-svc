package org.tkit.document.management.domain.models.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

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
