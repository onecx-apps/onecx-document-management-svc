package org.onecx.document.management.rs.v1.models;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

/**
 * Class used to create or update supported mime-type.
 */

@Getter
@Setter
public class SupportedMimeTypeCreateUpdateDTO {

    @NotBlank
    private String name;

    private String description;

}
