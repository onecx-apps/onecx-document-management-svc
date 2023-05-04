package org.tkit.document.management.rs.v1.models;

import org.tkit.quarkus.rs.models.TraceableDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDTO extends TraceableDTO {

    private String name;

    private String categoryVersion;
}
