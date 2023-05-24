package org.tkit.document.management.rs.v1.controllers;

import java.util.Objects;
import java.util.stream.Collectors;

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
import org.tkit.document.management.domain.daos.DocumentSpecificationDAO;
import org.tkit.document.management.domain.models.entities.DocumentSpecification;
import org.tkit.document.management.rs.v1.mappers.DocumentSpecificationMapper;
import org.tkit.document.management.rs.v1.models.DocumentSpecificationCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.DocumentSpecificationDTO;
import org.tkit.document.management.rs.v1.models.RFCProblemDTO;
import org.tkit.quarkus.rs.exceptions.RestException;

import io.quarkus.logging.Log;

@Path("/v1/document-specification")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "DocumentSpecificationControllerV1")
@ApplicationScoped
public class DocumentSpecificationController {

        @Inject
        DocumentSpecificationDAO documentSpecificationDAO;

        @Inject
        DocumentSpecificationMapper documentSpecificationMapper;

        @Inject
        DocumentDAO documentDAO;

        @POST
        @Transactional
        @Operation(operationId = "createDocumentSpecification", description = "Creates specification of document")
        @APIResponse(responseCode = "201", description = "Created specification of document", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DocumentSpecificationDTO.class)))
        @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
        @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

        public Response createDocumentSpecification(@Valid DocumentSpecificationCreateUpdateDTO dto) {
                Log.info("DocumentSpecificationController", "Entered createDocumentSpecification method", null);
                DocumentSpecification documentSpecification = documentSpecificationDAO
                                .create(documentSpecificationMapper.map(dto));
                Log.info("DocumentSpecificationController", "Exited createDocumentSpecification method", null);
                return Response.status(Response.Status.CREATED)
                                .entity(documentSpecificationMapper.mapToDTO(documentSpecification))
                                .build();
        }

        @GET
        @Path("/{id}")
        @Operation(operationId = "getDocumentSpecificationById", description = "Gets document specification by id")
        @APIResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DocumentSpecificationDTO.class)))
        @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
        @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

        public Response getDocumentSpecificationById(@PathParam("id") String id) {
                Log.info("DocumentSpecificationController", "Entered getDocumentSpecificationById method", null);
                DocumentSpecification documentSpecification = documentSpecificationDAO.findById(id);
                if (Objects.isNull(documentSpecification)) {
                        throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                                        getSpecificationNotFoundMsg(id));
                }
                Log.info("DocumentSpecificationController", "Exited getDocumentSpecificationById method", null);
                return Response.status(Response.Status.OK)
                                .entity(documentSpecificationMapper.mapToDTO(documentSpecification))
                                .build();
        }

        @GET
        @Operation(operationId = "getAllDocumentSpecifications", description = "Finds all specification of documents")
        @APIResponse(responseCode = "200", description = "Found all specification of documents", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DocumentSpecificationDTO[].class)))
        @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
        @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

        public Response getAllDocumentSpecifications() {
                Log.info("DocumentSpecificationController", "Entered getAllDocumentSpecifications method", null);
                Log.info("DocumentSpecificationController", "Exited getAllDocumentSpecifications method", null);
                return Response.status(Response.Status.OK)
                                .entity(documentSpecificationMapper
                                                .findAllDocumentSpecifications(documentSpecificationDAO.findAll()
                                                                .collect(Collectors.toList())))
                                .build();

        }

        @DELETE
        @Transactional
        @Path("/{id}")
        @Operation(operationId = "deleteDocumentSpecificationById", description = "Deletes specification of document by id")
        @APIResponse(responseCode = "204", description = "Deleted specification of document by id")
        @APIResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
        @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
        @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

        public Response deleteDocumentSpecificationById(@PathParam("id") String id) {
                Log.info("DocumentSpecificationController", "Entered deleteDocumentSpecificationById method", null);
                DocumentSpecification documentSpecification = documentSpecificationDAO.findById(id);
                if (Objects.nonNull(documentSpecification)) {
                        if (!documentDAO.findDocumentsWithDocumentSpecificationId(id).isEmpty()) {
                                throw new RestException(Response.Status.BAD_REQUEST, Response.Status.BAD_REQUEST,
                                                "You cannot delete specification of document with id " + id
                                                                + ". It is assigned to the document.");
                        }
                        documentSpecificationDAO.delete(documentSpecification);
                        Log.info("DocumentSpecificationController", "Exited deleteDocumentSpecificationById method",
                                        null);
                        return Response.status(Response.Status.NO_CONTENT).build();
                }
                throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                                getSpecificationNotFoundMsg(id));
        }

        @PUT
        @Transactional
        @Path("/{id}")
        @Operation(operationId = "updateDocumentSpecificationById", description = "Updates specification of document by id")
        @APIResponse(responseCode = "200", description = "Updated specification of document by id", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DocumentSpecificationDTO.class)))
        @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
        @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

        public Response updateDocumentSpecificationById(@PathParam("id") String id,
                        @Valid DocumentSpecificationCreateUpdateDTO dto) {
                Log.info("DocumentSpecificationController", "Entered updateDocumentSpecificationById method", null);
                DocumentSpecification documentSpecification = documentSpecificationDAO.findById(id);
                if (Objects.isNull(documentSpecification)) {
                        throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                                        getSpecificationNotFoundMsg(id));
                }
                documentSpecificationMapper.update(dto, documentSpecification);
                Log.info("DocumentSpecificationController", "Exited updateDocumentSpecificationById method", null);
                return Response.status(Response.Status.OK)
                                .entity(documentSpecificationMapper
                                                .mapToDTO(documentSpecificationDAO.update(documentSpecification)))
                                .build();
        }

        private String getSpecificationNotFoundMsg(String id) {
                Log.info("DocumentSpecificationController", "Entered getSpecificationNotFoundMsg method", null);
                Log.info("DocumentSpecificationController", "Exited getSpecificationNotFoundMsg method", null);
                return "The document specification with id " + id + " was not found.";
        }

}
