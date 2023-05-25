package org.tkit.document.management.rs.v1.controllers;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.*;

import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tkit.document.management.rs.v1.models.FileInfoDTO;
import org.tkit.document.management.test.AbstractTest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;

@QuarkusTest
public class FileControllerTest extends AbstractTest {

    private static final String SAMPLE_FILE_PATH = "src/test/resources/sample.jpg";
    private static final String SAMPLE2_FILE_PATH = "src/test/resources/sample2.jpg";
    private static final String BLANK_FILE_PATH = "src/test/resources/empty_file.txt";
    private static final String UNKNOWN_FILE_PATH = "src/test/resources/unknown_content_type_file";
    private static final String SAMPLE_FILE_TYPE = "image/jpeg";
    private static final String MINIO_FILE_PATH = "folderA/a.jpg";
    private static final String MINIO_UNKNOWN_FILE_PATH = "folderA/unknown_content_type_file";
    private static final String BUCKET_NAME = "test-bucket";
    private static final String NOT_ALLOWED_BUCKET_NAME = "test_bucket";
    private static final String FORM_PARAM_FILE = "file";
    private static final String BASE_PATH = "/v1/files/";
    private static final String NON_EXISTING_FILE_PATH = "l.png";
    private static final String APPLICATION_OCTET_STREAM_CONTENT_TYPE = "application/octet-stream";
    private static final String prefix = "fs-prod-";

    @Test
    @DisplayName("Create bucket for given name.")
    void testCreateBucketTest() {

        given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .post(BASE_PATH + "bucket/" + BUCKET_NAME)
                .then().log().all().statusCode(201);

    }

    @Test
    @DisplayName("Uploads jpg file")
    public void testSuccessfulUploadJPGFileTest() {

        File sampleFile = new File(SAMPLE_FILE_PATH);
        Response putResponse = given()
                .multiPart(FORM_PARAM_FILE, sampleFile)
                .when()
                .put(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH);
        putResponse.then().statusCode(201);
        FileInfoDTO file = putResponse.as(FileInfoDTO.class);
        assertEquals(MINIO_FILE_PATH, file.getPath());
        assertEquals(SAMPLE_FILE_TYPE, file.getContentType());
        assertEquals(prefix + BUCKET_NAME, file.getBucket());
    }

    @Test
    @DisplayName("Test upload of a file of an indeterminate content type and see if it defaults to application/octet-stream")
    void testSuccessfulUploadUnknownFile() {
        File unknownFile = new File(UNKNOWN_FILE_PATH);
        Response putResponse = given()
                .multiPart(FORM_PARAM_FILE, unknownFile)
                .when()
                .put(BASE_PATH + BUCKET_NAME + "/" + MINIO_UNKNOWN_FILE_PATH);
        putResponse.then().statusCode(201);
        FileInfoDTO file = putResponse.as(FileInfoDTO.class);
        assertEquals(MINIO_UNKNOWN_FILE_PATH, file.getPath());
        assertEquals(APPLICATION_OCTET_STREAM_CONTENT_TYPE, file.getContentType());
        assertEquals(prefix + BUCKET_NAME, file.getBucket());
    }

    @Test
    @DisplayName("Downloads an already uploaded jpg file")
    void testSuccessfulDownloadJPGFile() throws IOException {
        File sampleFile = new File(SAMPLE_FILE_PATH);
        InputStream is = new BufferedInputStream(new FileInputStream(sampleFile));
        byte[] fileBytes = is.readAllBytes();
        Response putResponse = given()
                .multiPart(FORM_PARAM_FILE, sampleFile)
                .when()
                .put(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH);
        putResponse.then().statusCode(201);
        Response getResponse = given()
                .when()
                .get(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH).andReturn();
        byte[] downloadedBytes = getResponse.asByteArray();
        assertArrayEquals(fileBytes, downloadedBytes);
    }

    @Test
    @DisplayName("Returns internal server error when getting file that does not exist")
    public void testFailedDownloadJPGFile() {
        Response getResponse = given()
                .when()
                .get(BASE_PATH + BUCKET_NAME + "/" + NON_EXISTING_FILE_PATH).andReturn();
        getResponse.then().statusCode(500);
    }

    @Test
    @DisplayName("Returns bad request when bucket contains not allowed characters")
    public void testFailedUploadJPGFile() {
        File sampleFile = new File(SAMPLE_FILE_PATH);
        Response putResponse = given()
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
                Response putResponse = given()
                        .multiPart(FORM_PARAM_FILE, sampleFile)
                        .when()
                        .put(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH);
                putResponse.then().statusCode(201);
                Response getResponseBefore = given()
                        .when()
                        .get(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH).andReturn();
                byte[] downloadedBytesBefore = getResponseBefore.asByteArray();
                Response putResponseAfter = given()
                        .multiPart(FORM_PARAM_FILE, sampleFile2)
                        .when()
                        .put(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH);
                putResponseAfter.then().statusCode(201);
                Response getResponseAfter = given()
                        .when()
                        .get(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH).andReturn();
                byte[] downloadedBytesAfter = getResponseAfter.asByteArray();
                assertArrayEquals(fileBytesBefore, downloadedBytesBefore);
                assertArrayEquals(fileBytesAfter, downloadedBytesAfter);
            }
        }
    }

    @Test
    @DisplayName("Returns a bad request error on attempting to upload a 0 bytes file")
    void testFailedUploadBlankFile() {
        File sampleFile = new File(BLANK_FILE_PATH);
        Response putResponse = given()
                .multiPart(FORM_PARAM_FILE, sampleFile)
                .when()
                .put(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH);
        putResponse.then().statusCode(400);
    }

    @Test
    @DisplayName("Deletes an already uploaded jpg file")
    void testSuccessfulDeleteJPGFile() throws IOException {
        File sampleFile = new File(SAMPLE_FILE_PATH);
        Response putResponse = given()
                .multiPart(FORM_PARAM_FILE, sampleFile)
                .when()
                .put(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH);
        putResponse.then().statusCode(201);
        Response deleteResponse = given()
                .when()
                .delete(BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH).andReturn();
        deleteResponse.then().statusCode(201);
    }

    @Test
    @DisplayName("Returns a not found error on attempting to delete a nonexistent file")
    void testFailedDeleteNonexistentFile() throws IOException {
        Response deleteResponse = given()
                .when()
                .delete(BASE_PATH + BUCKET_NAME + "/" + NON_EXISTING_FILE_PATH).andReturn();
        deleteResponse.then().statusCode(404);
    }
}
