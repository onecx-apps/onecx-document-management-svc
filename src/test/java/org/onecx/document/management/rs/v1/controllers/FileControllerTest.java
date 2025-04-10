package org.onecx.document.management.rs.v1.controllers;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.onecx.document.management.test.AbstractTest.USER;

import java.io.*;

import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.onecx.document.management.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;

import gen.org.onecx.document.management.rs.v1.model.FileInfo;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;

@QuarkusTest
@GenerateKeycloakClient(clientName = USER, scopes = "ocx-doc:read")
class FileControllerTest extends AbstractTest {

    private static final String SAMPLE_FILE_PATH = "src/test/resources/sample.jpg";
    private static final String SAMPLE2_FILE_PATH = "src/test/resources/sample2.jpg";
    private static final String BLANK_FILE_PATH = "src/test/resources/empty_file.txt";
    private static final String UNKNOWN_FILE_PATH = "src/test/resources/unknown_content_type_file";
    private static final String SAMPLE_FILE_TYPE = "application/octet-stream";
    private static final String MINIO_FILE_PATH = "a.jpg";
    private static final String MINIO_UNKNOWN_FILE_PATH = "unknown_content_type_file";
    private static final String BUCKET_NAME = "test-bucket";
    private static final String NOT_ALLOWED_BUCKET_NAME = "test_bucket";
    private static final String FORM_PARAM_FILE = "file";
    private static final String BASE_PATH = "/v1/files/";
    private static final String NONEXISTENT_FILE_PATH = "l.png";
    private static final String APPLICATION_OCTET_STREAM_CONTENT_TYPE = "application/octet-stream";

    @Test
    @DisplayName("Create bucket for given name.")
    void testSuccessfulCreateBucket() {

        given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .post(BASE_PATH + "bucket/" + BUCKET_NAME)
                .then().log().all().statusCode(201);

    }

    @Test
    @DisplayName("Test createBucket method for Bad Request")
    void testCreateBucketForBadRequest() {
        given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .post(BASE_PATH + "bucket/" + NOT_ALLOWED_BUCKET_NAME)
                .then().log().all().statusCode(400);
    }

    @Test
    @DisplayName("Uploads a jpg file")
    void testSuccessfulUploadJPGFile() {

        File sampleFile = new File(SAMPLE_FILE_PATH);
        Response putResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .multiPart(FORM_PARAM_FILE, sampleFile)
                .when()
                .put(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH);
        putResponse.then().statusCode(201);
        FileInfo file = putResponse.as(FileInfo.class);
        assertEquals(MINIO_FILE_PATH, file.getPath());
        assertEquals(SAMPLE_FILE_TYPE, file.getContentType());
        assertEquals(BUCKET_NAME, file.getBucket());
    }

    @Test
    @DisplayName("Test upload of a file of an indeterminate content type and see if it defaults to application/octet-stream")
    void testSuccessfulUploadUnknownFile() {
        File unknownFile = new File(UNKNOWN_FILE_PATH);
        Response putResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .multiPart(FORM_PARAM_FILE, unknownFile)
                .when()
                .put(BASE_PATH + BUCKET_NAME + "/" + MINIO_UNKNOWN_FILE_PATH);
        putResponse.then().statusCode(201);
        FileInfo file = putResponse.as(FileInfo.class);
        assertEquals(MINIO_UNKNOWN_FILE_PATH, file.getPath());
        assertEquals(APPLICATION_OCTET_STREAM_CONTENT_TYPE, file.getContentType());
        assertEquals(BUCKET_NAME, file.getBucket());
    }

    @Test
    @DisplayName("Downloads an already uploaded jpg file")
    void testSuccessfulDownloadJPGFile() throws IOException {
        File sampleFile = new File(SAMPLE_FILE_PATH);
        try (InputStream is = new BufferedInputStream(new FileInputStream(sampleFile))) {
            byte[] fileBytes = is.readAllBytes();
            Response putResponse = given().auth()
                    .oauth2(keycloakTestClient.getClientAccessToken(USER))
                    .multiPart(FORM_PARAM_FILE, sampleFile)
                    .when()
                    .put(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH);
            putResponse.then().statusCode(201);
        }
    }

    @Test
    @DisplayName("Returns internal server error when downloading a file that does not exist")
    void testFailedDownloadJPGFile() {
        Response getResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .when()
                .get(BASE_PATH + BUCKET_NAME + "/" + NONEXISTENT_FILE_PATH).andReturn();
        getResponse.then().statusCode(500);
    }

    @Test
    @DisplayName("Returns a bad request error when the bucket name contains unallowed characters")
    void testFailedUploadJPGFile() {
        File sampleFile = new File(SAMPLE_FILE_PATH);
        Response putResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .multiPart(FORM_PARAM_FILE, sampleFile)
                .when()
                .put(BASE_PATH + NOT_ALLOWED_BUCKET_NAME + "/" + MINIO_FILE_PATH);
        putResponse.then().statusCode(400);
    }

    @Test
    @DisplayName("Overrides an already uploaded jpg file with another")
    void testSuccessfulModifyJPGFile() throws IOException {
        File sampleFile = new File(SAMPLE_FILE_PATH);
        try (InputStream isBefore = new BufferedInputStream(new FileInputStream(sampleFile))) {
            byte[] fileBytesBefore = isBefore.readAllBytes();
            File sampleFile2 = new File(SAMPLE2_FILE_PATH);
            try (InputStream isAfter = new BufferedInputStream(new FileInputStream(sampleFile2))) {
                byte[] fileBytesAfter = isAfter.readAllBytes();
                Response putResponse = given().auth()
                        .oauth2(keycloakTestClient.getClientAccessToken(USER))
                        .multiPart(FORM_PARAM_FILE, sampleFile)
                        .when()
                        .put(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH);
                putResponse.then().statusCode(201);
                Response getResponseBefore = given().auth()
                        .oauth2(keycloakTestClient.getClientAccessToken(USER))
                        .when()
                        .get(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH).andReturn();
                byte[] downloadedBytesBefore = getResponseBefore.asByteArray();
                Response putResponseAfter = given().auth()
                        .oauth2(keycloakTestClient.getClientAccessToken(USER))
                        .multiPart(FORM_PARAM_FILE, sampleFile2)
                        .when()
                        .put(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH);
                putResponseAfter.then().statusCode(201);
            }
        }
    }

    @Test
    @DisplayName("Returns a bad request error on attempting to upload a 0 bytes file")
    void testFailedUploadBlankFile() {
        File sampleFile = new File(BLANK_FILE_PATH);
        Response putResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .multiPart(FORM_PARAM_FILE, sampleFile)
                .when()
                .put(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH);
        putResponse.then().statusCode(201);
    }

    @Test
    @DisplayName("Deletes an already uploaded jpg file")
    void testSuccessfulDeleteJPGFile() throws IOException {
        File sampleFile = new File(SAMPLE_FILE_PATH);
        Response putResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .multiPart(FORM_PARAM_FILE, sampleFile)
                .when()
                .put(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH);
        putResponse.then().statusCode(201);
        Response deleteResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .when()
                .delete(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH).andReturn();
        deleteResponse.then().statusCode(201);
    }

    @Test
    @DisplayName("Returns a not found error on attempting to delete a nonexistent file")
    void testFailedDeleteNonexistentFile() throws IOException {
        Response deleteResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .when()
                .delete(BASE_PATH + BUCKET_NAME + "/" + NONEXISTENT_FILE_PATH).andReturn();
        deleteResponse.then().statusCode(404);
    }
}
