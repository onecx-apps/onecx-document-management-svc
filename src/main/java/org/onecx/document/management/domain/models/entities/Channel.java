package org.onecx.document.management.domain.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The Channel entity.
 */
@Getter
@Setter
@Entity
@Table(name = "DM_CHANNEL")
public class Channel extends TraceableEntity {
    /**
     * Name of the channel.
     */
    @Column(name = "NAME")
    private String name;

}
