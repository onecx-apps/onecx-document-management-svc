package org.onecx.document.management.domain.criteria;

import java.time.LocalDateTime;
import java.util.List;

import org.onecx.document.management.domain.models.enums.LifeCycleState;

import lombok.Getter;
import lombok.Setter;

/**
 * The Search Criteria for document.
 */
@Getter
@Setter
public class DocumentSearchCriteria {
    /**
     * The document id.
     */
    private String id;
    /**
     * The document name.
     */
    private String name;
    /**
     * The document state.
     */
    private List<LifeCycleState> lifeCycleState;
    /**
     * The document type id.
     */
    private List<String> documentTypeId;
    /**
     * The channel name.
     */
    private String channelName;
    /**
     * The number of page.
     */

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String createBy;

    private String objectReferenceId;

    private String objectReferenceType;

    private Integer pageNumber;
    /**
     * The size of page.
     */
    private Integer pageSize;
}
