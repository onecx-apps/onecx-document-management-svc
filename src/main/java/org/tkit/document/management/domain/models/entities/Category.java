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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((categoryVersion == null) ? 0 : categoryVersion.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Category other = (Category) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (categoryVersion == null) {
            if (other.categoryVersion != null)
                return false;
        } else if (!categoryVersion.equals(other.categoryVersion))
            return false;
        return true;
    }
}
