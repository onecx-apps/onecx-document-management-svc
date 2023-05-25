package org.tkit.document.management.domain.models.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "DM_STORAGE_UPLOAD_AUDIT")
public class StorageUploadAudit extends TraceableEntity {

    @Column(name = "DOCUMENT_GUID")
    private String documentId;

    @Column(name = "DOCUMENT_NAME")
    private String documentName;

    @Column(name = "DOCUMENT_DESCRIPTION")
    private String documentDescription;

    @Column(name = "DOCUMENT_VERSION")
    private String documentVersion;

    @Column(name = "DOCUMENT_STATUS")
    private String lifeCycleState;

    @Column(name = "CHANNEL_GUID")
    private String channelId;

    @Column(name = "CHANNEL_NAME")
    private String channelName;

    @Column(name = "DOCUMENT_TYPE_GUID")
    private String documentTypeId;

    @Column(name = "DOCUMENT_TYPE_NAME")
    private String documentTypeName;

    @Column(name = "ATTACHMENT_GUID")
    private String attachmentId;

    @Column(name = "FILENAME")
    private String fileName;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ATTACHMENT_DESCRIPTION")
    private String attachmentDescription;

    @Column(name = "MIMETYPE_GUID")
    private String mimeTypeId;

    @Column(name = "MIMETYPE_NAME")
    private String mimeTypeName;

    @Column(name = "SPECIFICATION_GUID")
    private String specificationId;

    @Column(name = "SPECIFICATION_NAME")
    private String specificationName;

    @Column(name = "RELATED_OBJECT_GUID")
    private String relatedObjectId;

    @Column(name = "INVOLVEMENT")
    private String involvement;

    @Column(name = "OBJECT_REFERENCE_TYPE")
    private String objectReferenceType;

    @Column(name = "OBJECT_REFERENCE_ID")
    private String objectReferenceId;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((documentId == null) ? 0 : documentId.hashCode());
        result = prime * result + ((documentName == null) ? 0 : documentName.hashCode());
        result = prime * result + ((documentDescription == null) ? 0 : documentDescription.hashCode());
        result = prime * result + ((documentVersion == null) ? 0 : documentVersion.hashCode());
        result = prime * result + ((lifeCycleState == null) ? 0 : lifeCycleState.hashCode());
        result = prime * result + ((channelId == null) ? 0 : channelId.hashCode());
        result = prime * result + ((channelName == null) ? 0 : channelName.hashCode());
        result = prime * result + ((documentTypeId == null) ? 0 : documentTypeId.hashCode());
        result = prime * result + ((documentTypeName == null) ? 0 : documentTypeName.hashCode());
        result = prime * result + ((attachmentId == null) ? 0 : attachmentId.hashCode());
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((attachmentDescription == null) ? 0 : attachmentDescription.hashCode());
        result = prime * result + ((mimeTypeId == null) ? 0 : mimeTypeId.hashCode());
        result = prime * result + ((mimeTypeName == null) ? 0 : mimeTypeName.hashCode());
        result = prime * result + ((specificationId == null) ? 0 : specificationId.hashCode());
        result = prime * result + ((specificationName == null) ? 0 : specificationName.hashCode());
        result = prime * result + ((relatedObjectId == null) ? 0 : relatedObjectId.hashCode());
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
        StorageUploadAudit other = (StorageUploadAudit) obj;
        if (documentId == null) {
            if (other.documentId != null)
                return false;
        } else if (!documentId.equals(other.documentId))
            return false;
        if (documentName == null) {
            if (other.documentName != null)
                return false;
        } else if (!documentName.equals(other.documentName))
            return false;
        if (documentDescription == null) {
            if (other.documentDescription != null)
                return false;
        } else if (!documentDescription.equals(other.documentDescription))
            return false;
        if (documentVersion == null) {
            if (other.documentVersion != null)
                return false;
        } else if (!documentVersion.equals(other.documentVersion))
            return false;
        if (lifeCycleState == null) {
            if (other.lifeCycleState != null)
                return false;
        } else if (!lifeCycleState.equals(other.lifeCycleState))
            return false;
        if (channelId == null) {
            if (other.channelId != null)
                return false;
        } else if (!channelId.equals(other.channelId))
            return false;
        if (channelName == null) {
            if (other.channelName != null)
                return false;
        } else if (!channelName.equals(other.channelName))
            return false;
        if (documentTypeId == null) {
            if (other.documentTypeId != null)
                return false;
        } else if (!documentTypeId.equals(other.documentTypeId))
            return false;
        if (documentTypeName == null) {
            if (other.documentTypeName != null)
                return false;
        } else if (!documentTypeName.equals(other.documentTypeName))
            return false;
        if (attachmentId == null) {
            if (other.attachmentId != null)
                return false;
        } else if (!attachmentId.equals(other.attachmentId))
            return false;
        if (fileName == null) {
            if (other.fileName != null)
                return false;
        } else if (!fileName.equals(other.fileName))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (attachmentDescription == null) {
            if (other.attachmentDescription != null)
                return false;
        } else if (!attachmentDescription.equals(other.attachmentDescription))
            return false;
        if (mimeTypeId == null) {
            if (other.mimeTypeId != null)
                return false;
        } else if (!mimeTypeId.equals(other.mimeTypeId))
            return false;
        if (mimeTypeName == null) {
            if (other.mimeTypeName != null)
                return false;
        } else if (!mimeTypeName.equals(other.mimeTypeName))
            return false;
        if (specificationId == null) {
            if (other.specificationId != null)
                return false;
        } else if (!specificationId.equals(other.specificationId))
            return false;
        if (specificationName == null) {
            if (other.specificationName != null)
                return false;
        } else if (!specificationName.equals(other.specificationName))
            return false;
        if (relatedObjectId == null) {
            if (other.relatedObjectId != null)
                return false;
        } else if (!relatedObjectId.equals(other.relatedObjectId))
            return false;
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
