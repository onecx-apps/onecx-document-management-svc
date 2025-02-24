package org.onecx.document.management.rs.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.onecx.document.management.test.AbstractTest.USER;

import java.util.List;

import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.onecx.document.management.rs.v1.ExceptionToRFCProblemMapper;
import org.onecx.document.management.rs.v1.ValidationExceptionToRFCProblemMapper;
import org.onecx.document.management.rs.v1.models.RFCProblemDTO;
import org.onecx.document.management.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.onecx.document.management.rs.v1.model.SupportedMimeTypeCreateUpdateDTO;
import gen.org.onecx.document.management.rs.v1.model.SupportedMimeTypeDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

@QuarkusTest
@WithDBData(value = { "document-management-test-data.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = USER, scopes = "ocx-doc:read")

class SupportedMimeTypeControllerTest extends AbstractTest {

    private static final String BASE_PATH = "/v1/supported-mime-type";
    private static final String EXISTING_SUPPORTED_MIME_TYPE_ID = "151";
    private static final String EXISTING_SUPPORTED_MIME_TYPE_DELETE_ID = "153";
    private static final String NONEXISTENT_SUPPORTED_MIME_TYPE_ID = "10000";
    private static final String NAME_OF_SUPPORTED_MIME_TYPE_1 = "application/msexcel";
    private static final String DESCRIPTION_OF_SUPPORTED_MIME_TYPE_1 = "Microsoft Excel";
    private static final Object[] EXISTING_SUPPORTED_MIME_TYPE_IDS = { "151", "152", "153" };
    private static final Object[] EXISTING_SUPPORTED_MIME_TYPE_NAMES = { "application/msexcel", "application/pdf",
            "nonassigned" };

    @Test
    @DisplayName("Saves supported mime-type with the required fields with validated data.")
    void testSuccessfulCreateSupportedMimeType() {
        final String supportedMimeTypeName = "SUPPORTED_MIME_TYPE_NAME";
        final String supportedMimeTypeDescription = "SUPPORTED_MIME_TYPE_DESCRIPTION";
        SupportedMimeTypeCreateUpdateDTO supportedMimeTypeCreateDTO = new SupportedMimeTypeCreateUpdateDTO();
        supportedMimeTypeCreateDTO.setName(supportedMimeTypeName);
        supportedMimeTypeCreateDTO.setDescription(supportedMimeTypeDescription);

        Response postResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
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
    void testSuccessfulCreateSupportedMimeTypeWithoutDescription() {
        final String supportedMimeTypeName = "SUPPORTED_MIME_TYPE_NAME";
        SupportedMimeTypeCreateUpdateDTO supportedMimeTypeCreateDTO = new SupportedMimeTypeCreateUpdateDTO();
        supportedMimeTypeCreateDTO.setName(supportedMimeTypeName);
        supportedMimeTypeCreateDTO.setDescription(null);

        Response postResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
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
    void testFailedCreateSupportedMimeTypeWithoutName() {
        final String supportedMimeTypeDescription = "SUPPORTED_MIME_TYPE_DESCRIPTION";
        SupportedMimeTypeCreateUpdateDTO supportedMimeTypeCreateDTO = new SupportedMimeTypeCreateUpdateDTO();
        supportedMimeTypeCreateDTO.setName(null);
        supportedMimeTypeCreateDTO.setDescription(supportedMimeTypeDescription);

        Response postResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(supportedMimeTypeCreateDTO)
                .when()
                .post(BASE_PATH);
        postResponse.then().statusCode(BAD_REQUEST.getStatusCode());

        RFCProblemDTO rfcProblemDTO = postResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(rfcProblemDTO.getDetail())
                .isEqualTo("createSupportedMimeType.supportedMimeTypeCreateUpdateDTO.name: must not be null");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo(ValidationExceptionToRFCProblemMapper.TECHNICAL_ERROR);
        assertThat(rfcProblemDTO.getType())
                .isEqualTo(ValidationExceptionToRFCProblemMapper.RFCProblemType.VALIDATION_EXCEPTION
                        .toString());
    }

    @Test
    @DisplayName("Deletes supported mime-type by id.")
    void testSuccessfulDeleteSupportedMimeTypeById() {
        Response deleteResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .delete(BASE_PATH + "/" + EXISTING_SUPPORTED_MIME_TYPE_DELETE_ID);
        deleteResponse.then().statusCode(NO_CONTENT.getStatusCode());

        Response getResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH);
        getResponse.then().statusCode(OK.getStatusCode());

        List<SupportedMimeTypeDTO> supportedMimeTypeDTOS = getResponse.as(getSupportedMimeTypeDTOTypeRef());
        assertThat(supportedMimeTypeDTOS).hasSize(2);
    }

    @Test
    @DisplayName("Returns exception when trying to delete supported mime-type assigned to the attachment.")
    void testFailedDeleteSupportedMimeTypeWithAssignedId() {
        Response deleteResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .when()
                .delete(BASE_PATH + "/" + EXISTING_SUPPORTED_MIME_TYPE_ID);
        deleteResponse.then().statusCode(BAD_REQUEST.getStatusCode());

        RFCProblemDTO rfcProblemDTO = deleteResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("You cannot delete supported mime-type" +
                " with id " + EXISTING_SUPPORTED_MIME_TYPE_ID + ". It is assigned to the attachment.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo(ExceptionToRFCProblemMapper.TECHNICAL_ERROR);
        assertThat(rfcProblemDTO.getType())
                .isEqualTo(ExceptionToRFCProblemMapper.RFCProblemType.REST_EXCEPTION.toString());
    }

    @Test
    @DisplayName("Returns exception when trying to delete supported mime-type for a nonexistent id.")
    void testFailedDeleteSupportedMimeTypeById() {
        Response deleteResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .delete(BASE_PATH + "/" + NONEXISTENT_SUPPORTED_MIME_TYPE_ID);
        deleteResponse.then().statusCode(NOT_FOUND.getStatusCode());

        RFCProblemDTO rfcProblemDTO = deleteResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("The supported mime-type with id "
                + NONEXISTENT_SUPPORTED_MIME_TYPE_ID + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo(ExceptionToRFCProblemMapper.TECHNICAL_ERROR);
        assertThat(rfcProblemDTO.getType())
                .isEqualTo(ExceptionToRFCProblemMapper.RFCProblemType.REST_EXCEPTION.toString());
    }

    @Test
    @DisplayName("Updates name and description in supported mime-type.")
    void testSuccessfulUpdateSupportedMimeType() {
        final String supportedMimeTypeName = "TEST_UPDATE_SUPPORTED_MIME_TYPE_NAME";
        final String supportedMimeTypeDescription = "TEST_UPDATE_SUPPORTED_MIME_TYPE_DESCRIPTION";
        SupportedMimeTypeCreateUpdateDTO supportedMimeTypeUpdateDTO = new SupportedMimeTypeCreateUpdateDTO();
        supportedMimeTypeUpdateDTO.setName(supportedMimeTypeName);
        supportedMimeTypeUpdateDTO.setDescription(supportedMimeTypeDescription);

        Response putResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
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
    void testFailedUpdateSupportedMimeTypeById() {
        final String supportedMimeTypeName = "TEST_UPDATE_SUPPORTED_MIME_TYPE_NAME";
        final String supportedMimeTypeDescription = "TEST_UPDATE_SUPPORTED_MIME_TYPE_DESCRIPTION";
        SupportedMimeTypeCreateUpdateDTO supportedMimeTypeUpdateDTO = new SupportedMimeTypeCreateUpdateDTO();
        supportedMimeTypeUpdateDTO.setName(supportedMimeTypeName);
        supportedMimeTypeUpdateDTO.setDescription(supportedMimeTypeDescription);

        Response putResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(supportedMimeTypeUpdateDTO)
                .when()
                .put(BASE_PATH + "/" + NONEXISTENT_SUPPORTED_MIME_TYPE_ID);
        putResponse.then().statusCode(NOT_FOUND.getStatusCode());

        RFCProblemDTO rfcProblemDTO = putResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("The supported mime-type with id "
                + NONEXISTENT_SUPPORTED_MIME_TYPE_ID + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo(ExceptionToRFCProblemMapper.TECHNICAL_ERROR);
        assertThat(rfcProblemDTO.getType())
                .isEqualTo(ExceptionToRFCProblemMapper.RFCProblemType.REST_EXCEPTION.toString());
    }

    @Test
    @DisplayName("Gets all supported mime-types.")
    void testSuccessfulGetAllSupportedMimeTypes() {
        Response getResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH);
        getResponse.then().statusCode(OK.getStatusCode());

        List<SupportedMimeTypeDTO> typesOfDocuments = getResponse.as(getSupportedMimeTypeDTOTypeRef());
        assertThat(typesOfDocuments).hasSize(3);
        assertThat(typesOfDocuments.get(0).getId()).isIn(EXISTING_SUPPORTED_MIME_TYPE_IDS);
        assertThat(typesOfDocuments.get(0).getName()).isIn(EXISTING_SUPPORTED_MIME_TYPE_NAMES);
        assertThat(typesOfDocuments.get(1).getId()).isIn(EXISTING_SUPPORTED_MIME_TYPE_IDS);
        assertThat(typesOfDocuments.get(1).getName()).isIn(EXISTING_SUPPORTED_MIME_TYPE_NAMES);
        assertThat(typesOfDocuments.get(2).getId()).isIn(EXISTING_SUPPORTED_MIME_TYPE_IDS);
        assertThat(typesOfDocuments.get(2).getName()).isIn(EXISTING_SUPPORTED_MIME_TYPE_NAMES);
    }

    @Test
    @DisplayName("Returns supported mime type by id.")
    void testSuccessfulGetSupportedMimeType() {
        Response response = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
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
    void testFailedGetSupportedMimeType() {
        Response response = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .when()
                .get(BASE_PATH + "/" + NONEXISTENT_SUPPORTED_MIME_TYPE_ID);

        response.then().statusCode(NOT_FOUND.getStatusCode());
        RFCProblemDTO rfcProblemDTO = response.as(RFCProblemDTO.class);

        assertThat(rfcProblemDTO.getStatus()).hasToString("404");
        assertThat(rfcProblemDTO.getDetail())
                .isEqualTo("The supported mime-type with id " + NONEXISTENT_SUPPORTED_MIME_TYPE_ID
                        + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo("TECHNICAL ERROR");
        assertThat(rfcProblemDTO.getType()).isEqualTo("REST_EXCEPTION");
    }

    private TypeRef<List<SupportedMimeTypeDTO>> getSupportedMimeTypeDTOTypeRef() {
        return new TypeRef<>() {
        };
    }
}
