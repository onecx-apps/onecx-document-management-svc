package org.onecx.document.management.domain.models.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.onecx.document.management.domain.models.embeddable.TimePeriod;
import org.onecx.document.management.domain.models.enums.AttachmentUnit;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The Attachment entity.
 */
@Getter
@Setter
@Entity
@Table(name = "DM_ATTACHMENT")
public class Attachment extends TraceableEntity {
    /**
     * Name of the attachment.
     */
    @Column(name = "NAME")
    private String name;
    /**
     * Description of the attachment.
     */
    @Column(name = "DESCRIPTION")
    private String description;
    /**
     * Type of the attachment.
     */
    @Column(name = "TYPE")
    private String type;
    /**
     * Size of teh attachment.
     */
    private BigDecimal size;
    /**
     * Size unit of the attachment.
     */
    @Column(name = "SIZE_UNIT")
    @Enumerated(EnumType.STRING)
    private AttachmentUnit sizeUnit;
    /**
     * Validity period of the related party.
     */
    @Embedded
    private TimePeriod validFor;
    /**
     * Storage of teh attachment.
     */
    @Column(name = "STORAGE")
    private String storage;
    /**
     * External storage URL of the attachment.
     */
    @Column(name = "STORAGE_URL")
    private String externalStorageURL;
    /**
     * An attachment mimeType reference.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MIMETYPE_GUID")
    private SupportedMimeType mimeType;

    @Transient
    private String file;

    /**
     * Original name of the attached file.
     */
    @Column(name = "FILENAME")
    private String fileName;

    @Column(name = "STORAGE_UPLOAD_STATUS")
    private Boolean storageUploadStatus;

}
