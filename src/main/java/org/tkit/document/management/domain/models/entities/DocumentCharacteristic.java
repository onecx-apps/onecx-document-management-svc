package org.tkit.document.management.domain.models.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The DocumentCharacteristic entity.
 */
@Getter
@Setter
@Entity
@Table(name = "DM_DOCUMENT_CHARACTERISTIC")
@java.lang.SuppressWarnings("java:S2160")
public class DocumentCharacteristic extends TraceableEntity {
    /**
     * Name of the characteristic.
     */
    @Column(name = "NAME")
    private String name;
    /**
     * Value of the characteristic.
     */
    @Column(name = "VALUE")
    private String value;

}
