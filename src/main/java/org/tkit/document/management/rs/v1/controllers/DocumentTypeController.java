package org.tkit.document.management.rs.v1.controllers;

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
import org.tkit.document.management.domain.daos.DocumentDAO;
import org.tkit.document.management.domain.daos.DocumentTypeDAO;
import org.tkit.document.management.domain.models.entities.DocumentType;
import org.tkit.document.management.rs.v1.mappers.DocumentTypeMapper;
import org.tkit.document.management.rs.v1.models.DocumentTypeCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.DocumentTypeDTO;
import org.tkit.document.management.rs.v1.models.RFCProblemDTO;
import org.tkit.quarkus.rs.exceptions.RestException;

import io.quarkus.logging.Log;

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

    private static final String CLASS_NAME = "DocumentTypeController";

    @POST
    @Transactional
    @Operation(operationId = "createDocumentType", description = "Creates type of document")
    @APIResponse(responseCode = "201", description = "Created type of document", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DocumentTypeDTO.class)))
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

    public Response createDocumentType(@Valid DocumentTypeCreateUpdateDTO dto) {
        Log.info(CLASS_NAME, "Entered createDocumentType method", null);
        DocumentType documentType = documentTypeDAO.create(documentTypeMapper.map(dto));
        Log.info(CLASS_NAME, "Exited createDocumentType method", null);
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
        Log.info(CLASS_NAME, "Entered getDocumentTypeById method", null);
        DocumentType documentType = documentTypeDAO.findById(id);
        if (Objects.isNull(documentType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getTypeNotFoundMsg(id));
        }
        Log.info(CLASS_NAME, "Exited getDocumentTypeById method", null);
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
        Log.info(CLASS_NAME, "Entered getAllTypesOfDocument method", null);
        Log.info(CLASS_NAME, "Exited getAllTypesOfDocument method", null);
        return Response.status(Response.Status.OK)
                .entity(documentTypeMapper.findAllDocumentType(
                        documentTypeDAO.findAll().toList()))
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
        Log.info(CLASS_NAME, "Entered deleteDocumentTypeById method", null);
        DocumentType documentType = documentTypeDAO.findById(id);
        if (Objects.nonNull(documentType)) {
            if (!documentDAO.findDocumentsWithDocumentTypeId(id).isEmpty()) {
                throw new RestException(Response.Status.BAD_REQUEST, Response.Status.BAD_REQUEST,
                        "You cannot delete type of document with id " + id
                                + ". It is assigned to the document.");
            }
            documentTypeDAO.delete(documentType);
            Log.info(CLASS_NAME, "Exited deleteDocumentTypeById method", null);
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
        Log.info(CLASS_NAME, "Entered updateDocumentTypeById method", null);
        DocumentType documentType = documentTypeDAO.findById(id);
        if (Objects.isNull(documentType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getTypeNotFoundMsg(id));
        }
        documentTypeMapper.update(dto, documentType);
        Log.info(CLASS_NAME, "Exited updateDocumentTypeById method", null);
        return Response.status(Response.Status.CREATED)
                .entity(documentTypeMapper.mapDocumentType(documentTypeDAO.update(documentType)))
                .build();
    }

    private String getTypeNotFoundMsg(String id) {
        Log.info(CLASS_NAME, "Entered getTypeNotFoundMsg method", null);
        Log.info(CLASS_NAME, "Exited getTypeNotFoundMsg method", null);
        return "The document type with id " + id + " was not found.";
    }
}
