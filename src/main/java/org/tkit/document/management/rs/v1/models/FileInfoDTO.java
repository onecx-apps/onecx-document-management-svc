package org.tkit.document.management.rs.v1.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FileInfoDTO {
    /**
     * Type of file based on the InputStream.
     */
    String contentType;
    /**
     * The path of the file
     */
    String path;
    /**
     * Name of bucket
     */
    String bucket;
}
