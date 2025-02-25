package org.onecx.document.management.rs.v1.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.onecx.document.management.test.AbstractTest;

import gen.org.onecx.document.management.rs.v1.model.RFCProblemDetailDTO;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RFCProblemDTOTest extends AbstractTest {

    private static final String TO_STRING_RESULT_1 = "RFCProblemDTO(type=test-type, title=test-title, status=200)";
    private static final String TO_STRING_RESULT_2 = "RFCProblemDTO(type=test-type, title=test-title, status=400)";
    private static final String TO_STRING_RESULT_3 = "RFCProblemDTO.RFCProblemDTOBuilder(type=null, title=null, status=null, detail=null, instance=null, problems=null)";

    @Test
    @DisplayName("RFCProblemDTO toString() test")
    void RFCProblemDTOToStringTest() {

        RFCProblemDTO rfcProblemDTO = new RFCProblemDTO();
        rfcProblemDTO.setDetail("test-detail");
        rfcProblemDTO.setInstance("test-instance");
        rfcProblemDTO.setProblems(null);
        rfcProblemDTO.setStatus(200);
        rfcProblemDTO.setTitle("test-title");
        rfcProblemDTO.setType("test-type");

        String toStringResult = rfcProblemDTO.toString();

        assertEquals(TO_STRING_RESULT_1, toStringResult);
    }

    @Test
    @DisplayName("RFCProblemDTO @Builder test")
    void RFCProblemDTOBuilderTest() {

        String type = "test-type";
        String title = "test-title";
        Integer status = 400;
        String detail = "test-detail";
        String instance = "test-instance";
        List<RFCProblemDetailDTO> problems = new ArrayList<>();

        RFCProblemDTO dto = RFCProblemDTO.builder()
                .type(type)
                .title(title)
                .status(status)
                .detail(detail)
                .instance(instance)
                .problems(problems)
                .build();

        assertEquals(type, dto.getType());
        assertEquals(title, dto.getTitle());
        assertEquals(status, dto.getStatus());
        assertEquals(detail, dto.getDetail());
        assertEquals(instance, dto.getInstance());
        assertEquals(TO_STRING_RESULT_2, dto.toString());
    }

    @Test
    @DisplayName("RFCProblemDTO Builder toString() test")
    void RFCProblemDTOBuilderToStringTest() {

        String toStringResult = RFCProblemDTO.builder().toString();

        assertEquals(TO_STRING_RESULT_3, toStringResult);
    }
}
