package org.tkit.document.management.rs.v1.controllers;

import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.tkit.document.management.domain.daos.AttachmentDAO;
import org.tkit.document.management.domain.daos.SupportedMimeTypeDAO;
import org.tkit.document.management.domain.models.entities.SupportedMimeType;
import org.tkit.document.management.rs.v1.mappers.SupportedMimeTypeMapper;
import org.tkit.document.management.rs.v1.models.RFCProblemDTO;
import org.tkit.document.management.rs.v1.models.SupportedMimeTypeCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.SupportedMimeTypeDTO;
import org.tkit.quarkus.rs.exceptions.RestException;

@Path("/v1/supported-mime-type")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "SupportedMimeTypeControllerV1")
@ApplicationScoped
public class SupportedMimeTypeController {

    @Inject
    private SupportedMimeTypeDAO supportedMimeTypeDAO;

    @Inject
    private SupportedMimeTypeMapper supportedMimeTypeMapper;

    @Inject
    private AttachmentDAO attachmentDAO;

    @POST
    @Transactional
    @Operation(operationId = "createSupportedMimeType", description = "Creates supported mime-type")
    @APIResponse(responseCode = "201", description = "Created supported mime-type", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SupportedMimeTypeDTO.class)))
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

    public Response createSupportedMimeType(@Valid SupportedMimeTypeCreateUpdateDTO dto) {
        SupportedMimeType supportedMimeType = supportedMimeTypeDAO.create(supportedMimeTypeMapper.map(dto));
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
        SupportedMimeType supportedMimeType = supportedMimeTypeDAO.findById(id);
        if (Objects.isNull(supportedMimeType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getMimeTypeNotFoundMsg(id));
        }
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
        return Response.status(Response.Status.OK)
                .entity(supportedMimeTypeMapper.findAllSupportedMimeTypes(supportedMimeTypeDAO.findAll()
                        .collect(Collectors.toList())))
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
        SupportedMimeType supportedMimeType = supportedMimeTypeDAO.findById(id);
        if (Objects.nonNull(supportedMimeType)) {
            if (!attachmentDAO.findAttachmentsWithSupportedMimeTypeId(id).isEmpty()) {
                throw new RestException(Response.Status.BAD_REQUEST, Response.Status.BAD_REQUEST,
                        "You cannot delete supported mime-type with id " + id + ". It is assigned to the attachment.");
            }
            supportedMimeTypeDAO.delete(supportedMimeType);
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND, getMimeTypeNotFoundMsg(id));
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Operation(operationId = "updateSupportedMimeTypeById", description = "Updates supported mime-type by id")
    @APIResponse(responseCode = "200", description = "Updated supported mime-type by id", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SupportedMimeTypeDTO.class)))
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

    public Response updateSupportedMimeTypeById(@PathParam("id") String id, SupportedMimeTypeCreateUpdateDTO dto) {
        SupportedMimeType supportedMimeType = supportedMimeTypeDAO.findById(id);
        if (Objects.isNull(supportedMimeType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND, getMimeTypeNotFoundMsg(id));
        }
        supportedMimeTypeMapper.update(dto, supportedMimeType);
        return Response.status(Response.Status.OK)
                .entity(supportedMimeTypeMapper.mapToDTO(supportedMimeTypeDAO.update(supportedMimeType)))
                .build();
    }

    private String getMimeTypeNotFoundMsg(String id) {
        return "The supported mime-type with id " + id + " was not found.";
    }

}
