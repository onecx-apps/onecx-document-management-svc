package org.onecx.document.management.rs.v1.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.onecx.document.management.test.AbstractTest;

import gen.org.onecx.document.management.rs.v1.model.DocumentSearchCriteriaDTO;
import gen.org.onecx.document.management.rs.v1.model.LifeCycleStateDTO;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentSearchCriteriaDTOTest extends AbstractTest {

    @Test
    @DisplayName("Get criteria Should Return criteria value")
    void testSuccessfulGettingValues() {

        DocumentSearchCriteriaDTO criteria = new DocumentSearchCriteriaDTO();
        criteria.setId("001");
        criteria.setName("Important_Document");
        criteria.setLifeCycleState(List.of(LifeCycleStateDTO.DRAFT, LifeCycleStateDTO.REVIEW));
        criteria.setDocumentTypeId(List.of("1", "2"));
        criteria.setChannelName("Channel1");
        criteria.setStartDate("2022-07-05");
        criteria.setEndDate("2023-07-05");
        criteria.setCreateBy("Albert");
        criteria.setObjectReferenceId("ObjRef1");
        criteria.setObjectReferenceType("RefType1");
        criteria.setPageNumber(2);
        criteria.setPageSize(50);

        String id = criteria.getId();
        String name = criteria.getName();
        List<LifeCycleStateDTO> lifeCycleState = criteria.getLifeCycleState();
        List<String> documentTypeId = criteria.getDocumentTypeId();
        String channelName = criteria.getChannelName();
        String startDate = criteria.getStartDate();
        String endDate = criteria.getEndDate();
        String createBy = criteria.getCreateBy();
        String objectReferenceId = criteria.getObjectReferenceId();
        String objectReferenceType = criteria.getObjectReferenceType();
        Integer pageNumber = criteria.getPageNumber();
        Integer pageSize = criteria.getPageSize();

        assertThat(id, equalTo("001"));
        assertThat(name, equalTo("Important_Document"));
        assertThat(lifeCycleState, contains(LifeCycleStateDTO.DRAFT, LifeCycleStateDTO.REVIEW));
        assertThat(documentTypeId, contains("1", "2"));
        assertThat(channelName, equalTo("Channel1"));
        assertThat(startDate, equalTo("2022-07-05"));
        assertThat(endDate, equalTo("2023-07-05"));
        assertThat(createBy, equalTo("Albert"));
        assertThat(objectReferenceId, equalTo("ObjRef1"));
        assertThat(objectReferenceType, equalTo("RefType1"));
        assertThat(pageNumber, equalTo(2));
        assertThat(pageSize, equalTo(50));
    }
}
