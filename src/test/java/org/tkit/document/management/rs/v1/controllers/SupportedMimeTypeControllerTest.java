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
import org.tkit.document.management.rs.v1.models.RFCProblemDTO;
import org.tkit.document.management.rs.v1.models.SupportedMimeTypeCreateUpdateDTO;
import org.tkit.document.management.rs.v1.models.SupportedMimeTypeDTO;
import org.tkit.document.management.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

@QuarkusTest
@WithDBData(value = { "document-management-test-data.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
public class SupportedMimeTypeControllerTest extends AbstractTest {

    private static final String BASE_PATH = "/v1/supported-mime-type";
    private static final String EXISTING_SUPPORTED_MIME_TYPE_ID = "151";
    private static final String EXISTING_SUPPORTED_MIME_TYPE_DELETE_ID = "153";
    private static final String NOT_EXISTING_SUPPORTED_MIME_TYPE_ID = "10000";
    private static final String NAME_OF_SUPPORTED_MIME_TYPE_1 = "application/msexcel";
    private static final String DESCRIPTION_OF_SUPPORTED_MIME_TYPE_1 = "Microsoft Excel";

    @Test
    @DisplayName("Saves supported mime-type with the required fields with validated data.")
    public void testSuccessfulCreateSupportedMimeType() {
        final String supportedMimeTypeName = "SUPPORTED_MIME_TYPE_NAME";
        final String supportedMimeTypeDescription = "SUPPORTED_MIME_TYPE_DESCRIPTION";
        SupportedMimeTypeCreateUpdateDTO supportedMimeTypeCreateDTO = new SupportedMimeTypeCreateUpdateDTO();
        supportedMimeTypeCreateDTO.setName(supportedMimeTypeName);
        supportedMimeTypeCreateDTO.setDescription(supportedMimeTypeDescription);

        Response postResponse = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(supportedMimeTypeCreateDTO)
                .when()
                .post(BASE_PATH);
        postResponse.then().statusCode(CREATED.getStatusCode());

        SupportedMimeTypeDTO dto = postResponse.as(SupportedMimeTypeDTO.class);
        assertThat(dto.getName()).isEqualTo(supportedMimeTypeCreateDTO.getName());
        assertThat(dto.getDescription()).isEqualTo(supportedMimeTypeCreateDTO.getDescription());
    }

    @Test
    @DisplayName("Saves supported mime-type without description.")
    public void testSuccessfulCreateSupportedMimeTypeWithoutDescription() {
        final String supportedMimeTypeName = "SUPPORTED_MIME_TYPE_NAME";
        SupportedMimeTypeCreateUpdateDTO supportedMimeTypeCreateDTO = new SupportedMimeTypeCreateUpdateDTO();
        supportedMimeTypeCreateDTO.setName(supportedMimeTypeName);
        supportedMimeTypeCreateDTO.setDescription(null);

        Response postResponse = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(supportedMimeTypeCreateDTO)
                .when()
                .post(BASE_PATH);
        postResponse.then().statusCode(CREATED.getStatusCode());

        SupportedMimeTypeDTO dto = postResponse.as(SupportedMimeTypeDTO.class);
        assertThat(dto.getName()).isEqualTo(supportedMimeTypeCreateDTO.getName());
    }

    @Test
    @DisplayName("Saves supported mime-type without name.")
    public void testFailedCreateSupportedMimeTypeWithoutName() {
        final String supportedMimeTypeDescription = "SUPPORTED_MIME_TYPE_DESCRIPTION";
        SupportedMimeTypeCreateUpdateDTO supportedMimeTypeCreateDTO = new SupportedMimeTypeCreateUpdateDTO();
        supportedMimeTypeCreateDTO.setName(null);
        supportedMimeTypeCreateDTO.setDescription(supportedMimeTypeDescription);

        Response postResponse = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(supportedMimeTypeCreateDTO)
                .when()
                .post(BASE_PATH);
        postResponse.then().statusCode(BAD_REQUEST.getStatusCode());

        RFCProblemDTO rfcProblemDTO = postResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("createSupportedMimeType.dto.name: must not be blank");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo(ValidationExceptionToRFCProblemMapper.TECHNICAL_ERROR);
        assertThat(rfcProblemDTO.getType())
                .isEqualTo(ValidationExceptionToRFCProblemMapper.RFCProblemType.VALIDATION_EXCEPTION.toString());
    }

    @Test
    @DisplayName("Deletes supported mime-type by id.")
    public void testSuccessfulDeleteSupportedMimeTypeById() {
        Response deleteResponse = given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .delete(BASE_PATH + "/" + EXISTING_SUPPORTED_MIME_TYPE_DELETE_ID);
        deleteResponse.then().statusCode(NO_CONTENT.getStatusCode());

        Response getResponse = given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH);
        getResponse.then().statusCode(OK.getStatusCode());

        List<SupportedMimeTypeDTO> supportedMimeTypeDTOS = getResponse.as(getSupportedMimeTypeDTOTypeRef());
        assertThat(supportedMimeTypeDTOS.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns exception when trying to delete supported mime-type assigned to the attachment.")
    public void testFailedDeleteSupportedMimeTypeWithAssignedId() {
        Response deleteResponse = given()
                .when()
                .delete(BASE_PATH + "/" + EXISTING_SUPPORTED_MIME_TYPE_ID);
        deleteResponse.then().statusCode(BAD_REQUEST.getStatusCode());

        RFCProblemDTO rfcProblemDTO = deleteResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("You cannot delete supported mime-type" +
                " with id " + EXISTING_SUPPORTED_MIME_TYPE_ID + ". It is assigned to the attachment.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo(ExceptionToRFCProblemMapper.TECHNICAL_ERROR);
        assertThat(rfcProblemDTO.getType()).isEqualTo(ExceptionToRFCProblemMapper.RFCProblemType.REST_EXCEPTION.toString());
    }

    @Test
    @DisplayName("Returns exception when trying to delete supported mime-type for a nonexistent id.")
    public void testFailedDeleteSupportedMimeTypeById() {
        Response deleteResponse = given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .delete(BASE_PATH + "/" + NOT_EXISTING_SUPPORTED_MIME_TYPE_ID);
        deleteResponse.then().statusCode(NOT_FOUND.getStatusCode());

        RFCProblemDTO rfcProblemDTO = deleteResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("The supported mime-type with id "
                + NOT_EXISTING_SUPPORTED_MIME_TYPE_ID + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo(ExceptionToRFCProblemMapper.TECHNICAL_ERROR);
        assertThat(rfcProblemDTO.getType()).isEqualTo(ExceptionToRFCProblemMapper.RFCProblemType.REST_EXCEPTION.toString());
    }

    @Test
    @DisplayName("Updates name and description in supported mime-type.")
    public void testSuccessfulUpdateSupportedMimeType() {
        final String supportedMimeTypeName = "TEST_UPDATE_SUPPORTED_MIME_TYPE_NAME";
        final String supportedMimeTypeDescription = "TEST_UPDATE_SUPPORTED_MIME_TYPE_DESCRIPTION";
        SupportedMimeTypeCreateUpdateDTO supportedMimeTypeUpdateDTO = new SupportedMimeTypeCreateUpdateDTO();
        supportedMimeTypeUpdateDTO.setName(supportedMimeTypeName);
        supportedMimeTypeUpdateDTO.setDescription(supportedMimeTypeDescription);

        Response putResponse = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(supportedMimeTypeUpdateDTO)
                .when()
                .put(BASE_PATH + "/" + EXISTING_SUPPORTED_MIME_TYPE_ID);
        putResponse.then().statusCode(OK.getStatusCode());

        SupportedMimeTypeDTO dto = putResponse.as(SupportedMimeTypeDTO.class);
        assertThat(dto.getId()).isEqualTo(EXISTING_SUPPORTED_MIME_TYPE_ID);
        assertThat(dto.getName()).isEqualTo(supportedMimeTypeName);
        assertThat(dto.getDescription()).isEqualTo(supportedMimeTypeDescription);
    }

    @Test
    @DisplayName("Returns exception when trying to update supported mime-type for a nonexistent id.")
    public void testFailedUpdateSupportedMimeTypeById() {
        final String supportedMimeTypeName = "TEST_UPDATE_SUPPORTED_MIME_TYPE_NAME";
        final String supportedMimeTypeDescription = "TEST_UPDATE_SUPPORTED_MIME_TYPE_DESCRIPTION";
        SupportedMimeTypeCreateUpdateDTO supportedMimeTypeUpdateDTO = new SupportedMimeTypeCreateUpdateDTO();
        supportedMimeTypeUpdateDTO.setName(supportedMimeTypeName);
        supportedMimeTypeUpdateDTO.setDescription(supportedMimeTypeDescription);

        Response putResponse = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(supportedMimeTypeUpdateDTO)
                .when()
                .put(BASE_PATH + "/" + NOT_EXISTING_SUPPORTED_MIME_TYPE_ID);
        putResponse.then().statusCode(NOT_FOUND.getStatusCode());

        RFCProblemDTO rfcProblemDTO = putResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("The supported mime-type with id "
                + NOT_EXISTING_SUPPORTED_MIME_TYPE_ID + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo(ExceptionToRFCProblemMapper.TECHNICAL_ERROR);
        assertThat(rfcProblemDTO.getType()).isEqualTo(ExceptionToRFCProblemMapper.RFCProblemType.REST_EXCEPTION.toString());
    }

    @Test
    @DisplayName("Gets all supported mime-types.")
    public void testSuccessfulGetAllSupportedMimeTypes() {
        Response getResponse = given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH);
        getResponse.then().statusCode(OK.getStatusCode());

        List<SupportedMimeTypeDTO> typesOfDocuments = getResponse.as(getSupportedMimeTypeDTOTypeRef());
        assertThat(typesOfDocuments.size()).isEqualTo(3);
        assertThat(typesOfDocuments.get(0).getId()).isEqualTo(EXISTING_SUPPORTED_MIME_TYPE_ID);
        assertThat(typesOfDocuments.get(0).getName()).isEqualTo(NAME_OF_SUPPORTED_MIME_TYPE_1);
        assertThat(typesOfDocuments.get(0).getDescription()).isEqualTo(DESCRIPTION_OF_SUPPORTED_MIME_TYPE_1);
    }

    @Test
    @DisplayName("Returns supported mime type by id.")
    public void testSuccessfulGetSupportedMimeType() {
        Response response = given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH + "/" + EXISTING_SUPPORTED_MIME_TYPE_ID);

        response.then().statusCode(200);
        SupportedMimeTypeDTO supportedMimeTypeDTO = response.as(SupportedMimeTypeDTO.class);

        assertThat(supportedMimeTypeDTO.getId()).isEqualTo(EXISTING_SUPPORTED_MIME_TYPE_ID);
        assertThat(supportedMimeTypeDTO.getName()).isEqualTo(NAME_OF_SUPPORTED_MIME_TYPE_1);
        assertThat(supportedMimeTypeDTO.getDescription()).isEqualTo(DESCRIPTION_OF_SUPPORTED_MIME_TYPE_1);
    }

    @Test
    @DisplayName("Returns exception when trying to get supported mime type for a nonexistent id.")
    public void testFailedGetSupportedMimeType() {
        Response response = given()
                .when()
                .get(BASE_PATH + "/" + NOT_EXISTING_SUPPORTED_MIME_TYPE_ID);

        response.then().statusCode(NOT_FOUND.getStatusCode());
        RFCProblemDTO rfcProblemDTO = response.as(RFCProblemDTO.class);

        assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("404");
        assertThat(rfcProblemDTO.getDetail())
                .isEqualTo("The supported mime-type with id " + NOT_EXISTING_SUPPORTED_MIME_TYPE_ID + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo("TECHNICAL ERROR");
        assertThat(rfcProblemDTO.getType()).isEqualTo("REST_EXCEPTION");
    }

    private TypeRef<List<SupportedMimeTypeDTO>> getSupportedMimeTypeDTOTypeRef() {
        return new TypeRef<>() {
        };
    }
}
