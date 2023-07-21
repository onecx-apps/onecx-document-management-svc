package org.onecx.document.management.rs.v1.models;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true)
@Getter
@Setter
@RegisterForReflection
public class RFCProblemDetailDTO {
    /** A detailed human-readable message of the occurence of the problem. */
    @JsonProperty(value = "message", required = true)
    private @Valid String message = null;

    /** An internal application related code, like the violated business rule Id. */
    @JsonProperty(value = "messageId", required = true)
    @ToString.Include
    private @Valid String messageId = null;

    /**
     * Application related code (as defined in the API or from a common
     * list). This would be the application specific error (such as the
     * 16-digit "Fehlerbildnummer"). To support zero prefixed error code
     * numbers (0000123412341234), the string format is used. The error code
     * MUST be 16 digits in length for coherence across applications.
     */
    @JsonProperty(value = "code")
    @ToString.Include
    private @Valid String code = null;

    @JsonProperty(value = "messageType", required = true)
    private @Valid String messageType = null;
}
