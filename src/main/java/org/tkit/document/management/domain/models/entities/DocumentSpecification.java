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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((specificationVersion == null) ? 0 : specificationVersion.hashCode());
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
        DocumentSpecification other = (DocumentSpecification) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (specificationVersion == null) {
            if (other.specificationVersion != null)
                return false;
        } else if (!specificationVersion.equals(other.specificationVersion))
            return false;
        return true;
    }
}
