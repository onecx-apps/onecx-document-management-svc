package org.onecx.document.management.rs.v1.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Class used to create or update category.
 */
@Getter
@Setter
public class CategoryCreateUpdateDTO implements IdentifiableTraceableDTO {

    private String id;

    private String name;

    private String categoryVersion;
}
