package org.onecx.document.management.rs.v1.models;

import org.tkit.quarkus.rs.models.TraceableDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentTypeDTO extends TraceableDTO {

    private String name;
}
