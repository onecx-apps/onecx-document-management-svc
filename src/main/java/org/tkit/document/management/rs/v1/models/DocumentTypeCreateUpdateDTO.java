package org.tkit.document.management.rs.v1.models;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

/**
 * Class used to create or update document type.
 */
@Getter
@Setter
public class DocumentTypeCreateUpdateDTO {

    @NotBlank
    private String name;
}
