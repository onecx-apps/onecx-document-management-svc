package org.onecx.document.management.rs.v1.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import gen.org.onecx.document.management.rs.v1.model.RFCProblemDetail;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RFCProblemDetailDTOTest {

    private static final String TO_STRING_RESULT = """
            class RFCProblemDetailDTO {
                message: null
                messageId: 12345
                code: 105
                messageType: test
            }""";

    @Test
    @DisplayName("Get Code Get Message Type")
    void testSuccessfulGetCodeShouldReturnCode() {

        RFCProblemDetail rfcProblemDetailDTO = new RFCProblemDetail();
        rfcProblemDetailDTO.setMessageType("");
        rfcProblemDetailDTO.setCode("");

        String Code = rfcProblemDetailDTO.getCode();
        String MessageType = rfcProblemDetailDTO.getMessageType();

        assertThat(Code, equalTo(""));
        assertThat(MessageType, equalTo(""));
    }

    @Test
    @DisplayName("RFCProblemDetailDTO toString() test")
    void RFCProblemDetailDTOToStringTest() {

        RFCProblemDetail rfcProblemDetailDTO = new RFCProblemDetail();
        rfcProblemDetailDTO.setMessageType("test");
        rfcProblemDetailDTO.setMessageId("12345");
        rfcProblemDetailDTO.setCode("105");

        String toStringResult = rfcProblemDetailDTO.toString();

        assertEquals(TO_STRING_RESULT, toStringResult);
    }
}
