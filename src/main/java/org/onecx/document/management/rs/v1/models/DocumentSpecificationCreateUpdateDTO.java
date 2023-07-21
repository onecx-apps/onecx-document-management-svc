package org.onecx.document.management.rs.v1.models;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

/**
 * Class used to create or update document specification.
 */
@Getter
@Setter
public class DocumentSpecificationCreateUpdateDTO {

    @NotBlank
    private String name;

    private String specificationVersion;
}
