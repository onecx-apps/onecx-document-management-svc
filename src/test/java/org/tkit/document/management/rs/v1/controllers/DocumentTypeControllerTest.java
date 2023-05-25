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
import org.tkit.document.management.rs.v1.models.DocumentTypeCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.DocumentTypeDTO;
import org.tkit.document.management.rs.v1.models.RFCProblemDTO;
import org.tkit.document.management.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

@QuarkusTest
@WithDBData(value = { "document-management-test-data.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
public class DocumentTypeControllerTest extends AbstractTest {

    private static final String BASE_PATH = "/v1/document-type";
    private static final String EXISTING_DOCUMENT_TYPE_ID = "201";
    private static final String EXISTING_DOCUMENT_TYPE_DELETE_ID = "203";
    private static final String NOT_EXISTING_DOCUMENT_TYPE_ID = "1000";
    private static final String NAME_OF_DOCUMENT_TYPE_1 = "invoice";
    private static final Object[] EXISTING_DOCUMENT_TYPE_IDS = { "201", "202", "203" };
    private static final Object[] EXISTING_DOCUMENT_TYPE_NAMES = { "invoice", "exploration protocol",
            "nonassigned" };

    @Test
    @DisplayName("Saves type of document with the required fields with validated data.")
    public void testSuccessfulCreateDocumentType() {
        final String testDocumentTypeName = "DOCUMENT_TYPE_1";
        DocumentTypeCreateUpdateDTO documentTypeCreateDTO = new DocumentTypeCreateUpdateDTO();
        documentTypeCreateDTO.setName(testDocumentTypeName);

        Response postResponse = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentTypeCreateDTO)
                .when()
                .post(BASE_PATH);
        postResponse.then().statusCode(CREATED.getStatusCode());

        DocumentTypeDTO dto = postResponse.as(DocumentTypeDTO.class);
        assertThat(dto.getName()).isEqualTo(documentTypeCreateDTO.getName());
    }

    @Test
    @DisplayName("Saves type of document without name.")
    void testFailedCreateDocumentTypeWithoutName() {
        DocumentTypeCreateUpdateDTO documentTypeCreateDTO = new DocumentTypeCreateUpdateDTO();
        documentTypeCreateDTO.setName(null);

        Response postResponse = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentTypeCreateDTO)
                .when()
                .post(BASE_PATH);
        postResponse.then().statusCode(BAD_REQUEST.getStatusCode());

        RFCProblemDTO rfcProblemDTO = postResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("createDocumentType.dto.name: must not be blank");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo(ValidationExceptionToRFCProblemMapper.TECHNICAL_ERROR);
        assertThat(rfcProblemDTO.getType()).isEqualTo(
                ValidationExceptionToRFCProblemMapper.RFCProblemType.VALIDATION_EXCEPTION.toString());
    }

    @Test
    @DisplayName("Deletes type of document by id")
    public void testSuccessfulDeleteDocumentTypeById() {
        Response deleteResponse = given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .delete(BASE_PATH + "/" + EXISTING_DOCUMENT_TYPE_DELETE_ID);
        deleteResponse.then().statusCode(NO_CONTENT.getStatusCode());

        Response getResponse = given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH);
        getResponse.then().statusCode(OK.getStatusCode());

        List<DocumentTypeDTO> documentTypes = getResponse.as(getDocumentTypeDTOTypeRef());
        assertThat(documentTypes.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns exception when trying to delete type of document assigned to the document.")
    public void testFailedDeleteDocumentTypeWithAssignedId() {
        Response deleteResponse = given()
                .when()
                .delete(BASE_PATH + "/" + EXISTING_DOCUMENT_TYPE_ID);
        deleteResponse.then().statusCode(BAD_REQUEST.getStatusCode());

        RFCProblemDTO rfcProblemDTO = deleteResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("You cannot delete type of document" +
                " with id " + EXISTING_DOCUMENT_TYPE_ID + ". It is assigned to the document.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo(ExceptionToRFCProblemMapper.TECHNICAL_ERROR);
        assertThat(rfcProblemDTO.getType()).isEqualTo(ExceptionToRFCProblemMapper.RFCProblemType.REST_EXCEPTION.toString());
    }

    @Test
    @DisplayName("Returns exception when trying to delete type of document for a nonexistent id.")
    public void testFailedDeleteDocumentTypeById() {
        Response deleteResponse = given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .delete(BASE_PATH + "/" + NOT_EXISTING_DOCUMENT_TYPE_ID);
        deleteResponse.then().statusCode(NOT_FOUND.getStatusCode());

        RFCProblemDTO rfcProblemDTO = deleteResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("The document type with id " + NOT_EXISTING_DOCUMENT_TYPE_ID
                + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo(ExceptionToRFCProblemMapper.TECHNICAL_ERROR);
        assertThat(rfcProblemDTO.getType()).isEqualTo(ExceptionToRFCProblemMapper.RFCProblemType.REST_EXCEPTION.toString());
    }

    @Test
    @DisplayName("Updates name in type of document.")
    public void testSuccessfulUpdateNameInDocumentType() {
        final String documentTypeName = "TEST_UPDATE_DOCUMENT_TYPE_NAME";
        DocumentTypeCreateUpdateDTO documentTypeUpdateDTO = new DocumentTypeCreateUpdateDTO();
        documentTypeUpdateDTO.setName(documentTypeName);

        Response putResponse = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentTypeUpdateDTO)
                .when()
                .put(BASE_PATH + "/" + EXISTING_DOCUMENT_TYPE_ID);
        putResponse.then().statusCode(CREATED.getStatusCode());

        DocumentTypeDTO dto = putResponse.as(DocumentTypeDTO.class);
        assertThat(dto.getId()).isEqualTo(EXISTING_DOCUMENT_TYPE_ID);
        assertThat(dto.getName()).isEqualTo(documentTypeName);
    }

    @Test
    @DisplayName("Returns exception when trying to update type of document for a nonexistent id.")
    void testFailedUpdateDocumentTypeById() {
        final String documentTypeName = "TEST_UPDATE_DOCUMENT_TYPE_NAME";
        DocumentTypeCreateUpdateDTO documentTypeUpdateDTO = new DocumentTypeCreateUpdateDTO();
        documentTypeUpdateDTO.setName(documentTypeName);

        Response putResponse = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentTypeUpdateDTO)
                .when()
                .put(BASE_PATH + "/" + NOT_EXISTING_DOCUMENT_TYPE_ID);
        putResponse.then().statusCode(NOT_FOUND.getStatusCode());

        RFCProblemDTO rfcProblemDTO = putResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("The document type with id "
                + NOT_EXISTING_DOCUMENT_TYPE_ID + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo(ExceptionToRFCProblemMapper.TECHNICAL_ERROR);
        assertThat(rfcProblemDTO.getType())
                .isEqualTo(ExceptionToRFCProblemMapper.RFCProblemType.REST_EXCEPTION.toString());
    }

    @Test
    @DisplayName("Gets all types of document.")
    public void testSuccessfulGetAllTypesOfDocument() {
        Response getResponse = given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH);
        getResponse.then().statusCode(OK.getStatusCode());

        List<DocumentTypeDTO> typesOfDocuments = getResponse.as(getDocumentTypeDTOTypeRef());
        assertThat(typesOfDocuments).hasSize(3);
        assertThat(typesOfDocuments.get(0).getId()).isIn(EXISTING_DOCUMENT_TYPE_IDS);
        assertThat(typesOfDocuments.get(0).getName()).isIn(EXISTING_DOCUMENT_TYPE_NAMES);
        assertThat(typesOfDocuments.get(1).getId()).isIn(EXISTING_DOCUMENT_TYPE_IDS);
        assertThat(typesOfDocuments.get(1).getName()).isIn(EXISTING_DOCUMENT_TYPE_NAMES);
        assertThat(typesOfDocuments.get(2).getId()).isIn(EXISTING_DOCUMENT_TYPE_IDS);
        assertThat(typesOfDocuments.get(2).getName()).isIn(EXISTING_DOCUMENT_TYPE_NAMES);
    }

    @Test
    @DisplayName("Returns document type by id.")
    void testSuccessfulGetDocumentTypeById() {
        Response response = given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH + "/" + EXISTING_DOCUMENT_TYPE_ID);

        response.then().statusCode(200);
        DocumentTypeDTO documentTypeDTO = response.as(DocumentTypeDTO.class);

        assertThat(documentTypeDTO.getId()).isEqualTo(EXISTING_DOCUMENT_TYPE_ID);
        assertThat(documentTypeDTO.getName()).isEqualTo(NAME_OF_DOCUMENT_TYPE_1);
    }

    @Test
    @DisplayName("Returns exception when trying to get document type for a nonexistent id.")
    void testFailedGetDocumentTypeById() {
        Response response = given()
                .when()
                .get(BASE_PATH + "/" + NOT_EXISTING_DOCUMENT_TYPE_ID);

        response.then().statusCode(NOT_FOUND.getStatusCode());
        RFCProblemDTO rfcProblemDTO = response.as(RFCProblemDTO.class);

        assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("404");
        assertThat(rfcProblemDTO.getDetail())
                .isEqualTo("The document type with id " + NOT_EXISTING_DOCUMENT_TYPE_ID
                        + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo("TECHNICAL ERROR");
        assertThat(rfcProblemDTO.getType()).isEqualTo("REST_EXCEPTION");
    }

    private TypeRef<List<DocumentTypeDTO>> getDocumentTypeDTOTypeRef() {
        return new TypeRef<>() {
        };
    }
}
