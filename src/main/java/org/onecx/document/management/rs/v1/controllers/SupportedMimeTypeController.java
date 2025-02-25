package org.onecx.document.management.rs.v1.controllers;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.onecx.document.management.domain.daos.AttachmentDAO;
import org.onecx.document.management.domain.daos.SupportedMimeTypeDAO;
import org.onecx.document.management.rs.v1.RestException;
import org.onecx.document.management.rs.v1.mappers.SupportedMimeTypeMapper;

import gen.org.onecx.document.management.rs.v1.SupportedMimeTypeControllerV1Api;
import gen.org.onecx.document.management.rs.v1.model.SupportedMimeTypeCreateUpdateDTO;
import io.quarkus.logging.Log;

@ApplicationScoped
public class SupportedMimeTypeController implements SupportedMimeTypeControllerV1Api {

    @Inject
    SupportedMimeTypeDAO supportedMimeTypeDAO;

    @Inject
    SupportedMimeTypeMapper supportedMimeTypeMapper;

    @Inject
    AttachmentDAO attachmentDAO;

    private static final String CLASS_NAME = "SupportedMimeTypeController";

    @Override
    @Transactional
    public Response createSupportedMimeType(SupportedMimeTypeCreateUpdateDTO supportedMimeTypeCreateUpdateDTO) {
        Log.info(CLASS_NAME, "Entered createSupportedMimeType method", null);
        var supportedMimeType = supportedMimeTypeDAO.create(supportedMimeTypeMapper.map(supportedMimeTypeCreateUpdateDTO));
        Log.info(CLASS_NAME, "Exited createSupportedMimeType method", null);
        return Response.status(Response.Status.CREATED)
                .entity(supportedMimeTypeMapper.mapToDTO(supportedMimeType))
                .build();
    }

    @Override
    public Response getSupportedMimeTypeById(String id) {
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

    @Override
    public Response getAllSupportedMimeTypes() {
        Log.info(CLASS_NAME, "Entered getAllSupportedMimeTypes method", null);
        Log.info(CLASS_NAME, "Exited getAllSupportedMimeTypes method", null);
        return Response.status(Response.Status.OK)
                .entity(supportedMimeTypeMapper.findAllSupportedMimeTypes(supportedMimeTypeDAO.findAll()
                        .toList()))
                .build();
    }

    @Override
    @Transactional
    public Response deleteSupportedMimeTypeId(String id) {
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

    @Override
    @Transactional
    public Response updateSupportedMimeTypeById(String id, SupportedMimeTypeCreateUpdateDTO supportedMimeTypeCreateUpdateDTO) {
        Log.info(CLASS_NAME, "Entered updateSupportedMimeTypeById method", null);
        var supportedMimeType = supportedMimeTypeDAO.findById(id);
        if (Objects.isNull(supportedMimeType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getMimeTypeNotFoundMsg(id));
        }
        supportedMimeTypeMapper.update(supportedMimeTypeCreateUpdateDTO, supportedMimeType);
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
