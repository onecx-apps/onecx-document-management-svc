package org.onecx.document.management.domain.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * MinioAuditLog Entity Class
 * The table which got created for this entity class stores the attachmentIds of
 * all those attachments
 * which got deleted from "dm_attachment" table
 * but the file object of that attachment got failed to get deleted from the
 * Minio Storage.
 */
@Getter
@Setter
@Entity
@Table(name = "DM_MINIO_AUDIT_LOG")
public class MinioAuditLog extends TraceableEntity {

    /**
     * attachmentId of the attachment whose file object could not be deleted from
     * Minio bucket
     */
    @Column(name = "ATTACHMENT_GUID")
    private String attachmentId;

}
