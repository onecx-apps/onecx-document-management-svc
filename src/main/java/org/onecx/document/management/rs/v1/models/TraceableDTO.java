package org.onecx.document.management.rs.v1.models;

import java.util.Objects;
import java.util.UUID;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class TraceableDTO extends AbstractTraceableDTO<String> {
    private static final long serialVersionUID = 3699279519938221976L;
    private String id = UUID.randomUUID().toString();

    public TraceableDTO() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            TraceableDTO other = (TraceableDTO) obj;
            Object guid = this.getId();
            Object otherGuid = other.getId();
            if (guid == null) {
                return otherGuid != null ? false : super.equals(obj);
            } else {
                return guid.equals(otherGuid);
            }
        }
    }

    public int hashCode() {
        //        int prime = true;
        int result = 1;
        result = 31 * result + Objects.hashCode(this.getId());
        return result;
    }
}
