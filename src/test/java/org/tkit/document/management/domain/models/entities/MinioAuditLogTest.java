package org.tkit.document.management.domain.models.entities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MinioAuditLogTest {

    @Test
    @DisplayName("Getter And Setters for AttachmentId")
    void testAttachmentSetters() {

        MinioAuditLog minioAuditLog = new MinioAuditLog();
        minioAuditLog.setAttachmentId("1101");
        String Attachment = minioAuditLog.getAttachmentId();
        assertThat(Attachment, equalTo("1101"));
    }
}
