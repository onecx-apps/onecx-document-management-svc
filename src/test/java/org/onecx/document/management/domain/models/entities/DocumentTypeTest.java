package org.onecx.document.management.domain.models.entities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentTypeTest {
    @Test
    @DisplayName("Getter and Setters for Document Type")
    void testDocumentTypeSetters() {

        DocumentType documentType = new DocumentType();
        documentType.setDescription("The Document has been created");
        documentType.setActiveStatus(true);

        String Description = documentType.getDescription();
        Boolean ActiveStatus = documentType.getActiveStatus();

        assertThat(Description, equalTo("The Document has been created"));
        assertThat(ActiveStatus, equalTo(true));

    }
}
