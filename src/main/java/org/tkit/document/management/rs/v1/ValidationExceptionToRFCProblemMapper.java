package org.tkit.document.management.rs.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Priority;
import javax.validation.ValidationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.tkit.document.management.rs.v1.models.RFCProblemDTO;
import org.tkit.document.management.rs.v1.models.RFCProblemDetailDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * Maps ValidationException thrown in application to Response with RFCProblem.
 */
@Slf4j
@Provider
@Priority(ValidationExceptionToRFCProblemMapper.PRIORITY)
public class ValidationExceptionToRFCProblemMapper implements ExceptionMapper<ValidationException> {

    /**
     * The exception mapper priority
     */
    public static final int PRIORITY = 0;
    public static final String TECHNICAL_ERROR = "TECHNICAL ERROR";

    /**
     * The request URI info.
     */
    @Context
    UriInfo uriInfo;

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(ValidationException exception) {

        log.error("REST exception URL:{},ERROR:{}", uriInfo.getRequestUri(), exception.getMessage());
        log.error("REST exception error!", exception);
        return createResponse(exception);
    }

    /**
     * Creates the {@link Response} from the {@link ValidationException}
     *
     * @param validationException the {@link ValidationException}
     * @return the corresponding {@link Response}
     */
    private Response createResponse(ValidationException validationException) {
        RFCProblemDTO rfcProblemDTO = RFCProblemDTO.builder()
                .type(RFCProblemType.VALIDATION_EXCEPTION.name())
                .title(TECHNICAL_ERROR)
                .status(Response.Status.BAD_REQUEST.getStatusCode())
                .detail(validationException.getMessage())
                .problems(createRfcProblemDetailDTOs(validationException.getCause()))
                .build();

        return Response
                .status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(rfcProblemDTO)
                .build();
    }

    private List<RFCProblemDetailDTO> createRfcProblemDetailDTOs(Throwable cause) {
        if (cause == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(cause.getStackTrace())
                .map(this::mapStackTraceElement)
                .toList();
    }

    private RFCProblemDetailDTO mapStackTraceElement(StackTraceElement stackTraceElement) {
        RFCProblemDetailDTO problemDetail = new RFCProblemDetailDTO();
        problemDetail.setMessage(
                "An error occured in " + stackTraceElement.getMethodName() + " in line " + stackTraceElement.getLineNumber());
        problemDetail.setMessageId(stackTraceElement.getClassName());

        return problemDetail;
    }

    /**
     * The RFCProblem types.
     */
    public enum RFCProblemType {

        /**
         * The error code for the validation exception {@link ValidationException}
         */
        VALIDATION_EXCEPTION
    }
}
