package org.tkit.document.management.domain.models.entities;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.tkit.document.management.domain.models.embeddable.TimePeriod;
import org.tkit.document.management.domain.models.enums.AttachmentUnit;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((size == null) ? 0 : size.hashCode());
        result = prime * result + ((sizeUnit == null) ? 0 : sizeUnit.hashCode());
        result = prime * result + ((validFor == null) ? 0 : validFor.hashCode());
        result = prime * result + ((storage == null) ? 0 : storage.hashCode());
        result = prime * result + ((externalStorageURL == null) ? 0 : externalStorageURL.hashCode());
        result = prime * result + ((mimeType == null) ? 0 : mimeType.hashCode());
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + ((storageUploadStatus == null) ? 0 : storageUploadStatus.hashCode());
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
        Attachment other = (Attachment) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (size == null) {
            if (other.size != null)
                return false;
        } else if (!size.equals(other.size))
            return false;
        if (sizeUnit != other.sizeUnit)
            return false;
        if (validFor == null) {
            if (other.validFor != null)
                return false;
        } else if (!validFor.equals(other.validFor))
            return false;
        if (storage == null) {
            if (other.storage != null)
                return false;
        } else if (!storage.equals(other.storage))
            return false;
        if (externalStorageURL == null) {
            if (other.externalStorageURL != null)
                return false;
        } else if (!externalStorageURL.equals(other.externalStorageURL))
            return false;
        if (mimeType == null) {
            if (other.mimeType != null)
                return false;
        } else if (!mimeType.equals(other.mimeType))
            return false;
        if (file == null) {
            if (other.file != null)
                return false;
        } else if (!file.equals(other.file))
            return false;
        if (fileName == null) {
            if (other.fileName != null)
                return false;
        } else if (!fileName.equals(other.fileName))
            return false;
        if (storageUploadStatus == null) {
            if (other.storageUploadStatus != null)
                return false;
        } else if (!storageUploadStatus.equals(other.storageUploadStatus))
            return false;
        return true;
    }
}
