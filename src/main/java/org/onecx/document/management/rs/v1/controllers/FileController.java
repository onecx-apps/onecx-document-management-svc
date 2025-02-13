package org.onecx.document.management.rs.v1.controllers;

import java.io.File;
import java.io.FileNotFoundException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import org.onecx.document.management.rs.v1.RestException;
import org.onecx.document.management.rs.v1.services.FileService;

import gen.org.onecx.document.management.rs.v1.FileControllerV1Api;
import gen.org.onecx.document.management.rs.v1.model.FileInfoDTO;
import io.minio.GetObjectResponse;
import io.quarkus.logging.Log;

@ApplicationScoped
public class FileController implements FileControllerV1Api {

    @Inject
    FileService fileService;

    private static final String CLASS_NAME = "FileController";

    @Override
    @Transactional
    public Response createBucket(String name) {
        try {
            fileService.checkAndCreateBucket(name);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        return Response.status(201).build();
    }

    @Override
    public Response uploadFile(String bucket, String path, File file) {
        Log.info(CLASS_NAME, "Entered uploadFile method", null);
        Log.info("lengthwew" + file.getAbsoluteFile().length());
        if (file.length() == 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("File has not been provided").build();
        }
        FileInfoDTO fileInfoDTO;
        try {
            fileInfoDTO = fileService.uploadFile(path, file, bucket);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        Log.info(CLASS_NAME, "Exited uploadFile method", null);
        return Response.status(201).entity(fileInfoDTO).build();
    }

    @Override
    public Response downloadFile(String bucket, String path) {
        Log.info(CLASS_NAME, "Entered downloadFileBytes method", null);
        try {
            final GetObjectResponse object = fileService.downloadFile(path, bucket);
            String contentType = object.headers().get("Content-Type");

            final byte[] data = object.readAllBytes();
            final StreamingOutput entity = output -> {
                output.write(data);
                output.flush();
            };
            Log.info(CLASS_NAME, "Exited downloadFileBytes method", null);
            return Response.ok(entity).header("Content-Type", contentType).build();
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR,
                    e.getMessage());
        }
    }

    @Override
    @Transactional
    public Response deleteFile(String bucket, String path) {
        Log.info(CLASS_NAME, "Entered deleteFile method", null);
        try {
            fileService.deleteFile(path, bucket);
            Log.info(CLASS_NAME, "Exited deleteFile method", null);
            return Response.status(Response.Status.CREATED).build();
        } catch (FileNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
