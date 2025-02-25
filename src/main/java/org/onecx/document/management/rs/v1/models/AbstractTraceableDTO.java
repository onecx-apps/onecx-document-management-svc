package org.onecx.document.management.rs.v1.models;

import java.io.Serializable;
import java.time.OffsetDateTime;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public abstract class AbstractTraceableDTO<T> implements Serializable {
    private static final long serialVersionUID = -8041083748062531412L;
    private Integer modificationCount;
    private OffsetDateTime creationDate;
    private String creationUser;
    private OffsetDateTime modificationDate;
    private String modificationUser;

    public AbstractTraceableDTO() {
    }

    public abstract T getId();

    public abstract void setId(T var1);

    public String toString() {
        String var10000 = this.getClass().getSimpleName();
        return var10000 + ":" + this.getId();
    }

    public Integer getModificationCount() {
        return this.modificationCount;
    }

    public void setModificationCount(Integer modificationCount) {
        this.modificationCount = modificationCount;
    }

    public OffsetDateTime getCreationDate() {
        return this.creationDate;
    }

    public void setCreationDate(OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreationUser() {
        return this.creationUser;
    }

    public void setCreationUser(String creationUser) {
        this.creationUser = creationUser;
    }

    public OffsetDateTime getModificationDate() {
        return this.modificationDate;
    }

    public void setModificationDate(OffsetDateTime modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getModificationUser() {
        return this.modificationUser;
    }

    public void setModificationUser(String modificationUser) {
        this.modificationUser = modificationUser;
    }
}
