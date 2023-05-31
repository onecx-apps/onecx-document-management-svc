package org.tkit.document.management.rs.v1.models;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

import org.tkit.document.management.domain.models.enums.LifeCycleState;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentSearchCriteriaDTO {

    @QueryParam("id")
    private String id;

    @QueryParam("name")
    private String name;

    @QueryParam("state")
    private List<LifeCycleState> lifeCycleState;

    @QueryParam("typeId")
    private List<String> documentTypeId;

    @QueryParam("channelName")
    private String channelName;

    @QueryParam("startDate")
    private String startDate;

    @QueryParam("endDate")
    private String endDate;

    @QueryParam("createdBy")
    private String createBy;

    @QueryParam("objectReferenceId")
    private String objectReferenceId;

    @QueryParam("objectReferenceType")
    private String objectReferenceType;

    @Min(0)
    @QueryParam("page")
    @DefaultValue("0")
    private Integer pageNumber;

    @Min(1)
    @Max(200)
    @QueryParam("size")
    @DefaultValue("200")
    private Integer pageSize;
}
