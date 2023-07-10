package org.tkit.document.management.rs.v1.models;

import java.math.BigDecimal;

import org.tkit.document.management.domain.models.enums.AttachmentUnit;
import org.tkit.quarkus.rs.models.TraceableDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachmentDTO extends TraceableDTO {

    private String name;

    private String description;

    private String type;

    private BigDecimal size;

    private AttachmentUnit sizeUnit;

    private TimePeriodDTO validFor;

    private String storage;

    private String externalStorageURL;

    private SupportedMimeTypeDTO mimeType;

    private String fileName;

    private Boolean storageUploadStatus;
}
