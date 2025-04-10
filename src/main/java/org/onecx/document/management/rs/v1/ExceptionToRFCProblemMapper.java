package org.onecx.document.management.rs.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.annotation.Priority;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.onecx.document.management.rs.v1.models.RFCProblemDTO;
import org.tkit.quarkus.jpa.exceptions.DAOException;

import gen.org.onecx.document.management.rs.v1.model.RFCProblemDetail;
import lombok.extern.slf4j.Slf4j;

/**
 * Maps exceptions thrown in application to Response with RFCProblem.
 */
@Slf4j
@Provider
@Priority(ExceptionToRFCProblemMapper.PRIORITY)
public class ExceptionToRFCProblemMapper implements ExceptionMapper<Exception> {

    /**
     * The exception mapper priority
     */
    public static final int PRIORITY = 9000;
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
    public Response toResponse(Exception exception) {

        log.error("REST exception URL:{},ERROR:{}", uriInfo.getRequestUri(), exception.getMessage());
        log.error("REST exception error!", exception);

        if (exception instanceof DAOException daoException) {
            return createResponse(daoException);
        }
        if (exception instanceof RestException restException) {
            return createResponse(restException);
        }
        if (exception instanceof WebApplicationException webApplicationException) {
            return createResponse(webApplicationException);
        }
        return createResponse(exception);
    }

    /**
     * Creates the {@link Response} from the {@link DAOException}
     *
     * @param daoException the {@link DAOException}
     * @return the corresponding {@link Response}
     */
    private Response createResponse(DAOException daoException) {

        String message = daoException.getCause() != null && daoException.getCause().getCause() != null
                ? daoException.getCause().getCause().getMessage()
                : null;

        var rfcProblemDTO = RFCProblemDTO.builder()
                .type(RFCProblemType.DAO_EXCEPTION.name())
                .title(TECHNICAL_ERROR)
                .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .detail(message)
                .problems(createRfcProblemDetailDTOs(daoException.getCause()))
                .build();

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(rfcProblemDTO)
                .build();
    }

    /**
     * Creates the {@link Response} from the {@link RestException}
     *
     * @param restException the {@link RestException}
     * @return the corresponding {@link Response}
     */
    private Response createResponse(RestException restException) {
        var rfcProblemDTO = RFCProblemDTO.builder()
                .type(RFCProblemType.REST_EXCEPTION.name())
                .title(TECHNICAL_ERROR)
                .status(restException.getStatus().getStatusCode())
                .detail(restException.getParameters().get(0).toString())
                .problems(createRfcProblemDetailDTOs(restException.getCause()))
                .build();

        return Response
                .status(restException.getStatus().getStatusCode())
                .type(MediaType.APPLICATION_JSON)
                .entity(rfcProblemDTO)
                .build();
    }

    private List<RFCProblemDetail> createRfcProblemDetailDTOs(Throwable cause) {
        if (cause == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(cause.getStackTrace())
                .map(this::mapStackTraceElement)
                .toList();
    }

    private RFCProblemDetail mapStackTraceElement(StackTraceElement stackTraceElement) {
        var problemDetail = new RFCProblemDetail();
        problemDetail.setMessage(
                "An error occured in " + stackTraceElement.getMethodName() + " at line "
                        + stackTraceElement.getLineNumber());
        problemDetail.setMessageId(stackTraceElement.getClassName());

        return problemDetail;
    }

    /**
     * Creates the {@link Response} from the {@link WebApplicationException}
     *
     * @param webApplicationException the {@link WebApplicationException}
     * @return the corresponding {@link Response}
     */
    private Response createResponse(WebApplicationException webApplicationException) {
        var rfcProblemDTO = RFCProblemDTO.builder()
                .type(RFCProblemType.WEB_APPLICATION_EXCEPTION.name())
                .title(TECHNICAL_ERROR)
                .status(webApplicationException.getResponse().getStatus())
                .detail(webApplicationException.getMessage())
                .problems(createRfcProblemDetailDTOs(webApplicationException.getCause()))
                .build();

        return Response
                .fromResponse(webApplicationException.getResponse())
                .entity(rfcProblemDTO)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * Creates the {@link Response} from the {@link Exception}
     *
     * @param exception the {@link Exception}
     * @return the corresponding {@link Response}
     */
    private Response createResponse(Exception exception) {
        var rfcProblemDTO = RFCProblemDTO.builder()
                .type(Response.Status.INTERNAL_SERVER_ERROR.name())
                .title(TECHNICAL_ERROR)
                .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .detail(exception.getMessage())
                .problems(createRfcProblemDetailDTOs(exception.getCause()))
                .build();

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(rfcProblemDTO)
                .build();
    }

    /**
     * The RFCProblem types.
     */
    public enum RFCProblemType {

        /**
         * The error code for the dao exception {@link DAOException}
         */
        DAO_EXCEPTION,

        /**
         * The error code for the REST exception {@link RestException}
         */
        REST_EXCEPTION,

        /**
         * The error code for the web application exception
         * {@link WebApplicationException}
         */
        WEB_APPLICATION_EXCEPTION,

        /**
         * The error code for undefined exception.
         */
        UNDEFINED_EXCEPTION

    }
}
