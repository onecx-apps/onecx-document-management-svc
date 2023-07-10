package org.tkit.document.management.domain.models.entities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.tkit.document.management.domain.models.enums.AttachmentUnit;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AttachmentTest {

    @Test
    @DisplayName("Getter And Setters for Attachment")
    void testAttachmentSetters() {

        Attachment attachment = new Attachment();

        attachment.setStorage("1020");
        attachment.setExternalStorageURL("dd");
        attachment.setSizeUnit(AttachmentUnit.BYTES);
        attachment.setSize(BigDecimal.ZERO);
        attachment.setFile("Document File");

        String Storage = attachment.getStorage();
        String ExternalStorageURL = attachment.getExternalStorageURL();
        AttachmentUnit SizeUnit = attachment.getSizeUnit();
        BigDecimal Size = attachment.getSize();
        String File = attachment.getFile();

        assertThat(Storage, equalTo("1020"));
        assertThat(ExternalStorageURL, equalTo("dd"));
        assertThat(SizeUnit, equalTo(AttachmentUnit.BYTES));
        assertThat(Size, equalTo(BigDecimal.ZERO));
        assertThat(File, equalTo("Document File"));

    }
}
