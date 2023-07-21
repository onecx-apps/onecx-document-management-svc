package org.onecx.document.management.rs.v1.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.onecx.document.management.test.AbstractTest;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileInfoDTOTest extends AbstractTest {

    private static final String TO_STRING_RESULT = "FileInfoDTO(contentType=test, path=src/resources/105, bucket=test-bucket)";

    @Test
    @DisplayName("FileInfoDTO toString() test")
    void fileInfoDTOToStringTest() {

        FileInfoDTO fileInfoDTO = new FileInfoDTO();
        fileInfoDTO.setContentType("test");
        fileInfoDTO.setBucket("test-bucket");
        fileInfoDTO.setPath("src/resources/105");
        String toStringResult = fileInfoDTO.toString();

        assertEquals(TO_STRING_RESULT, toStringResult);
    }
}
