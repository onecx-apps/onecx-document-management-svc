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
import org.tkit.document.management.domain.daos.DocumentDAO;
import org.tkit.document.management.domain.daos.DocumentTypeDAO;
import org.tkit.document.management.domain.models.entities.DocumentType;
import org.tkit.document.management.rs.v1.mappers.DocumentTypeMapper;
import org.tkit.document.management.rs.v1.models.DocumentTypeCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.DocumentTypeDTO;
import org.tkit.document.management.rs.v1.models.RFCProblemDTO;
import org.tkit.quarkus.rs.exceptions.RestException;

@Path("/v1/document-type")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "DocumentTypeControllerV1")
@ApplicationScoped
public class DocumentTypeController {

    @Inject
    DocumentTypeDAO documentTypeDAO;

    @Inject
    DocumentTypeMapper documentTypeMapper;

    @Inject
    DocumentDAO documentDAO;

    @POST
    @Transactional
    @Operation(operationId = "createDocumentType", description = "Creates type of document")
    @APIResponse(responseCode = "201", description = "Created type of document", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DocumentTypeDTO.class)))
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

    public Response createDocumentType(@Valid DocumentTypeCreateUpdateDTO dto) {
        DocumentType documentType = documentTypeDAO.create(documentTypeMapper.map(dto));
        return Response.status(Response.Status.CREATED)
                .entity(documentTypeMapper.mapDocumentType(documentType))
                .build();
    }

    @GET
    @Path("/{id}")
    @Operation(operationId = "getDocumentTypeById", description = "Gets document type by id")
    @APIResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DocumentTypeDTO.class)))
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

    public Response getDocumentTypeById(@PathParam("id") String id) {
        DocumentType documentType = documentTypeDAO.findById(id);
        if (Objects.isNull(documentType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND, getTypeNotFoundMsg(id));
        }
        return Response.status(Response.Status.OK)
                .entity(documentTypeMapper.mapDocumentType(documentType))
                .build();
    }

    @GET
    @Operation(operationId = "getAllTypesOfDocument", description = "Finds all types of document")
    @APIResponse(responseCode = "200", description = "Found all types of document", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DocumentTypeDTO[].class)))
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

    public Response getAllTypesOfDocument() {
        return Response.status(Response.Status.OK)
                .entity(documentTypeMapper.findAllDocumentType(documentTypeDAO.findAll().collect(Collectors.toList())))
                .build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Operation(operationId = "deleteDocumentTypeById", description = "Deletes type of document by id")
    @APIResponse(responseCode = "204", description = "Deleted type of document by id")
    @APIResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

    public Response deleteDocumentTypeById(@PathParam("id") String id) {
        DocumentType documentType = documentTypeDAO.findById(id);
        if (Objects.nonNull(documentType)) {
            if (!documentDAO.findDocumentsWithDocumentTypeId(id).isEmpty()) {
                throw new RestException(Response.Status.BAD_REQUEST, Response.Status.BAD_REQUEST,
                        "You cannot delete type of document with id " + id + ". It is assigned to the document.");
            }
            documentTypeDAO.delete(documentType);
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND, getTypeNotFoundMsg(id));
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Operation(operationId = "updateDocumentTypeById", description = "Updates type of document by id")
    @APIResponse(responseCode = "201", description = "Updated type of document by id", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DocumentTypeDTO.class)))
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

    public Response updateDocumentTypeById(@PathParam("id") String id, @Valid DocumentTypeCreateUpdateDTO dto) {
        DocumentType documentType = documentTypeDAO.findById(id);
        if (Objects.isNull(documentType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND, getTypeNotFoundMsg(id));
        }
        documentTypeMapper.update(dto, documentType);
        return Response.status(Response.Status.CREATED)
                .entity(documentTypeMapper.mapDocumentType(documentTypeDAO.update(documentType)))
                .build();
    }

    private String getTypeNotFoundMsg(String id) {
        return "The document type with id " + id + " was not found.";
    }
}
