package org.onecx.document.management.rs.v1.models;

import org.tkit.quarkus.rs.models.TraceableDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageUploadAuditDTO extends TraceableDTO {

    private String documentId;

    private String documentName;

    private String documentDescription;

    private String documentVersion;

    private String lifeCycleState;

    private String channelId;

    private String channelName;

    private String documentTypeId;

    private String documentTypeName;

    private String attachmentId;

    private String fileName;

    private String name;

    private String attachmentDescription;

    private String mimeTypeId;

    private String mimeTypeName;

    private String specificationId;

    private String specificationName;

    private String relatedObjectId;

    private String involvement;

    private String objectReferenceType;

    private String objectReferenceId;
}
