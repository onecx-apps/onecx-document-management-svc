package org.onecx.document.management.domain.models.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.onecx.document.management.test.AbstractTest;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class StorageUploadAuditTest extends AbstractTest {
    @Test
    void testStorageUploadAuditSetters() {
        // Create an instance of StorageUploadAudit
        StorageUploadAudit storageUploadAudit = new StorageUploadAudit();

        // Define all the string variables
        String documentId = "123456";
        String documentName = "MyDoc";
        String documentDescription = "Bhupendra Singh";
        String documentVersion = "1";
        String lifeCycleState = "VALIDATED";
        String channelId = "9977";
        String channelName = "DTAG";
        String documentTypeId = "9910";
        String documentTypeName = "Sample Document";
        String attachmentId = "123";
        String fileName = "File 1";
        String name = "Bhupendra Singh";
        String attachmentDescription = "Attachment 1";
        String mimeTypeId = "1122";
        String mimeTypeName = "xls";
        String specificationId = "3535";
        String specificationName = "specification";
        String relatedObjectId = "7788";
        String involvement = "involement";
        String objectReferenceType = "Valorant";
        String objectReferenceId = "0000";

        // Use setter method to set all the properties of storageUploadAudit
        storageUploadAudit.setDocumentId(documentId);
        storageUploadAudit.setDocumentName(documentName);
        storageUploadAudit.setDocumentDescription(documentDescription);
        storageUploadAudit.setDocumentVersion(documentVersion);
        storageUploadAudit.setLifeCycleState(lifeCycleState);
        storageUploadAudit.setChannelId(channelId);
        storageUploadAudit.setChannelName(channelName);
        storageUploadAudit.setDocumentTypeId(documentTypeId);
        storageUploadAudit.setDocumentTypeName(documentTypeName);
        storageUploadAudit.setAttachmentId(attachmentId);
        storageUploadAudit.setFileName(fileName);
        storageUploadAudit.setName(name);
        storageUploadAudit.setAttachmentDescription(attachmentDescription);
        storageUploadAudit.setMimeTypeId(mimeTypeId);
        storageUploadAudit.setMimeTypeName(mimeTypeName);
        storageUploadAudit.setSpecificationId(specificationId);
        storageUploadAudit.setSpecificationName(specificationName);
        storageUploadAudit.setRelatedObjectId(relatedObjectId);
        storageUploadAudit.setInvolvement(involvement);
        storageUploadAudit.setObjectReferenceType(objectReferenceType);
        storageUploadAudit.setObjectReferenceId(objectReferenceId);

        // Verify that the documentId field is properly set and retrieved
        assertEquals(documentId, storageUploadAudit.getDocumentId());
    }
}
