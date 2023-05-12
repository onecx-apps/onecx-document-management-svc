package org.tkit.document.management.rs.v1.controllers;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.tkit.document.management.rs.v1.models.FileInfoDTO;
import org.tkit.document.management.rs.v1.models.FileMultipartBody;
import org.tkit.document.management.rs.v1.models.RFCProblemDTO;
import org.tkit.document.management.rs.v1.services.FileService;
import org.tkit.quarkus.rs.exceptions.RestException;

import io.minio.GetObjectResponse;

@Path("/v1/files")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "FileControllerV1")
@ApplicationScoped
public class FileController {

    @Inject
    FileService fileService;

    @ConfigProperty(name = "bucketNamePrefix", defaultValue = "def-")
    String prefix;

    @POST
    @Path("/bucket/{name}")
    @Operation(operationId = "createBucket", description = "Create a bucket", summary = "Creates a bucket with the given name")
    @APIResponse(responseCode = "201", description = "Created")
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    @Consumes(MediaType.WILDCARD)
    public Response createBucket(@PathParam("name") String name) {
        if (name == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Bucket name has to be provided").build();
        }

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
    @Operation(operationId = "uploadFile", description = "Uploads the file", summary = "Uploads a file to the given location")
    @APIResponse(responseCode = "201", description = "Created")
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "403", description = "Not Authorized")
    @APIResponse(responseCode = "500", description = "Internal Server Error")

    public Response uploadFile(@MultipartForm FileMultipartBody data, @PathParam("bucket") String bucket,
            @PathParam("path") String path) {
        if (data.file.length() == 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("File has not been provided").build();
        }
        FileInfoDTO fileInfoDTO;
        try {
            fileInfoDTO = fileService.uploadFile(path, data.file, prefix + bucket);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        return Response.status(201).entity(fileInfoDTO).build();
    }

    @GET
    @Path("/{bucket}/{path : .+}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(operationId = "downloadFile", summary = "Download file contents")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    @APIResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error, please check Problem Details", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RFCProblemDTO.class)))

    public Response downloadFileBytes(@PathParam("bucket") String bucket, @PathParam("path") String path) {
        try {
            final GetObjectResponse object = fileService.downloadFile(path, prefix + bucket);
            String contentType = object.headers().get("Content-Type");

            final byte[] data = object.readAllBytes();
            final StreamingOutput entity = output -> {
                output.write(data);
                output.flush();
            };
            return Response.ok(entity).header("Content-Type", contentType).build();
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR,
                    e.getMessage());
        }
    }
}
