package org.tkit.document.management.domain.models.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

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
@java.lang.SuppressWarnings("java:S2160")
public class Channel extends TraceableEntity {
    /**
     * Name of the channel.
     */
    @Column(name = "NAME")
    private String name;

}
