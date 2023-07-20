package org.tkit.document.management.rs.v1.controllers;

import java.io.FileNotFoundException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
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
import javax.ws.rs.core.StreamingOutput;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.tkit.document.management.rs.v1.models.FileInfoDTO;
import org.tkit.document.management.rs.v1.models.FileMultipartBody;
import org.tkit.document.management.rs.v1.services.FileService;
import org.tkit.quarkus.rs.exceptions.RestException;

import io.minio.GetObjectResponse;
import io.quarkus.logging.Log;

@Path("/v1/files")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "FileControllerV1")
@ApplicationScoped
public class FileController {

    @Inject
    FileService fileService;

    private static final String CLASS_NAME = "FileController";

    @POST
    @Path("/bucket/{name}")
    @Operation(operationId = "createBucket", description = "Create a bucket", summary = "Creates a bucket with the given name")
    @APIResponse(responseCode = "201", description = "Created")
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    @Consumes(MediaType.WILDCARD)
    public Response createBucket(@PathParam("name") String name) {
        try {
            fileService.checkAndCreateBucket(name);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        return Response.status(201).build();
    }

    @PUT
    @Path("/{bucket}/{path : .+}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @Operation(operationId = "uploadFile", description = "Uploads the file", summary = "Uploads a file to the given location")
    @APIResponse(responseCode = "201", description = "Created")
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "500", description = "Internal Server Error")

    public Response uploadFile(@MultipartForm FileMultipartBody data, @PathParam("bucket") String bucket,
            @PathParam("path") String path) {
        Log.info(CLASS_NAME, "Entered uploadFile method", null);
        if (data.file.length() == 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("File has not been provided").build();
        }
        FileInfoDTO fileInfoDTO;
        try {
            fileInfoDTO = fileService.uploadFile(path, data.file, bucket);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        Log.info(CLASS_NAME, "Exited uploadFile method", null);
        return Response.status(201).entity(fileInfoDTO).build();
    }

    @GET
    @Path("/{bucket}/{path : .+}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(operationId = "downloadFile", summary = "Download file contents")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    @APIResponse(responseCode = "404", description = "Not found")
    @APIResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestException.class)))

    public Response downloadFileBytes(@PathParam("bucket") String bucket, @PathParam("path") String path) {
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

    @DELETE
    @Path("/{bucket}/{path : .+}")
    @Transactional
    @Operation(operationId = "deleteFile", description = "Deletes the file", summary = "Deletes the file from Minio object storage")
    @APIResponse(responseCode = "201", description = "File Deleted")
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "404", description = "Not Found")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response deleteFile(@PathParam("bucket") String bucket, @PathParam("path") String path) {
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
