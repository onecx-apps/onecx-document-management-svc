package org.onecx.document.management.rs.v1.models;

import java.util.Map;

import org.tkit.quarkus.rs.models.TraceableDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentResponseDTO extends TraceableDTO {

    private Map<String, Integer> attachmentResponse;
}
