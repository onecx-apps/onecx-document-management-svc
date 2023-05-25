package org.tkit.document.management.domain.models.entities;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.tkit.document.management.domain.models.embeddable.TimePeriod;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The RelatedParty entity.
 */
@Getter
@Setter
@Entity
@Table(name = "DM_RELATED_PARTY")
public class RelatedPartyRef extends TraceableEntity {
    /**
     * Name of the related party.
     */
    @Column(name = "NAME")
    private String name;
    /**
     * Role of the related party.
     */
    @Column(name = "ROLE")
    private String role;
    /**
     * Validity period of the related party.
     */
    @Embedded
    private TimePeriod validFor;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        result = prime * result + ((validFor == null) ? 0 : validFor.hashCode());
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
        RelatedPartyRef other = (RelatedPartyRef) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (role == null) {
            if (other.role != null)
                return false;
        } else if (!role.equals(other.role))
            return false;
        if (validFor == null) {
            if (other.validFor != null)
                return false;
        } else if (!validFor.equals(other.validFor))
            return false;
        return true;
    }
}
