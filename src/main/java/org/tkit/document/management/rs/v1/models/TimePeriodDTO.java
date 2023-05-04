package org.tkit.document.management.rs.v1.models;

import java.io.Serializable;
import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimePeriodDTO implements Serializable {

    private OffsetDateTime startDateTime;

    private OffsetDateTime endDateTime;
}
