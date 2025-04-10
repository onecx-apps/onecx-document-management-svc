package org.onecx.document.management.rs.v1.controllers;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.onecx.document.management.domain.daos.DocumentDAO;
import org.onecx.document.management.domain.daos.DocumentTypeDAO;
import org.onecx.document.management.rs.v1.RestException;
import org.onecx.document.management.rs.v1.mappers.DocumentTypeMapper;

import gen.org.onecx.document.management.rs.v1.DocumentTypeControllerV1Api;
import gen.org.onecx.document.management.rs.v1.model.DocumentTypeCreateUpdate;
import io.quarkus.logging.Log;

@ApplicationScoped
public class DocumentTypeController implements DocumentTypeControllerV1Api {

    @Inject
    DocumentTypeDAO documentTypeDAO;

    @Inject
    DocumentTypeMapper documentTypeMapper;

    @Inject
    DocumentDAO documentDAO;

    private static final String CLASS_NAME = "DocumentTypeController";

    @Override
    @Transactional
    public Response createDocumentType(DocumentTypeCreateUpdate documentTypeCreateUpdateDTO) {
        Log.info(CLASS_NAME, "Entered createDocumentType method", null);
        var documentType = documentTypeDAO.create(documentTypeMapper.map(documentTypeCreateUpdateDTO));
        Log.info(CLASS_NAME, "Exited createDocumentType method", null);
        return Response.status(Response.Status.CREATED)
                .entity(documentTypeMapper.mapDocumentType(documentType))
                .build();
    }

    @Override
    public Response getDocumentTypeById(String id) {
        Log.info(CLASS_NAME, "Entered getDocumentTypeById method", null);
        var documentType = documentTypeDAO.findById(id);
        if (Objects.isNull(documentType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getTypeNotFoundMsg(id));
        }
        Log.info(CLASS_NAME, "Exited getDocumentTypeById method", null);
        return Response.status(Response.Status.OK)
                .entity(documentTypeMapper.mapDocumentType(documentType))
                .build();
    }

    @Override
    public Response getAllTypesOfDocument() {
        Log.info(CLASS_NAME, "Entered getAllTypesOfDocument method", null);
        Log.info(CLASS_NAME, "Exited getAllTypesOfDocument method", null);
        return Response.status(Response.Status.OK)
                .entity(documentTypeMapper.findAllDocumentType(
                        documentTypeDAO.findAll().toList()))
                .build();
    }

    @Override
    @Transactional
    public Response deleteDocumentTypeById(String id) {
        Log.info(CLASS_NAME, "Entered deleteDocumentTypeById method", null);
        var documentType = documentTypeDAO.findById(id);
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

    @Override
    @Transactional
    public Response updateDocumentTypeById(String id, DocumentTypeCreateUpdate documentTypeCreateUpdateDTO) {
        Log.info(CLASS_NAME, "Entered updateDocumentTypeById method", null);
        var documentType = documentTypeDAO.findById(id);
        if (Objects.isNull(documentType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getTypeNotFoundMsg(id));
        }
        documentTypeMapper.update(documentTypeCreateUpdateDTO, documentType);
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
