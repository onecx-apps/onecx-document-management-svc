package org.tkit.document.management.rs.v1.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Class used to create or update channel.
 */
@Getter
@Setter
public class ChannelCreateUpdateDTO implements IdentifiableTraceableDTO {

    private String id;

    private String name;
}
