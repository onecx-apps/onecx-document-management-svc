package org.onecx.document.management.rs.v1.controllers;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.onecx.document.management.domain.daos.AttachmentDAO;
import org.onecx.document.management.domain.daos.SupportedMimeTypeDAO;
import org.onecx.document.management.rs.v1.mappers.SupportedMimeTypeMapper;
import org.onecx.document.management.rs.v1.models.RFCProblemDTO;
import org.onecx.document.management.rs.v1.models.SupportedMimeTypeCreateUpdateDTO;
import org.onecx.document.management.rs.v1.models.SupportedMimeTypeDTO;
import org.tkit.quarkus.rs.exceptions.RestException;

import io.quarkus.logging.Log;

@Path("/v1/supported-mime-type")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "SupportedMimeTypeControllerV1")
@ApplicationScoped
public class SupportedMimeTypeController {

    @Inject
    SupportedMimeTypeDAO supportedMimeTypeDAO;

    @Inject
    SupportedMimeTypeMapper supportedMimeTypeMapper;

    @Inject
    AttachmentDAO attachmentDAO;

    private static final String CLASS_NAME = "SupportedMimeTypeController";

    @POST
    @Transactional
    @Operation(operationId = "createSupportedMimeType", description = "Creates supported mime-type")
    @APIResponse(responseCode = "201", description = "Created supported mime-type", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SupportedMimeTypeDTO.class)))
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

    public Response createSupportedMimeType(@Valid SupportedMimeTypeCreateUpdateDTO dto) {
        Log.info(CLASS_NAME, "Entered createSupportedMimeType method", null);
        var supportedMimeType = supportedMimeTypeDAO.create(supportedMimeTypeMapper.map(dto));
        Log.info(CLASS_NAME, "Exited createSupportedMimeType method", null);
        return Response.status(Response.Status.CREATED)
                .entity(supportedMimeTypeMapper.mapToDTO(supportedMimeType))
                .build();
    }

    @GET
    @Path("/{id}")
    @Operation(operationId = "getSupportedMimeTypeById", description = "Gets supported mime-type by id")
    @APIResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SupportedMimeTypeDTO.class)))
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

    public Response getSupportedMimeTypeById(@PathParam("id") String id) {
        Log.info(CLASS_NAME, "Entered getSupportedMimeTypeById method", null);
        var supportedMimeType = supportedMimeTypeDAO.findById(id);
        if (Objects.isNull(supportedMimeType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getMimeTypeNotFoundMsg(id));
        }
        Log.info(CLASS_NAME, "Exited getSupportedMimeTypeById method", null);
        return Response.status(Response.Status.OK)
                .entity(supportedMimeTypeMapper.mapToDTO(supportedMimeType))
                .build();
    }

    @GET
    @Operation(operationId = "getAllSupportedMimeTypes", description = "Finds all supported mime-types")
    @APIResponse(responseCode = "200", description = "Found all supported mime-types", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SupportedMimeTypeDTO[].class)))
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

    public Response getAllSupportedMimeTypes() {
        Log.info(CLASS_NAME, "Entered getAllSupportedMimeTypes method", null);
        Log.info(CLASS_NAME, "Exited getAllSupportedMimeTypes method", null);
        return Response.status(Response.Status.OK)
                .entity(supportedMimeTypeMapper.findAllSupportedMimeTypes(supportedMimeTypeDAO.findAll()
                        .toList()))
                .build();

    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Operation(operationId = "deleteSupportedMimeTypeId", description = "Deletes supported mime-type by id")
    @APIResponse(responseCode = "204", description = "Deleted supported mime-type by id")
    @APIResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

    public Response deleteSupportedMimeTypeById(@PathParam("id") String id) {
        Log.info(CLASS_NAME, "Entered deleteSupportedMimeTypeById method", null);
        var supportedMimeType = supportedMimeTypeDAO.findById(id);
        if (Objects.nonNull(supportedMimeType)) {
            if (!attachmentDAO.findAttachmentsWithSupportedMimeTypeId(id).isEmpty()) {
                throw new RestException(Response.Status.BAD_REQUEST, Response.Status.BAD_REQUEST,
                        "You cannot delete supported mime-type with id " + id
                                + ". It is assigned to the attachment.");
            }
            supportedMimeTypeDAO.delete(supportedMimeType);
            Log.info(CLASS_NAME, "Exited deleteSupportedMimeTypeById method", null);
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                getMimeTypeNotFoundMsg(id));
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Operation(operationId = "updateSupportedMimeTypeById", description = "Updates supported mime-type by id")
    @APIResponse(responseCode = "200", description = "Updated supported mime-type by id", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SupportedMimeTypeDTO.class)))
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

    public Response updateSupportedMimeTypeById(@PathParam("id") String id, SupportedMimeTypeCreateUpdateDTO dto) {
        Log.info(CLASS_NAME, "Entered updateSupportedMimeTypeById method", null);
        var supportedMimeType = supportedMimeTypeDAO.findById(id);
        if (Objects.isNull(supportedMimeType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getMimeTypeNotFoundMsg(id));
        }
        supportedMimeTypeMapper.update(dto, supportedMimeType);
        Log.info(CLASS_NAME, "Exited updateSupportedMimeTypeById method", null);
        return Response.status(Response.Status.OK)
                .entity(supportedMimeTypeMapper
                        .mapToDTO(supportedMimeTypeDAO.update(supportedMimeType)))
                .build();
    }

    private String getMimeTypeNotFoundMsg(String id) {
        Log.info(CLASS_NAME, "Entered getMimeTypeNotFoundMsg method", null);
        Log.info(CLASS_NAME, "Exited getMimeTypeNotFoundMsg method", null);
        return "The supported mime-type with id " + id + " was not found.";
    }

}
