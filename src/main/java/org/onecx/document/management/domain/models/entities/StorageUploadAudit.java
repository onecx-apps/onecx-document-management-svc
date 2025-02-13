package org.onecx.document.management.domain.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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

}
