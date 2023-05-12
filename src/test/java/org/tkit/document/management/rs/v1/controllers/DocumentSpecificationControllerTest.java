package org.tkit.document.management.rs.v1.controllers;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tkit.document.management.rs.v1.ExceptionToRFCProblemMapper;
import org.tkit.document.management.rs.v1.ValidationExceptionToRFCProblemMapper;
import org.tkit.document.management.rs.v1.models.DocumentSpecificationCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.DocumentSpecificationDTO;
import org.tkit.document.management.rs.v1.models.RFCProblemDTO;
import org.tkit.document.management.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

@QuarkusTest
@WithDBData(value = { "document-management-test-data.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
public class DocumentSpecificationControllerTest extends AbstractTest {

    private static final String BASE_PATH = "/v1/document-specification";
    private static final String EXISTING_DOCUMENT_SPECIFICATION_ID = "251";
    private static final String EXISTING_DOCUMENT_SPECIFICATION_DELETE_ID = "253";
    private static final String NOT_EXISTING_DOCUMENT_SPECIFICATION_ID = "10000";
    private static final String NAME_OF_DOCUMENT_SPECIFICATION_1 = "specification_1";
    private static final String VERSION_OF_DOCUMENT_SPECIFICATION_1 = "v_1";

    @Test
    @DisplayName("Saves specification of document with the required fields with validated data.")
    public void testSuccessfulCreateDocumentSpecification() {
        final String documentSpecificationName = "DOCUMENT_SPECIFICATION_NAME";
        final String documentSpecificationVersion = "DOCUMENT_SPECIFICATION_VERSION";
        DocumentSpecificationCreateUpdateDTO documentSpecificationCreateDTO = new DocumentSpecificationCreateUpdateDTO();
        documentSpecificationCreateDTO.setName(documentSpecificationName);
        documentSpecificationCreateDTO.setSpecificationVersion(documentSpecificationVersion);

        Response postResponse = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentSpecificationCreateDTO)
                .when()
                .post(BASE_PATH);
        postResponse.then().statusCode(CREATED.getStatusCode());

        DocumentSpecificationDTO dto = postResponse.as(DocumentSpecificationDTO.class);
        assertThat(dto.getName()).isEqualTo(documentSpecificationCreateDTO.getName());
        assertThat(dto.getSpecificationVersion()).isEqualTo(documentSpecificationCreateDTO.getSpecificationVersion());
    }

    @Test
    @DisplayName("Saves specification of document without version.")
    public void testSuccessfulCreateDocumentSpecificationWithoutVersion() {
        final String documentSpecificationName = "DOCUMENT_SPECIFICATION_NAME";
        DocumentSpecificationCreateUpdateDTO documentSpecificationCreateDTO = new DocumentSpecificationCreateUpdateDTO();
        documentSpecificationCreateDTO.setName(documentSpecificationName);
        documentSpecificationCreateDTO.setSpecificationVersion(null);

        Response postResponse = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentSpecificationCreateDTO)
                .when()
                .post(BASE_PATH);
        postResponse.then().statusCode(CREATED.getStatusCode());

        DocumentSpecificationDTO dto = postResponse.as(DocumentSpecificationDTO.class);
        assertThat(dto.getName()).isEqualTo(documentSpecificationCreateDTO.getName());
    }

    @Test
    @DisplayName("Saves specification of document without name.")
    public void testFailedCreateDocumentSpecificationWithoutName() {
        final String documentSpecificationVersion = "DOCUMENT_SPECIFICATION_VERSION";
        DocumentSpecificationCreateUpdateDTO documentSpecificationCreateDTO = new DocumentSpecificationCreateUpdateDTO();
        documentSpecificationCreateDTO.setName(null);
        documentSpecificationCreateDTO.setSpecificationVersion(documentSpecificationVersion);

        Response postResponse = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentSpecificationCreateDTO)
                .when()
                .post(BASE_PATH);
        postResponse.then().statusCode(BAD_REQUEST.getStatusCode());

        RFCProblemDTO rfcProblemDTO = postResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("createDocumentSpecification.dto.name: must not be blank");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo(ValidationExceptionToRFCProblemMapper.TECHNICAL_ERROR);
        assertThat(rfcProblemDTO.getType())
                .isEqualTo(ValidationExceptionToRFCProblemMapper.RFCProblemType.VALIDATION_EXCEPTION.toString());
    }

    @Test
    @DisplayName("Deletes specification of document by id.")
    public void testSuccessfulDeleteSupportedMimeTypeById() {
        Response deleteResponse = given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .delete(BASE_PATH + "/" + EXISTING_DOCUMENT_SPECIFICATION_DELETE_ID);
        deleteResponse.then().statusCode(NO_CONTENT.getStatusCode());

        Response getResponse = given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH);
        getResponse.then().statusCode(OK.getStatusCode());

        List<DocumentSpecificationDTO> documentSpecificationDTOS = getResponse.as(getDocumentSpecificationDTOTypeRef());
        assertThat(documentSpecificationDTOS.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns exception when trying to delete specification of document assigned to the document.")
    public void testFailedDeleteDocumentSpecificationWithAssignedId() {
        Response deleteResponse = given()
                .when()
                .delete(BASE_PATH + "/" + EXISTING_DOCUMENT_SPECIFICATION_ID);
        deleteResponse.then().statusCode(BAD_REQUEST.getStatusCode());

        RFCProblemDTO rfcProblemDTO = deleteResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("You cannot delete specification of document" +
                " with id " + EXISTING_DOCUMENT_SPECIFICATION_ID + ". It is assigned to the document.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo(ExceptionToRFCProblemMapper.TECHNICAL_ERROR);
        assertThat(rfcProblemDTO.getType()).isEqualTo(ExceptionToRFCProblemMapper.RFCProblemType.REST_EXCEPTION.toString());
    }

    @Test
    @DisplayName("Returns exception when trying to delete specification of document for a nonexistent id.")
    public void testFailedDeleteDocumentSpecificationById() {
        Response deleteResponse = given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .delete(BASE_PATH + "/" + NOT_EXISTING_DOCUMENT_SPECIFICATION_ID);
        deleteResponse.then().statusCode(NOT_FOUND.getStatusCode());

        RFCProblemDTO rfcProblemDTO = deleteResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("The document specification with id "
                + NOT_EXISTING_DOCUMENT_SPECIFICATION_ID + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo(ExceptionToRFCProblemMapper.TECHNICAL_ERROR);
        assertThat(rfcProblemDTO.getType()).isEqualTo(ExceptionToRFCProblemMapper.RFCProblemType.REST_EXCEPTION.toString());
    }

    @Test
    @DisplayName("Updates name and version in specification of document.")
    public void testSuccessfulUpdateDocumentSpecification() {
        final String documentSpecificationName = "TEST_UPDATE_SUPPORTED_MIME_TYPE_NAME";
        final String documentSpecificationVersion = "TEST_UPDATE_SUPPORTED_MIME_TYPE_DESCRIPTION";
        DocumentSpecificationCreateUpdateDTO documentSpecificationUpdateDTO = new DocumentSpecificationCreateUpdateDTO();
        documentSpecificationUpdateDTO.setName(documentSpecificationName);
        documentSpecificationUpdateDTO.setSpecificationVersion(documentSpecificationVersion);

        Response putResponse = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentSpecificationUpdateDTO)
                .when()
                .put(BASE_PATH + "/" + EXISTING_DOCUMENT_SPECIFICATION_ID);
        putResponse.then().statusCode(OK.getStatusCode());

        DocumentSpecificationDTO dto = putResponse.as(DocumentSpecificationDTO.class);
        assertThat(dto.getId()).isEqualTo(EXISTING_DOCUMENT_SPECIFICATION_ID);
        assertThat(dto.getName()).isEqualTo(documentSpecificationUpdateDTO.getName());
        assertThat(dto.getSpecificationVersion()).isEqualTo(documentSpecificationUpdateDTO.getSpecificationVersion());
    }

    @Test
    @DisplayName("Returns exception when trying to update specification of document for a nonexistent id.")
    public void testFailedUpdateDocumentSpecificationById() {
        final String documentSpecificationName = "TEST_UPDATE_SUPPORTED_MIME_TYPE_NAME";
        final String documentSpecificationVersion = "TEST_UPDATE_SUPPORTED_MIME_TYPE_DESCRIPTION";
        DocumentSpecificationCreateUpdateDTO documentSpecificationUpdateDTO = new DocumentSpecificationCreateUpdateDTO();
        documentSpecificationUpdateDTO.setName(documentSpecificationName);
        documentSpecificationUpdateDTO.setSpecificationVersion(documentSpecificationVersion);

        Response putResponse = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentSpecificationUpdateDTO)
                .when()
                .put(BASE_PATH + "/" + NOT_EXISTING_DOCUMENT_SPECIFICATION_ID);
        putResponse.then().statusCode(NOT_FOUND.getStatusCode());

        RFCProblemDTO rfcProblemDTO = putResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("The document specification with id "
                + NOT_EXISTING_DOCUMENT_SPECIFICATION_ID + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo(ExceptionToRFCProblemMapper.TECHNICAL_ERROR);
        assertThat(rfcProblemDTO.getType()).isEqualTo(ExceptionToRFCProblemMapper.RFCProblemType.REST_EXCEPTION.toString());
    }

    @Test
    @DisplayName("Gets all specifications of document.")
    public void testSuccessfulGetAllDocumentSpecifications() {
        Response getResponse = given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH);
        getResponse.then().statusCode(OK.getStatusCode());

        List<DocumentSpecificationDTO> documentSpecificationDTOS = getResponse.as(getDocumentSpecificationDTOTypeRef());
        assertThat(documentSpecificationDTOS.size()).isEqualTo(3);
        assertThat(documentSpecificationDTOS.get(0).getId()).isEqualTo(EXISTING_DOCUMENT_SPECIFICATION_ID);
        assertThat(documentSpecificationDTOS.get(0).getName()).isEqualTo(NAME_OF_DOCUMENT_SPECIFICATION_1);
        assertThat(documentSpecificationDTOS.get(0).getSpecificationVersion()).isEqualTo(VERSION_OF_DOCUMENT_SPECIFICATION_1);
    }

    @Test
    @DisplayName("Returns document specification by id.")
    public void testSuccessfulGetDocumentSpecification() {
        Response response = given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH + "/" + EXISTING_DOCUMENT_SPECIFICATION_ID);

        response.then().statusCode(200);
        DocumentSpecificationDTO documentSpecificationDTO = response.as(DocumentSpecificationDTO.class);

        assertThat(documentSpecificationDTO.getId()).isEqualTo(EXISTING_DOCUMENT_SPECIFICATION_ID);
        assertThat(documentSpecificationDTO.getName()).isEqualTo(NAME_OF_DOCUMENT_SPECIFICATION_1);
        assertThat(documentSpecificationDTO.getSpecificationVersion()).isEqualTo(VERSION_OF_DOCUMENT_SPECIFICATION_1);
    }

    @Test
    @DisplayName("Returns exception when trying to get document specification for a nonexistent id.")
    public void testFailedGetDocumentSpecification() {
        Response response = given()
                .when()
                .get(BASE_PATH + "/" + NOT_EXISTING_DOCUMENT_SPECIFICATION_ID);

        response.then().statusCode(NOT_FOUND.getStatusCode());
        RFCProblemDTO rfcProblemDTO = response.as(RFCProblemDTO.class);

        assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("404");
        assertThat(rfcProblemDTO.getDetail())
                .isEqualTo("The document specification with id " + NOT_EXISTING_DOCUMENT_SPECIFICATION_ID + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo("TECHNICAL ERROR");
        assertThat(rfcProblemDTO.getType()).isEqualTo("REST_EXCEPTION");
    }

    private TypeRef<List<DocumentSpecificationDTO>> getDocumentSpecificationDTOTypeRef() {
        return new TypeRef<>() {
        };
    }
}
