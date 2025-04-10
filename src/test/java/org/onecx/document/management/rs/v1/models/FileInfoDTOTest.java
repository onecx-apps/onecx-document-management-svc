package org.onecx.document.management.rs.v1.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.onecx.document.management.test.AbstractTest;

import gen.org.onecx.document.management.rs.v1.model.FileInfo;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileInfoDTOTest extends AbstractTest {

    private static final String TO_STRING_RESULT = "class FileInfoDTO {\n" +
            "    contentType: test\n" +
            "    path: src/resources/105\n" +
            "    bucket: test-bucket\n" +
            "}";

    @Test
    @DisplayName("FileInfoDTO toString() test")
    void fileInfoDTOToStringTest() {

        FileInfo fileInfoDTO = new FileInfo();
        fileInfoDTO.setContentType("test");
        fileInfoDTO.setBucket("test-bucket");
        fileInfoDTO.setPath("src/resources/105");
        String toStringResult = fileInfoDTO.toString();

        assertEquals(TO_STRING_RESULT, toStringResult);
    }
}
