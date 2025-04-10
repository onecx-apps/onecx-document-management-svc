package org.onecx.document.management.rs.v1.controllers;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.onecx.document.management.domain.daos.DocumentDAO;
import org.onecx.document.management.domain.daos.DocumentSpecificationDAO;
import org.onecx.document.management.rs.v1.RestException;
import org.onecx.document.management.rs.v1.mappers.DocumentSpecificationMapper;

import gen.org.onecx.document.management.rs.v1.DocumentSpecificationControllerV1Api;
import gen.org.onecx.document.management.rs.v1.model.DocumentSpecificationCreateUpdate;
import io.quarkus.logging.Log;

@ApplicationScoped
public class DocumentSpecificationController implements DocumentSpecificationControllerV1Api {

    @Inject
    DocumentSpecificationDAO documentSpecificationDAO;

    @Inject
    DocumentSpecificationMapper documentSpecificationMapper;

    @Inject
    DocumentDAO documentDAO;

    @Inject
    EntityManager entityManager;

    private static final String CLASS_NAME = "DocumentSpecificationController";

    @Override
    @Transactional
    public Response createDocumentSpecification(DocumentSpecificationCreateUpdate documentSpecificationCreateUpdateDTO) {
        Log.info(CLASS_NAME, "Entered createDocumentSpecification method", null);
        if (Objects.isNull(documentSpecificationCreateUpdateDTO.getName())
                || Objects.equals(documentSpecificationCreateUpdateDTO.getName().trim(), "")) {
            throw new RestException(Response.Status.BAD_REQUEST, Response.Status.BAD_REQUEST,
                    "createDocumentSpecification.documentSpecificationCreateUpdateDTO.name: must not be blank");
        }
        var documentSpecification = documentSpecificationDAO
                .create(documentSpecificationMapper.map(documentSpecificationCreateUpdateDTO));
        Log.info(CLASS_NAME, "Exited createDocumentSpecification method", null);
        return Response.status(Response.Status.CREATED)
                .entity(documentSpecificationMapper.mapToDTO(documentSpecification))
                .build();
    }

    @Override
    public Response getDocumentSpecificationById(String id) {
        Log.info(CLASS_NAME, "Entered getDocumentSpecificationById method", null);
        var documentSpecification = documentSpecificationDAO.findById(id);
        if (Objects.isNull(documentSpecification)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getSpecificationNotFoundMsg(id));
        }
        Log.info(CLASS_NAME, "Exited getDocumentSpecificationById method", null);
        return Response.status(Response.Status.OK)
                .entity(documentSpecificationMapper.mapToDTO(documentSpecification))
                .build();
    }

    @Override
    public Response getAllDocumentSpecifications() {
        Log.info(CLASS_NAME, "Entered getAllDocumentSpecifications method", null);
        Log.info(CLASS_NAME, "Exited getAllDocumentSpecifications method", null);
        return Response.status(Response.Status.OK)
                .entity(documentSpecificationMapper
                        .findAllDocumentSpecifications(documentSpecificationDAO.findAll()
                                .toList()))
                .build();
    }

    @Override
    @Transactional
    public Response deleteDocumentSpecificationById(String id) {
        Log.info(CLASS_NAME, "Entered deleteDocumentSpecificationById method", null);
        var documentSpecification = documentSpecificationDAO.findById(id);
        if (Objects.nonNull(documentSpecification)) {
            if (!documentDAO.findDocumentsWithDocumentSpecificationId(id).isEmpty()) {
                throw new RestException(Response.Status.BAD_REQUEST, Response.Status.BAD_REQUEST,
                        "You cannot delete specification of document with id " + id
                                + ". It is assigned to the document.");
            }
            entityManager.remove(documentSpecification);
            Log.info(CLASS_NAME, "Exited deleteDocumentSpecificationById method",
                    null);
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                getSpecificationNotFoundMsg(id));
    }

    @Override
    @Transactional
    public Response updateDocumentSpecificationById(String id,
            DocumentSpecificationCreateUpdate documentSpecificationCreateUpdateDTO) {
        Log.info(CLASS_NAME, "Entered updateDocumentSpecificationById method", null);
        var documentSpecification = documentSpecificationDAO.findById(id);
        if (Objects.isNull(documentSpecification)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getSpecificationNotFoundMsg(id));
        }
        documentSpecificationMapper.update(documentSpecificationCreateUpdateDTO, documentSpecification);
        Log.info(CLASS_NAME, "Exited updateDocumentSpecificationById method", null);
        return Response.status(Response.Status.OK)
                .entity(documentSpecificationMapper
                        .mapToDTO(documentSpecificationDAO.update(documentSpecification)))
                .build();
    }

    private String getSpecificationNotFoundMsg(String id) {
        Log.info(CLASS_NAME, "Entered getSpecificationNotFoundMsg method", null);
        Log.info(CLASS_NAME, "Exited getSpecificationNotFoundMsg method", null);
        return "The document specification with id " + id + " was not found.";
    }

}
