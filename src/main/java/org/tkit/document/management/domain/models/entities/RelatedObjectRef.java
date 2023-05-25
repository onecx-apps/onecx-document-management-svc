package org.tkit.document.management.domain.models.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The RelatedObject entity.
 */
@Getter
@Setter
@Entity
@Table(name = "DM_RELATED_OBJECT")
public class RelatedObjectRef extends TraceableEntity {
    /**
     * Describes the involvement to the related object.
     */
    @Column(name = "INVOLVEMENT")
    private String involvement;
    /**
     * Type of the related object .
     */
    @Column(name = "RO_TYPE")
    private String objectReferenceType;
    /**
     * Id of the related object.
     */
    @Column(name = "RO_ID")
    private String objectReferenceId;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((involvement == null) ? 0 : involvement.hashCode());
        result = prime * result + ((objectReferenceType == null) ? 0 : objectReferenceType.hashCode());
        result = prime * result + ((objectReferenceId == null) ? 0 : objectReferenceId.hashCode());
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
        RelatedObjectRef other = (RelatedObjectRef) obj;
        if (involvement == null) {
            if (other.involvement != null)
                return false;
        } else if (!involvement.equals(other.involvement))
            return false;
        if (objectReferenceType == null) {
            if (other.objectReferenceType != null)
                return false;
        } else if (!objectReferenceType.equals(other.objectReferenceType))
            return false;
        if (objectReferenceId == null) {
            if (other.objectReferenceId != null)
                return false;
        } else if (!objectReferenceId.equals(other.objectReferenceId))
            return false;
        return true;
    }
}
