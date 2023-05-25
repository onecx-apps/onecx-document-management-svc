package org.tkit.document.management.domain.models.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The DocumentRelationship entity.
 */
@Getter
@Setter
@Entity
@Table(name = "DM_DOCUMENT_RELATIONSHIP")
public class DocumentRelationship extends TraceableEntity {
    /**
     * Type of the relationship.
     */
    @Column(name = "TYPE")
    private String type;
    /**
     * Id of the referenced document.
     */
    @Column(name = "DOCUMENT_REF_ID")
    private String documentRefId;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((documentRefId == null) ? 0 : documentRefId.hashCode());
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
        DocumentRelationship other = (DocumentRelationship) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (documentRefId == null) {
            if (other.documentRefId != null)
                return false;
        } else if (!documentRefId.equals(other.documentRefId))
            return false;
        return true;
    }
}
