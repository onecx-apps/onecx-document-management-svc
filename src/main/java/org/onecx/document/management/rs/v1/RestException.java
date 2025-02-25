package org.onecx.document.management.rs.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class RestException extends RuntimeException {
    private Response.Status status;
    private Enum<?> errorCode;
    private List<Object> parameters;
    private Map<String, Object> namedParameters;

    public RestException(Enum<?> errorCode) {
        this(errorCode, Status.INTERNAL_SERVER_ERROR);
    }

    public RestException(Enum<?> errorCode, Response.Status status) {
        super(requireNonNull(errorCode));
        this.parameters = new ArrayList();
        this.namedParameters = new HashMap();
        this.errorCode = errorCode;
        this.status = status;
    }

    public RestException(Enum<?> errorCode, Response.Status status, Throwable cause) {
        super(requireNonNull(errorCode), cause);
        this.parameters = new ArrayList();
        this.namedParameters = new HashMap();
        this.errorCode = errorCode;
        this.status = status;
    }

    public RestException(Enum<?> errorCode, Throwable cause) {
        this(errorCode, Status.INTERNAL_SERVER_ERROR, cause);
    }

    public RestException(Enum<?> errorCode, Response.Status status, Throwable cause, Object... params) {
        this(errorCode, status, cause);
        if (params != null) {
            Collections.addAll(this.parameters, params);
        }

    }

    public RestException(Enum<?> errorCode, Throwable cause, Object... params) {
        this(errorCode, Status.INTERNAL_SERVER_ERROR, cause, params);
    }

    public RestException(Enum<?> errorCode, Response.Status status, Object... params) {
        this(errorCode, status);
        if (params != null) {
            Collections.addAll(this.parameters, params);
        }

    }

    public RestException(Enum<?> errorCode, Object... params) {
        this(errorCode, Status.INTERNAL_SERVER_ERROR, params);
    }

    public RestException addParam(String name, Object value) {
        this.namedParameters.put(name, value);
        return this;
    }

    public RestException addParam(Object value) {
        this.parameters.add(value);
        return this;
    }

    public RestException withParams(Object... params) {
        Collections.addAll(this.parameters, params);
        return this;
    }

    public RestException withNamedParams(Map<String, Object> namedParams) {
        this.namedParameters.putAll(namedParams);
        return this;
    }

    private static String requireNonNull(Enum<?> errorCode) {
        if (errorCode == null) {
            throw new NullPointerException("Error code is null!");
        } else {
            return errorCode.name();
        }
    }

    public Response.Status getStatus() {
        return this.status;
    }

    public Enum<?> getErrorCode() {
        return this.errorCode;
    }

    public List<Object> getParameters() {
        return this.parameters;
    }

    public Map<String, Object> getNamedParameters() {
        return this.namedParameters;
    }
}
