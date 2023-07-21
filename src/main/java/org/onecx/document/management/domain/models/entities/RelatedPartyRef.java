package org.onecx.document.management.domain.models.entities;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.onecx.document.management.domain.models.embeddable.TimePeriod;
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

}
