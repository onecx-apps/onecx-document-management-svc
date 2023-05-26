package org.tkit.document.management.rs.v1.controllers;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tkit.document.management.domain.criteria.DocumentSearchCriteria;
import org.tkit.document.management.domain.daos.DocumentDAO;
import org.tkit.document.management.domain.models.enums.LifeCycleState;
import org.tkit.document.management.rs.v1.models.*;
import org.tkit.document.management.rs.v1.services.FileService;
import org.tkit.document.management.test.AbstractTest;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.rs.models.PageResultDTO;
import org.tkit.quarkus.rs.models.TraceableDTO;
import org.tkit.quarkus.test.WithDBData;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

@QuarkusTest
@SuppressWarnings("java:S5961")
@WithDBData(value = { "document-management-test-data.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
public class DocumentControllerTest extends AbstractTest {

    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private FileService fileService;

    @Inject
    private DocumentController documentController;
    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private static final String BASE_PATH = "/v1/document";
    private static final String EXISTING_DOCUMENT_ID = "51";
    private static final String EXISTING_DOCUMENT_ID_WITHOUT_ATTACHMENTS = "53";
    private static final String NOT_EXISTING_DOCUMENT_ID = "1000";
    private static final String NAME_OF_DOCUMENT_1 = "document_1";
    private static final String DOCUMENT_CREATION_USER = "test";
    private static final String DESCRIPTION_OF_DOCUMENT_1 = "description_1";
    private static final String VERSION_OF_DOCUMENT_1 = "v_1";
    private static final LifeCycleState STATUS_OF_DOCUMENT_1 = LifeCycleState.DRAFT;
    private static final String CHANNEL_ID_OF_DOCUMENT_1 = "1";
    private static final String RELATED_OBJECT_ID_OF_DOCUMENT_1 = "1";
    private static final String RELATED_OBJECT_REF_ID_OF_DOCUMENT = "43";
    private static final String RELATED_OBJECT_REF_TYPE_OF_DOCUMENT = "customer bill";
    private static final String SPECIFICATION_ID_OF_DOCUMENT_1 = "251";
    private static final String TYPE_ID_OF_DOCUMENT_1 = "201";
    private static final int NUMBER_OF_TAGS_OF_DOCUMENT_1 = 2;
    private static final int NUMBER_OF_DOCUMENT_RELATIONSHIPS_OF_DOCUMENT_1 = 1;
    private static final String DOCUMENT_RELATIONSHIP_ID = "1";
    private static final int NUMBER_OF_DOCUMENT_CHARACTERISTICS_OF_DOCUMENT_1 = 1;
    private static final String DOCUMENT_CHARACTERISTIC_ID = "1";
    private static final int NUMBER_OF_RELATED_PARTIES_OF_DOCUMENT_1 = 1;
    private static final String RELATED_PARTY_ID = "1";
    private static final int NUMBER_OF_CATEGORIES_RELATIONSHIPS_OF_DOCUMENT_1 = 3;
    private static final int NUMBER_OF_ATTACHMENTS_RELATIONSHIPS_OF_DOCUMENT_1 = 2;
    private static final String ZIP_CONTENT_TYPE = "application/zip";
    private static final String SAMPLE_FILE_PATH_1 = "src/test/resources/sample.jpg";
    private static final String SAMPLE_FILE_PATH_2 = "src/test/resources/sample2.jpg";
    private static final String FORM_PARAM_FILE = "file";
    private static final String FILE_BASE_PATH = "/v1/files/";
    private static final String BUCKET_NAME = "test-bucket";
    private static final String MINIO_FILE_PATH_1 = "101";
    private static final String MINIO_FILE_PATH_2 = "102";
    private static final String INVALID_MINIO_FILE_PATH_1 = "10001";
    private static final String INVALID_MINIO_FILE_PATH_2 = "10002";
    private static final String EXISTING_DOCUMENT_ID_2 = "52";
    private static final String EXISTING_DOCUMENT_ID_4 = "54";
    private static final String NOT_EXISTING_DOCUMENT_ID_2 = "1001";
    private static final String UPDATED_DOCUMENT_NAME = "updated_document_name";
    private static final String UPDATED_DOCUMENT_TYPE = "203";
    private static final String UPDATED_DOCUMENT_DESCRIPTION = "updated_description";

    @Test
    @DisplayName("Returns all documents with no criteria given.")
    public void testSuccessfulGetWithoutCriteria() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO documents = response.as(PageResultDTO.class);
        assertThat(documents.getStream().size()).isEqualTo(4);
    }

    @Test
    @DisplayName("Returns all documents with no criteria given with set page size.")
    public void testSuccessfulGetWithoutCriteriaWithPageSize() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("size", 1)
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO documents = response.as(PageResultDTO.class);
        assertThat(documents.getStream().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns all documents with no criteria given with set page size and given page number.")
    public void testSuccessfulGetWithoutCriteriaWithPageSizeAndPageNumber() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("size", 1)
                .queryParam("page", 1)
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO documents = response.as(PageResultDTO.class);
        assertThat(documents.getStream().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns document by ID")
    public void testSuccessfulGetDocument() {
        Set<String> tags = new HashSet<>();
        tags.add("tag_1");
        tags.add("tag_2");
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add("1");
        categoryIds.add("2");
        categoryIds.add("3");
        List<String> attachmentIds = new ArrayList<>();
        attachmentIds.add("101");
        attachmentIds.add("102");

        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH + "/" + EXISTING_DOCUMENT_ID);

        response.then().statusCode(200);
        DocumentDetailDTO document = response.as(DocumentDetailDTO.class);

        assertThat(document.getName()).isEqualTo(NAME_OF_DOCUMENT_1);
        assertThat(document.getDescription()).isEqualTo(DESCRIPTION_OF_DOCUMENT_1);
        assertThat(document.getDocumentVersion()).isEqualTo(VERSION_OF_DOCUMENT_1);
        assertThat(document.getLifeCycleState()).isEqualTo(STATUS_OF_DOCUMENT_1);
        assertThat(document.getChannel().getId()).isEqualTo(CHANNEL_ID_OF_DOCUMENT_1);
        assertThat(document.getRelatedObject().getId()).isEqualTo(RELATED_OBJECT_ID_OF_DOCUMENT_1);
        assertThat(document.getSpecification().getId()).isEqualTo(SPECIFICATION_ID_OF_DOCUMENT_1);
        assertThat(document.getType().getId()).isEqualTo(TYPE_ID_OF_DOCUMENT_1);
        assertThat(document.getTags().size()).isEqualTo(NUMBER_OF_TAGS_OF_DOCUMENT_1);
        assertThat(document.getTags().contains("tag_1")).isTrue();
        assertThat(document.getDocumentRelationships().size()).isEqualTo(NUMBER_OF_DOCUMENT_RELATIONSHIPS_OF_DOCUMENT_1);
        assertThat(document.getDocumentRelationships().stream().findFirst().get().getId()).isEqualTo(DOCUMENT_RELATIONSHIP_ID);
        assertThat(document.getCharacteristics().size()).isEqualTo(NUMBER_OF_DOCUMENT_CHARACTERISTICS_OF_DOCUMENT_1);
        assertThat(document.getCharacteristics().stream().findFirst().get().getId()).isEqualTo(DOCUMENT_CHARACTERISTIC_ID);
        assertThat(document.getRelatedParties().size()).isEqualTo(NUMBER_OF_RELATED_PARTIES_OF_DOCUMENT_1);
        assertThat(document.getRelatedParties().stream().findFirst().get().getId()).isEqualTo(RELATED_PARTY_ID);
        assertThat(document.getCategories().size()).isEqualTo(NUMBER_OF_CATEGORIES_RELATIONSHIPS_OF_DOCUMENT_1);
        assertThat(document.getCategories().stream().map(TraceableDTO::getId).collect(Collectors.toList()))
                .isEqualTo(categoryIds);
        assertThat(document.getAttachments().size()).isEqualTo(NUMBER_OF_ATTACHMENTS_RELATIONSHIPS_OF_DOCUMENT_1);
        assertThat(document.getAttachments().stream().map(TraceableDTO::getId).collect(Collectors.toList()))
                .containsAll(attachmentIds);
    }

    @Test
    @DisplayName("Returns exception when trying to get document for a nonexistent id.")
    public void testFailedGetDocument() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .when()
                .get(BASE_PATH + "/" + NOT_EXISTING_DOCUMENT_ID);
        response.then().statusCode(NOT_FOUND.getStatusCode());
        RFCProblemDTO rfcProblemDTO = response.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("404");
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("Document with id " + NOT_EXISTING_DOCUMENT_ID + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo("TECHNICAL ERROR");
        assertThat(rfcProblemDTO.getType()).isEqualTo("REST_EXCEPTION");
    }

    @Test
    @DisplayName("Search criteria. Finds document by id.")
    public void testSuccessfulSearchCriteriaFindDocumentById() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("id", EXISTING_DOCUMENT_ID)
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream().size()).isEqualTo(1);
        assertThat(documents.getStream().stream()).allMatch(el -> el.getId().equals(EXISTING_DOCUMENT_ID));
    }

    @Test
    @DisplayName("Search criteria. Finds all documents by id.")
    public void testSuccessfulSearchCriteriaFindAllDocumentsById() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("id", EXISTING_DOCUMENT_ID)
                .when()
                .get(BASE_PATH + "/show-all-documents");

        response.then().statusCode(200);
        // List<DocumentDetailDTO> documents = response.as(List);
        List<DocumentDetailDTO> documentList = Arrays.asList(response.getBody().as(DocumentDetailDTO[].class));
        assertThat(documentList.size()).isEqualTo(1);
        assertThat(documentList.stream()).allMatch(el -> el.getId().equals(EXISTING_DOCUMENT_ID));
    }

    @Test
    @DisplayName("Search criteria. Returns empty list when trying to find documents for nonexistent param.")
    public void testSuccessfulSearchCriteriaFindDocumentsByNotExistingParam() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("id", NOT_EXISTING_DOCUMENT_ID)
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Search criteria. Returns empty list when trying to find all documents for nonexistent param.")
    void testSuccessfulSearchCriteriaFindAllDocumentsByNonExistentParam() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("id", NOT_EXISTING_DOCUMENT_ID)
                .when()
                .get(BASE_PATH + "/show-all-documents");

        response.then().statusCode(200);
        List<DocumentDetailDTO> documentList = Arrays.asList(response.getBody().as(DocumentDetailDTO[].class));
        assertThat(documentList).hasSize(0);
    }

    @Test
    @DisplayName("Search criteria. Finds documents by name.")
    public void testSuccessfulSearchCriteriaFindDocumentsByName() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("name", NAME_OF_DOCUMENT_1)
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream().size()).isEqualTo(1);
        assertThat(documents.getStream().stream()).allMatch(el -> el.getName().equals(NAME_OF_DOCUMENT_1));
    }

    @Test
    @DisplayName("Search criteria. Finds all documents by name.")
    public void testSuccessfulSearchCriteriaFindAllDocumentsByName() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("name", NAME_OF_DOCUMENT_1)
                .when()
                .get(BASE_PATH + "/show-all-documents");

        response.then().statusCode(200);
        List<DocumentDetailDTO> documentList = Arrays.asList(response.getBody().as(DocumentDetailDTO[].class));
        assertThat(documentList).hasSize(1);
        assertThat(documentList.stream()).allMatch(el -> el.getName().equals(NAME_OF_DOCUMENT_1));
    }

    @Test
    @DisplayName("Search criteria. Finds documents by blank name. It should return all documents.")
    public void testSuccessfulSearchCriteriaFindDocumentsByBlankName() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("name", "")
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream()).hasSize(4);
    }

    @Test
    @DisplayName("Search criteria. Finds documents by first letters of name.")
    public void testSuccessfulSearchCriteriaFindDocumentsByFirstLetterOfName() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("name", "docu")
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream().size()).isEqualTo(4);
        assertThat(documents.getStream().stream()).allMatch(el -> el.getName().startsWith("docu"));
    }

    @Test
    @DisplayName("Search criteria. Finds all documents by first letters of name.")
    public void testSuccessfulSearchCriteriaFindAllDocumentsByFirstLetterOfName() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("name", "docu")
                .when()
                .get(BASE_PATH + "/show-all-documents");

        response.then().statusCode(200);
        List<DocumentDetailDTO> documentList = Arrays.asList(response.getBody().as(DocumentDetailDTO[].class));
        assertThat(documentList).hasSize(4);
        assertThat(documentList.stream()).allMatch(el -> el.getName().startsWith("docu"));
    }

    @Test
    @DisplayName("Search criteria. Finds documents by state.")
    public void testSuccessfulSearchCriteriaFindDocumentsByState() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("state", STATUS_OF_DOCUMENT_1)
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream().size()).isEqualTo(1);
        assertThat(documents.getStream().stream()).allMatch(el -> el.getLifeCycleState().equals(STATUS_OF_DOCUMENT_1));
    }

    @Test
    @DisplayName("Search criteria. Finds all documents by state.")
    public void testSuccessfulSearchCriteriaFindAllDocumentsByState() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("state", STATUS_OF_DOCUMENT_1)
                .when()
                .get(BASE_PATH + "/show-all-documents");

        response.then().statusCode(200);
        List<DocumentDetailDTO> documentList = Arrays.asList(response.getBody().as(DocumentDetailDTO[].class));
        assertThat(documentList).hasSize(1);
        assertThat(documentList.stream()).allMatch(el -> el.getLifeCycleState().equals(STATUS_OF_DOCUMENT_1));
    }

    @Test
    @DisplayName("Search criteria. Finds documents by type.")
    public void testSuccessfulSearchCriteriaFindDocumentsByType() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("typeId", TYPE_ID_OF_DOCUMENT_1)
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream().size()).isEqualTo(1);
        assertThat(documents.getStream().stream()).allMatch(el -> el.getType().getId().equals(TYPE_ID_OF_DOCUMENT_1));
    }

    @Test
    @DisplayName("Search criteria. Finds all documents by type.")
    public void testSuccessfulSearchCriteriaFindAllDocumentsByType() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("typeId", TYPE_ID_OF_DOCUMENT_1)
                .when()
                .get(BASE_PATH + "/show-all-documents");

        response.then().statusCode(200);
        List<DocumentDetailDTO> documentList = Arrays.asList(response.getBody().as(DocumentDetailDTO[].class));
        assertThat(documentList).hasSize(1);
        assertThat(documentList.stream()).allMatch(el -> el.getType().getId().equals(TYPE_ID_OF_DOCUMENT_1));
    }

    @Test
    @DisplayName("Search criteria. Finds documents by channel.")
    public void testSuccessfulSearchCriteriaFindDocumentsByChannel() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("channelName", "channel_1")
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream().size()).isEqualTo(1);
        assertThat(documents.getStream().stream()).allMatch(el -> el.getChannel().getId().equals(CHANNEL_ID_OF_DOCUMENT_1));
    }

    @Test
    @DisplayName("Search criteria. Finds all documents by channel.")
    public void testSuccessfulSearchCriteriaFindAllDocumentsByChannel() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("channelName", "channel_1")
                .when()
                .get(BASE_PATH + "/show-all-documents");

        response.then().statusCode(200);
        List<DocumentDetailDTO> documentList = Arrays.asList(response.getBody().as(DocumentDetailDTO[].class));
        assertThat(documentList).hasSize(1);
        assertThat(documentList.stream()).allMatch(el -> el.getChannel().getId().equals(CHANNEL_ID_OF_DOCUMENT_1));
    }

    @Test
    @DisplayName("Search criteria. Finds documents by creation user.")
    public void testSuccessfulSearchCriteriaFindDocumentsByCreationUser() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("createdBy", DOCUMENT_CREATION_USER)
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream()).hasSize(1);
        assertThat(documents.getStream().stream())
                .allMatch(el -> el.getCreationUser().equals(DOCUMENT_CREATION_USER));
    }

    @Test
    @DisplayName("Search criteria. Finds all documents by creation user.")
    public void testSuccessfulSearchCriteriaFindAllDocumentsByCreationUser() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("createdBy", DOCUMENT_CREATION_USER)
                .when()
                .get(BASE_PATH + "/show-all-documents");

        response.then().statusCode(200);
        List<DocumentDetailDTO> documentList = Arrays.asList(response.getBody().as(DocumentDetailDTO[].class));
        assertThat(documentList).hasSize(1);
        assertThat(documentList.stream())
                .allMatch(el -> el.getCreationUser().equals(DOCUMENT_CREATION_USER));
    }

    @Test
    @DisplayName("Search criteria. Finds documents by related object reference ID.")
    public void testSuccessfulSearchCriteriaFindDocumentsByObjectRefId() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("objectReferenceId", RELATED_OBJECT_REF_ID_OF_DOCUMENT)
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream()).hasSize(2);
        assertThat(documents.getStream().stream()).allMatch(el -> el.getRelatedObject().getObjectReferenceId()
                .equals(RELATED_OBJECT_REF_ID_OF_DOCUMENT));
    }

    @Test
    @DisplayName("Search criteria. Finds all documents by related object reference ID.")
    public void testSuccessfulSearchCriteriaFindAllDocumentsByObjectRefId() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("objectReferenceId", RELATED_OBJECT_REF_ID_OF_DOCUMENT)
                .when()
                .get(BASE_PATH + "/show-all-documents");

        response.then().statusCode(200);
        List<DocumentDetailDTO> documentList = Arrays.asList(response.getBody().as(DocumentDetailDTO[].class));
        assertThat(documentList).hasSize(2);
        assertThat(documentList.stream()).allMatch(el -> el.getRelatedObject().getObjectReferenceId()
                .equals(RELATED_OBJECT_REF_ID_OF_DOCUMENT));
    }

    @Test
    @DisplayName("Search criteria. Finds documents by related object reference type.")
    public void testSuccessfulSearchCriteriaFindDocumentsByObjectRefType() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("objectReferenceType", RELATED_OBJECT_REF_TYPE_OF_DOCUMENT)
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream()).hasSize(3);
        assertThat(documents.getStream().stream()).allMatch(el -> el.getRelatedObject().getObjectReferenceType()
                .equals(RELATED_OBJECT_REF_TYPE_OF_DOCUMENT));
    }

    @Test
    @DisplayName("Search criteria. Finds all documents by related object reference type.")
    public void testSuccessfulSearchCriteriaFindAllDocumentsByObjectRefType() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("objectReferenceType", RELATED_OBJECT_REF_TYPE_OF_DOCUMENT)
                .when()
                .get(BASE_PATH + "/show-all-documents");

        response.then().statusCode(200);
        List<DocumentDetailDTO> documentList = Arrays.asList(response.getBody().as(DocumentDetailDTO[].class));
        assertThat(documentList).hasSize(3);
        assertThat(documentList.stream()).allMatch(el -> el.getRelatedObject().getObjectReferenceType()
                .equals(RELATED_OBJECT_REF_TYPE_OF_DOCUMENT));
    }

    @Test
    @DisplayName("Search criteria. Fails to perform a search with null criteria.")
    public void testFailedSearchWithNullCriteria() {
        DocumentSearchCriteria criteria = null;
        DAOException exception = assertThrows(DAOException.class,
                () -> documentDAO.findBySearchCriteria(criteria));
        assertEquals(DocumentDAO.ErrorKeys.ERROR_FIND_DOCUMENT_SEARCH_CRITERIA_REQUIRED, exception.getMessageKey());
    }

    @Test
    @DisplayName("Search criteria. Fails to perform a search of all documents with null criteria.")
    public void testFailedShowAllDocumentsWithNullCriteria() {
        DocumentSearchCriteria criteria = null;
        DAOException exception = assertThrows(DAOException.class,
                () -> documentDAO.findAllDocumentsBySearchCriteria(criteria));
        assertEquals(DocumentDAO.ErrorKeys.ERROR_FIND_DOCUMENT_SEARCH_CRITERIA_REQUIRED, exception.getMessageKey());
    }

    @Test
    @DisplayName("Search criteria. Test fails when we mock an exception.")
    public void testFailedSearchWithCriteriaMockException() {
        DocumentSearchCriteria criteria = new DocumentSearchCriteria();

        EntityManager entityManagerMock = Mockito.mock(EntityManager.class);
        Mockito.when(entityManagerMock.getCriteriaBuilder()).thenThrow(new RuntimeException());

        DAOException exception = assertThrows(DAOException.class,
                () -> documentDAO.findBySearchCriteria(criteria));

        assertEquals(DocumentDAO.ErrorKeys.ERROR_FIND_DOCUMENT_BY_CRITERIA, exception.getMessageKey());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof RuntimeException);
    }

    @Test
    @DisplayName("Search criteria. Test fails when we search all documents but mock an exception.")
    public void testFailedShowAllDocumentsWithCriteriaMockException() {
        DocumentSearchCriteria criteria = new DocumentSearchCriteria();

        EntityManager entityManagerMock = Mockito.mock(EntityManager.class);
        Mockito.when(entityManagerMock.getCriteriaBuilder()).thenThrow(new RuntimeException());

        DAOException exception = assertThrows(DAOException.class,
                () -> documentDAO.findAllDocumentsBySearchCriteria(criteria));

        assertEquals(DocumentDAO.ErrorKeys.ERROR_FIND_DOCUMENT_BY_CRITERIA, exception.getMessageKey());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof RuntimeException);
    }

    @Test
    @DisplayName("Search criteria. Finds documents by Start Date")
    public void testSuccessfulSearchCriteriaFindDocumentsByStartDate() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("startDate", "2023-05-14 00:00")
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream()).hasSize(0);
    }

    @Test
    @DisplayName("Search criteria. Finds all documents by Start Date")
    public void testSuccessfulSearchCriteriaFindAllDocumentsByStartDate() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("startDate", "2023-05-14 00:00")
                .when()
                .get(BASE_PATH + "/show-all-documents");

        response.then().statusCode(200);
        List<DocumentDetailDTO> documentList = Arrays.asList(response.getBody().as(DocumentDetailDTO[].class));
        assertThat(documentList).hasSize(0);
    }

    @Test
    @DisplayName("Search criteria. Finds documents by Start Date Null")
    public void testSuccessfulSearchCriteriaFindDocumentsByStartDateNull() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("startDate", (Object) null)
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream().size()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Search criteria. Finds all documents by Start Date Null")
    public void testSuccessfulSearchCriteriaFindAllDocumentsByStartDateNull() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("startDate", (Object) null)
                .when()
                .get(BASE_PATH + "/show-all-documents");

        response.then().statusCode(200);
        List<DocumentDetailDTO> documentList = Arrays.asList(response.getBody().as(DocumentDetailDTO[].class));
        assertThat(documentList.size()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Search criteria. Finds documents by End Date")
    public void testSuccessfulSearchCriteriaFindDocumentsByEndDate() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("endDate", "2023-05-14 00:00")
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream()).hasSize(0);
    }

    @Test
    @DisplayName("Search criteria. Finds all documents by End Date")
    public void testSuccessfulSearchCriteriaFindAllDocumentsByEndDate() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("endDate", "2023-05-14 00:00")
                .when()
                .get(BASE_PATH + "/show-all-documents");

        response.then().statusCode(200);
        List<DocumentDetailDTO> documentList = Arrays.asList(response.getBody().as(DocumentDetailDTO[].class));
        assertThat(documentList).hasSize(0);
    }

    @Test
    @DisplayName("Search criteria. Finds documents by End Date Null")
    public void testSuccessfulSearchCriteriaFindDocumentsByEndDateNull() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("startDate", "2023-05-14 00:00")
                .queryParam("endDate", "2023-05-14 00:00")
                .queryParam("endDate", (Object) null)
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream().size()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Search criteria. Finds all documents by End Date Null")
    public void testSuccessfulSearchCriteriaFindAllDocumentsByEndDateNull() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("startDate", "2023-05-14 00:00")
                .queryParam("endDate", "2023-05-14 00:00")
                .queryParam("endDate", (Object) null)
                .when()
                .get(BASE_PATH + "/show-all-documents");

        response.then().statusCode(200);
        List<DocumentDetailDTO> documentList = Arrays.asList(response.getBody().as(DocumentDetailDTO[].class));
        assertThat(documentList.size()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Search criteria. Finds document by a non-existent ID")
    public void testFailedGetDocumentById() {
        Response response = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH + "/" + NOT_EXISTING_DOCUMENT_ID);

        response.then().statusCode(NOT_FOUND.getStatusCode());
        RFCProblemDTO rfcProblemDTO = response.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("404");
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("Document with id " + NOT_EXISTING_DOCUMENT_ID + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo("TECHNICAL ERROR");
        assertThat(rfcProblemDTO.getType()).isEqualTo("REST_EXCEPTION");
    }

    @Test
    @DisplayName("Deletes document by id.")
    public void testSuccessfulDeletesDocumentById() {
        Response deleteResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .delete(BASE_PATH + "/" + EXISTING_DOCUMENT_ID);
        deleteResponse.then().statusCode(NO_CONTENT.getStatusCode());

        Response getResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH);
        getResponse.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = getResponse.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("Returns exception when trying to delete document for a nonexistent id.")
    public void testFailedDeleteDocumentById() {
        Response deleteResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .delete(BASE_PATH + "/" + NOT_EXISTING_DOCUMENT_ID);
        deleteResponse.then().statusCode(NOT_FOUND.getStatusCode());
        RFCProblemDTO rfcProblemDTO = deleteResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("404");
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("Document with id " + NOT_EXISTING_DOCUMENT_ID + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo("TECHNICAL ERROR");
        assertThat(rfcProblemDTO.getType()).isEqualTo("REST_EXCEPTION");
    }

    @Test
    @DisplayName("Saves Document  with the required fields with validated data.")
    public void testSuccessfulCreateDocumentWithRequiredFields() {
        final String documentName = "TEST_DOCUMENT_NAME";
        final String attachmentName = "TEST_ATTACHMENT_NAME";
        final String channelName = "TEST_CHANNEL_NAME";
        final String documentTypeId = "202";
        final String attachmentMimeTypeId = "152";
        ChannelCreateUpdateDTO channelDTO = new ChannelCreateUpdateDTO();
        channelDTO.setName(channelName);
        AttachmentCreateUpdateDTO attachment = new AttachmentCreateUpdateDTO();
        attachment.setName(attachmentName);
        attachment.setMimeTypeId(attachmentMimeTypeId);
        List<AttachmentCreateUpdateDTO> attachments = new ArrayList<>();
        attachments.add(attachment);

        DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
        documentCreateDTO.setName(documentName);
        documentCreateDTO.setTypeId(documentTypeId);
        documentCreateDTO.setChannel(channelDTO);
        documentCreateDTO.setAttachments(attachments);

        Response postResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .post(BASE_PATH);

        postResponse.then().statusCode(201);
        DocumentDetailDTO documentDTO = postResponse.as(DocumentDetailDTO.class);

        assertThat(documentDTO.getId()).isNotNull();
        assertThat(documentDTO.getName()).isEqualTo(documentName);
        assertThat(documentDTO.getType().getId()).isEqualTo(documentTypeId);
        assertThat(documentDTO.getChannel().getId()).isNotNull();
        assertThat(documentDTO.getChannel().getName()).isEqualTo(channelName);
        assertThat(documentDTO.getAttachments().size()).isEqualTo(1);
        assertThat(documentDTO.getAttachments().stream().findFirst().get().getId()).isNotNull();
        assertThat(documentDTO.getAttachments().stream()).allMatch(el -> el.getMimeType().getId().equals(attachmentMimeTypeId));
    }

    @Test
    @DisplayName("Test the successful creation of a document with null attachments.")
    public void testSuccessfulCreateDocumentWithNullAttachments() {

        final String documentName = "TEST_DOCUMENT_NAME";
        final String channelName = "TEST_CHANNEL_NAME";
        final String documentTypeId = "202";

        ChannelCreateUpdateDTO channelDTO = new ChannelCreateUpdateDTO();
        channelDTO.setName(channelName);
        DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
        documentCreateDTO.setName(documentName);
        documentCreateDTO.setTypeId(documentTypeId);
        documentCreateDTO.setChannel(channelDTO);
        documentCreateDTO.setAttachments(null);

        Response postResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .post(BASE_PATH);

        postResponse.then().statusCode(201);
        DocumentDetailDTO documentDetailDTO = postResponse.as(DocumentDetailDTO.class);

        assertThat(documentDetailDTO.getId()).isNotNull();
        assertThat(documentDetailDTO.getName()).isEqualTo(documentName);
        assertThat(documentDetailDTO.getType().getId()).isEqualTo(documentTypeId);
        assertThat(documentDetailDTO.getChannel().getId()).isNotNull();
        assertThat(documentDetailDTO.getChannel().getName()).isEqualTo(channelName);
        assertThat(documentDetailDTO.getAttachments()).isNull();
    }

    @Test
    @DisplayName("Test the successful creation of a document with attachments having null or empty IDs.")
    public void testSuccessfulCreateDocumentWithAttachmentsHavingNullOrEmptyIds() {

        final String documentName = "TEST_DOCUMENT_NAME";
        final String channelName = "TEST_CHANNEL_NAME";
        final String documentTypeId = "202";
        final String attachmentNamePrefix = "TEST_ATTACHMENT_NAME";
        final String attachmentMimeTypeId = "152";

        ChannelCreateUpdateDTO channelDTO = new ChannelCreateUpdateDTO();
        channelDTO.setName(channelName);
        AttachmentCreateUpdateDTO attachment1 = new AttachmentCreateUpdateDTO();
        attachment1.setId(null);
        attachment1.setName(attachmentNamePrefix + "1");
        attachment1.setMimeTypeId(attachmentMimeTypeId);
        AttachmentCreateUpdateDTO attachment2 = new AttachmentCreateUpdateDTO();
        attachment2.setId("");
        attachment2.setName(attachmentNamePrefix + "2");
        attachment2.setMimeTypeId(attachmentMimeTypeId);
        List<AttachmentCreateUpdateDTO> attachments = new ArrayList<>();
        attachments.add(attachment1);
        attachments.add(attachment2);

        DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
        documentCreateDTO.setName(documentName);
        documentCreateDTO.setTypeId(documentTypeId);
        documentCreateDTO.setChannel(channelDTO);
        documentCreateDTO.setAttachments(attachments);

        Response postResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .post(BASE_PATH);

        postResponse.then().statusCode(201);
        DocumentDetailDTO documentDetailDTO = postResponse.as(DocumentDetailDTO.class);

        assertThat(documentDetailDTO.getId()).isNotNull();
        assertThat(documentDetailDTO.getName()).isEqualTo(documentName);
        assertThat(documentDetailDTO.getType().getId()).isEqualTo(documentTypeId);
        assertThat(documentDetailDTO.getChannel().getId()).isNotNull();
        assertThat(documentDetailDTO.getChannel().getName()).isEqualTo(channelName);
        assertThat(documentDetailDTO.getAttachments()).hasSize(2);
        assertThat(documentDetailDTO.getAttachments().stream().findFirst().get().getId()).isNotNull();
        assertThat(documentDetailDTO.getAttachments().stream())
                .allMatch(el -> el.getMimeType().getId().equals(attachmentMimeTypeId));
    }

    @Test
    @DisplayName("Saves Document  with all fields with validated data.")
    public void testSuccessfulCreateDocumentWithAllFields() {

        Set<String> tags = new HashSet<>();
        tags.add("TEST_DOCUMENT_TAG");

        final String documentTypeId = "202";

        final String documentSpecificationName = "TEST_SPECIFICATION_NAME";
        DocumentSpecificationCreateUpdateDTO documentSpecificationCreateUpdateDTO = new DocumentSpecificationCreateUpdateDTO();
        documentSpecificationCreateUpdateDTO.setName(documentSpecificationName);

        ChannelCreateUpdateDTO channelDTO = new ChannelCreateUpdateDTO();
        final String channelName = "TEST_CHANNEL_NAME";
        channelDTO.setName(channelName);

        RelatedObjectRefCreateUpdateDTO relatedObjectDTO = new RelatedObjectRefCreateUpdateDTO();
        final String relatedObjInvolvement = "TEST_INVOLVEMENT";
        relatedObjectDTO.setInvolvement(relatedObjInvolvement);

        Set<DocumentRelationshipCreateUpdateDTO> documentRelationshipDTOs = new HashSet<>();
        DocumentRelationshipCreateUpdateDTO documentRelationshipDTO = new DocumentRelationshipCreateUpdateDTO();
        final String documentRelationshipType = "TEST_RELATIONSHIP_TYPE";
        documentRelationshipDTO.setType(documentRelationshipType);
        documentRelationshipDTOs.add(documentRelationshipDTO);

        Set<DocumentCharacteristicCreateUpdateDTO> documentCharacteristicDTOs = new HashSet<>();
        DocumentCharacteristicCreateUpdateDTO documentCharacteristicDTO = new DocumentCharacteristicCreateUpdateDTO();
        final String characteristicName = "TEST_CHARACTERISTIC_NAME";
        documentCharacteristicDTO.setName(characteristicName);
        documentCharacteristicDTOs.add(documentCharacteristicDTO);

        Set<RelatedPartyRefCreateUpdateDTO> documentRelatedPartyDTOs = new HashSet<>();
        RelatedPartyRefCreateUpdateDTO documentRelatedPartyDTO = new RelatedPartyRefCreateUpdateDTO();
        final String relatedPartyName = "TEST_RELATED_PARTY_NAME";
        documentRelatedPartyDTO.setName(relatedPartyName);
        documentRelatedPartyDTOs.add(documentRelatedPartyDTO);

        Set<CategoryCreateUpdateDTO> documentCategoryDTOs = new HashSet<>();
        CategoryCreateUpdateDTO documentCategoryDTO = new CategoryCreateUpdateDTO();
        final String categoryName = "TEST_CATEGORY_NAME";
        documentCategoryDTO.setName(categoryName);
        documentCategoryDTOs.add(documentCategoryDTO);

        final String attachmentMimeTypeId = "151";

        List<AttachmentCreateUpdateDTO> attachments = new ArrayList<>();
        AttachmentCreateUpdateDTO attachment = new AttachmentCreateUpdateDTO();
        final String attachmentName = "TEST_ATTACHMENT_NAME";
        attachment.setName(attachmentName);
        attachment.setMimeTypeId(attachmentMimeTypeId);
        attachments.add(attachment);

        DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
        final String documentName = "TEST_DOCUMENT_NAME";
        final String documentDescription = "TEST_DOCUMENT_DESCRIPTION";
        final LifeCycleState documentState = LifeCycleState.ARCHIVED;
        final String documentVersion = "TEST_DOCUMENT_VERSION";
        documentCreateDTO.setName(documentName);
        documentCreateDTO.setDescription(documentDescription);
        documentCreateDTO.setLifeCycleState(documentState);
        documentCreateDTO.setDocumentVersion(documentVersion);
        documentCreateDTO.setTags(tags);
        documentCreateDTO.setTypeId(documentTypeId);
        documentCreateDTO.setSpecification(documentSpecificationCreateUpdateDTO);
        documentCreateDTO.setChannel(channelDTO);
        documentCreateDTO.setDocumentRelationships(documentRelationshipDTOs);
        documentCreateDTO.setCharacteristics(documentCharacteristicDTOs);
        documentCreateDTO.setRelatedParties(documentRelatedPartyDTOs);
        documentCreateDTO.setRelatedObject(relatedObjectDTO);
        documentCreateDTO.setCategories(documentCategoryDTOs);
        documentCreateDTO.setAttachments(attachments);

        Response postResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .post(BASE_PATH);

        postResponse.then().statusCode(201);
        DocumentDetailDTO documentDTO = postResponse.as(DocumentDetailDTO.class);

        assertThat(documentDTO.getId()).isNotNull();
        assertThat(documentDTO.getName()).isEqualTo(documentName);
        assertThat(documentDTO.getDescription()).isEqualTo(documentDescription);
        assertThat(documentDTO.getLifeCycleState()).isEqualTo(documentState);
        assertThat(documentDTO.getDocumentVersion()).isEqualTo(documentVersion);
        assertThat(documentDTO.getTags().size()).isEqualTo(1);
        assertThat(documentDTO.getTags().contains("TEST_DOCUMENT_TAG")).isTrue();
        assertThat(documentDTO.getType().getId()).isEqualTo(documentTypeId);
        assertThat(documentDTO.getSpecification().getName()).isEqualTo(documentSpecificationName);
        assertThat(documentDTO.getChannel().getId()).isNotNull();
        assertThat(documentDTO.getChannel().getName()).isEqualTo(channelName);
        assertThat(documentDTO.getRelatedObject().getId()).isNotNull();
        assertThat(documentDTO.getRelatedObject().getInvolvement()).isEqualTo(relatedObjInvolvement);
        assertThat(documentDTO.getDocumentRelationships().size()).isEqualTo(1);
        assertThat(documentDTO.getDocumentRelationships().stream().findFirst().get().getId()).isNotNull();
        assertThat(documentDTO.getDocumentRelationships().stream())
                .allMatch(el -> el.getType().equals(documentRelationshipType));
        assertThat(documentDTO.getCharacteristics().size()).isEqualTo(1);
        assertThat(documentDTO.getCharacteristics().stream().findFirst().get().getId()).isNotNull();
        assertThat(documentDTO.getCharacteristics().stream()).allMatch(el -> el.getName().equals(characteristicName));
        assertThat(documentDTO.getRelatedParties().size()).isEqualTo(1);
        assertThat(documentDTO.getRelatedParties().stream().findFirst().get().getId()).isNotNull();
        assertThat(documentDTO.getRelatedParties().stream()).allMatch(el -> el.getName().equals(relatedPartyName));
        assertThat(documentDTO.getCategories().size()).isEqualTo(1);
        assertThat(documentDTO.getCategories().stream().findFirst().get().getId()).isNotNull();
        assertThat(documentDTO.getCategories().stream()).allMatch(el -> el.getName().equals(categoryName));
        assertThat(documentDTO.getAttachments().size()).isEqualTo(1);
        assertThat(documentDTO.getAttachments().stream().findFirst().get().getId()).isNotNull();
        assertThat(documentDTO.getAttachments().stream()).allMatch(el -> el.getMimeType().getId().equals(attachmentMimeTypeId));
    }

    @Test
    @DisplayName("Saves Document  without name.")
    public void testFailedCreateDocumentWithoutName() {
        final String channelName = "TEST_CHANNEL_NAME";
        final String attachmentName = "TEST_ATTACHMENT_NAME";
        final String documentTypeId = "2";
        final String attachmentMimeTypeId = "2";
        ChannelCreateUpdateDTO channelDTO = new ChannelCreateUpdateDTO();
        channelDTO.setName(channelName);
        AttachmentCreateUpdateDTO attachment = new AttachmentCreateUpdateDTO();
        attachment.setName(attachmentName);
        attachment.setMimeTypeId(attachmentMimeTypeId);
        List<AttachmentCreateUpdateDTO> attachments = new ArrayList<>();
        attachments.add(attachment);

        DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
        documentCreateDTO.setName(null);
        documentCreateDTO.setTypeId(documentTypeId);
        documentCreateDTO.setChannel(channelDTO);
        documentCreateDTO.setAttachments(attachments);

        Response postResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .post(BASE_PATH);

        postResponse.then().statusCode(BAD_REQUEST.getStatusCode());
        RFCProblemDTO rfcProblemDTO = postResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("400");
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("createDocument.documentDTO.name: must not be blank");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo("TECHNICAL ERROR");
        assertThat(rfcProblemDTO.getType()).isEqualTo("VALIDATION_EXCEPTION");
    }

    @Test
    @DisplayName("Saves Document  without type.")
    public void testFailedCreateDocumentWithoutType() {
        final String documentName = "TEST_DOCUMENT_NAME";
        final String attachmentName = "TEST_ATTACHMENT_NAME";
        final String channelName = "TEST_CHANNEL_NAME";
        final String attachmentMimeTypeId = "2";
        ChannelCreateUpdateDTO channelDTO = new ChannelCreateUpdateDTO();
        channelDTO.setName(channelName);
        AttachmentCreateUpdateDTO attachment = new AttachmentCreateUpdateDTO();
        attachment.setName(attachmentName);
        attachment.setMimeTypeId(attachmentMimeTypeId);
        List<AttachmentCreateUpdateDTO> attachments = new ArrayList<>();
        attachments.add(attachment);

        DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
        documentCreateDTO.setName(documentName);
        documentCreateDTO.setTypeId(null);
        documentCreateDTO.setChannel(channelDTO);
        documentCreateDTO.setAttachments(attachments);

        Response postResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .post(BASE_PATH);

        postResponse.then().statusCode(BAD_REQUEST.getStatusCode());
        RFCProblemDTO rfcProblemDTO = postResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("400");
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("createDocument.documentDTO.typeId: must not be null");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo("TECHNICAL ERROR");
        assertThat(rfcProblemDTO.getType()).isEqualTo("VALIDATION_EXCEPTION");
    }

    @Test
    @DisplayName("Saves Document  without channel.")
    public void testFailedCreateDocumentWithoutChannel() {
        final String documentName = "TEST_DOCUMENT_NAME";
        final String attachmentName = "TEST_ATTACHMENT_NAME";
        final String documentTypeId = "2";
        final String attachmentMimeTypeId = "2";
        AttachmentCreateUpdateDTO attachment = new AttachmentCreateUpdateDTO();
        attachment.setName(attachmentName);
        attachment.setMimeTypeId(attachmentMimeTypeId);
        List<AttachmentCreateUpdateDTO> attachments = new ArrayList<>();
        attachments.add(attachment);

        DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
        documentCreateDTO.setName(documentName);
        documentCreateDTO.setTypeId(documentTypeId);
        documentCreateDTO.setChannel(null);
        documentCreateDTO.setAttachments(attachments);

        Response postResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .post(BASE_PATH);

        postResponse.then().statusCode(BAD_REQUEST.getStatusCode());
        RFCProblemDTO rfcProblemDTO = postResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("400");
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("createDocument.documentDTO.channel: must not be null");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo("TECHNICAL ERROR");
        assertThat(rfcProblemDTO.getType()).isEqualTo("VALIDATION_EXCEPTION");
    }

    @Test
    @DisplayName("Saves Document without mimeType in attachment.")
    public void testFailedCreateDocumentWithoutMimeTypeInAttachment() {
        final String documentName = "TEST_DOCUMENT_NAME";
        final String attachmentName = "TEST_ATTACHMENT_NAME";
        final String channelName = "TEST_CHANNEL_NAME";
        final String documentTypeId = "202";
        ChannelCreateUpdateDTO channelDTO = new ChannelCreateUpdateDTO();
        channelDTO.setName(channelName);
        AttachmentCreateUpdateDTO attachment = new AttachmentCreateUpdateDTO();
        attachment.setName(attachmentName);
        attachment.setMimeTypeId(null);
        List<AttachmentCreateUpdateDTO> attachments = new ArrayList<>();
        attachments.add(attachment);

        DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
        documentCreateDTO.setName(documentName);
        documentCreateDTO.setTypeId(documentTypeId);
        documentCreateDTO.setChannel(channelDTO);
        documentCreateDTO.setAttachments(attachments);

        Response postResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .post(BASE_PATH);

        postResponse.then().statusCode(500);

    }

    @Test
    @DisplayName("Saves Document  with nonexistent type.")
    public void testFailedCreateDocumentWithNonexistentType() {
        final String documentName = "TEST_DOCUMENT_NAME";
        final String attachmentName = "TEST_ATTACHMENT_NAME";
        final String channelName = "TEST_CHANNEL_NAME";
        final String documentTypeId = "200";
        final String attachmentMimeTypeId = "2";
        ChannelCreateUpdateDTO channelDTO = new ChannelCreateUpdateDTO();
        channelDTO.setName(channelName);
        AttachmentCreateUpdateDTO attachment = new AttachmentCreateUpdateDTO();
        attachment.setName(attachmentName);
        attachment.setMimeTypeId(attachmentMimeTypeId);
        List<AttachmentCreateUpdateDTO> attachments = new ArrayList<>();
        attachments.add(attachment);

        DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
        documentCreateDTO.setName(documentName);
        documentCreateDTO.setTypeId(documentTypeId);
        documentCreateDTO.setChannel(channelDTO);
        documentCreateDTO.setAttachments(attachments);

        Response postResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .post(BASE_PATH);

        postResponse.then().statusCode(NOT_FOUND.getStatusCode());
        RFCProblemDTO rfcProblemDTO = postResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("404");
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("The document with ID " + documentTypeId + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo("TECHNICAL ERROR");
        assertThat(rfcProblemDTO.getType()).isEqualTo("REST_EXCEPTION");
    }

    @Test
    @DisplayName("Saves Document with nonexistent specification.")
    public void testSuccessfulCreateDocumentWithNonexistentSpecification() {
        final String documentName = "TEST_DOCUMENT_NAME";
        final String attachmentName = "TEST_ATTACHMENT_NAME";
        final String channelName = "TEST_CHANNEL_NAME";
        final String documentTypeId = "202";
        final String attachmentMimeTypeId = "152";
        ChannelCreateUpdateDTO channelDTO = new ChannelCreateUpdateDTO();
        channelDTO.setName(channelName);
        AttachmentCreateUpdateDTO attachment = new AttachmentCreateUpdateDTO();
        attachment.setName(attachmentName);
        attachment.setMimeTypeId(attachmentMimeTypeId);
        List<AttachmentCreateUpdateDTO> attachments = new ArrayList<>();
        attachments.add(attachment);

        DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
        documentCreateDTO.setName(documentName);
        documentCreateDTO.setTypeId(documentTypeId);
        documentCreateDTO.setSpecification(null);
        documentCreateDTO.setChannel(channelDTO);
        documentCreateDTO.setAttachments(attachments);

        Response postResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .post(BASE_PATH);

        postResponse.then().statusCode(201);
        DocumentDetailDTO documentDTO = postResponse.as(DocumentDetailDTO.class);

        assertThat(documentDTO.getId()).isNotNull();
        assertThat(documentDTO.getName()).isEqualTo(documentName);
        assertThat(documentDTO.getType().getId()).isEqualTo(documentTypeId);
        assertThat(documentDTO.getChannel().getId()).isNotNull();
        assertThat(documentDTO.getChannel().getName()).isEqualTo(channelName);
        assertThat(documentDTO.getAttachments().size()).isEqualTo(1);
        assertThat(documentDTO.getAttachments().stream().findFirst().get().getId()).isNotNull();
        assertThat(documentDTO.getAttachments().stream()).allMatch(el -> el.getMimeType().getId().equals(attachmentMimeTypeId));
    }

    @Test
    @DisplayName("Saves Document  with nonexistent mimeType.")
    public void testFailedCreateDocumentWithNonexistentMimeType() {
        final String documentName = "TEST_DOCUMENT_NAME";
        final String attachmentName = "TEST_ATTACHMENT_NAME";
        final String channelName = "TEST_CHANNEL_NAME";
        final String documentTypeId = "202";
        final String attachmentMimeTypeId = "200";
        ChannelCreateUpdateDTO channelDTO = new ChannelCreateUpdateDTO();
        channelDTO.setName(channelName);
        AttachmentCreateUpdateDTO attachment = new AttachmentCreateUpdateDTO();
        attachment.setName(attachmentName);
        attachment.setMimeTypeId(attachmentMimeTypeId);
        List<AttachmentCreateUpdateDTO> attachments = new ArrayList<>();
        attachments.add(attachment);

        DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
        documentCreateDTO.setName(documentName);
        documentCreateDTO.setTypeId(documentTypeId);
        documentCreateDTO.setChannel(channelDTO);
        documentCreateDTO.setAttachments(attachments);

        Response postResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .post(BASE_PATH);

        postResponse.then().statusCode(NOT_FOUND.getStatusCode());
        RFCProblemDTO rfcProblemDTO = postResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("404");
        assertThat(rfcProblemDTO.getDetail())
                .isEqualTo("The supported mime type with ID " + attachmentMimeTypeId + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo("TECHNICAL ERROR");
        assertThat(rfcProblemDTO.getType()).isEqualTo("REST_EXCEPTION");
    }

    @Test
    @DisplayName("Saves Document  with the required fields with validated data and given time period in attachment.")
    public void testSuccessfulCreateDocumentWithRequiredFieldsAndGivenTimePeriodInAttachment() {
        final String documentName = "TEST_DOCUMENT_NAME";
        final String attachmentName = "TEST_ATTACHMENT_NAME";
        final String channelName = "TEST_CHANNEL_NAME";
        final String documentTypeId = "202";
        final String attachmentMimeTypeId = "152";
        ChannelCreateUpdateDTO channelDTO = new ChannelCreateUpdateDTO();
        channelDTO.setName(channelName);
        AttachmentCreateUpdateDTO attachment = new AttachmentCreateUpdateDTO();
        attachment.setName(attachmentName);
        attachment.setMimeTypeId(attachmentMimeTypeId);
        TimePeriodDTO timePeriodDTO = new TimePeriodDTO();
        timePeriodDTO.setStartDateTime(OffsetDateTime.now());
        timePeriodDTO.setEndDateTime(OffsetDateTime.now().plusMonths(12));
        attachment.setValidFor(timePeriodDTO);
        List<AttachmentCreateUpdateDTO> attachments = new ArrayList<>();
        attachments.add(attachment);

        DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
        documentCreateDTO.setName(documentName);
        documentCreateDTO.setTypeId(documentTypeId);
        documentCreateDTO.setChannel(channelDTO);
        documentCreateDTO.setAttachments(attachments);

        Response postResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .post(BASE_PATH);

        postResponse.then().statusCode(201);
        DocumentDetailDTO documentDTO = postResponse.as(DocumentDetailDTO.class);

        assertThat(documentDTO.getId()).isNotNull();
        assertThat(documentDTO.getName()).isEqualTo(documentName);
        assertThat(documentDTO.getType().getId()).isEqualTo(documentTypeId);
        assertThat(documentDTO.getChannel().getId()).isNotNull();
        assertThat(documentDTO.getChannel().getName()).isEqualTo(channelName);
        assertThat(documentDTO.getAttachments().size()).isEqualTo(1);
        assertThat(documentDTO.getAttachments().stream().findFirst().get().getId()).isNotNull();
        assertThat(documentDTO.getAttachments().stream().findFirst().get().getValidFor().getStartDateTime()).isNotNull();
        assertThat(documentDTO.getAttachments().stream().findFirst().get().getValidFor().getEndDateTime()).isNotNull();
        assertThat(documentDTO.getAttachments().stream()).allMatch(el -> el.getMimeType().getId().equals(attachmentMimeTypeId));
    }

    @Test
    @DisplayName("Updates basic and required fields in Document.")
    public void testSuccessfulUpdateBasicAndRequiredFieldsInDocument() {
        Set<String> tags = new HashSet<>();
        tags.add("TEST_UPDATE_DOCUMENT_TAG_1");
        tags.add("TEST_UPDATE_DOCUMENT_TAG_2");
        final String documentTypeId = "201";
        ChannelCreateUpdateDTO channelDTO = new ChannelCreateUpdateDTO();
        final String channelName = "TEST_CHANNEL_NAME";
        channelDTO.setName(channelName);
        final String attachmentMimeTypeId = "151";
        List<AttachmentCreateUpdateDTO> attachments = new ArrayList<>();
        AttachmentCreateUpdateDTO attachment = new AttachmentCreateUpdateDTO();
        final String attachmentName = "TEST_UPDATE_ATTACHMENT_NAME";
        attachment.setName(attachmentName);
        attachment.setMimeTypeId(attachmentMimeTypeId);
        attachments.add(attachment);
        RelatedObjectRefCreateUpdateDTO relatedObjectRefCreateUpdateDTO = new RelatedObjectRefCreateUpdateDTO();
        relatedObjectRefCreateUpdateDTO.setInvolvement("TEST_UPDATE");

        DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
        final String documentName = "TEST_UPDATE_DOCUMENT_NAME";
        final String documentDescription = "TEST_UPDATE_DOCUMENT_DESCRIPTION";
        final LifeCycleState documentState = LifeCycleState.ARCHIVED;
        final String documentVersion = "TEST_UPDATE_DOCUMENT_VERSION";
        documentCreateDTO.setName(documentName);
        documentCreateDTO.setDescription(documentDescription);
        documentCreateDTO.setLifeCycleState(documentState);
        documentCreateDTO.setDocumentVersion(documentVersion);
        documentCreateDTO.setTags(tags);
        documentCreateDTO.setTypeId(documentTypeId);
        documentCreateDTO.setChannel(channelDTO);
        documentCreateDTO.setAttachments(attachments);
        documentCreateDTO.setRelatedObject(relatedObjectRefCreateUpdateDTO);

        Response putResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .put(BASE_PATH + "/" + EXISTING_DOCUMENT_ID);

        putResponse.then().statusCode(201);
        DocumentDetailDTO documentDetailDTO = putResponse.as(DocumentDetailDTO.class);

        assertThat(documentDetailDTO.getId()).isEqualTo(EXISTING_DOCUMENT_ID);
        assertThat(documentDetailDTO.getName()).isEqualTo(documentName);
        assertThat(documentDetailDTO.getDescription()).isEqualTo(documentDescription);
        assertThat(documentDetailDTO.getLifeCycleState()).isEqualTo(documentState);
        assertThat(documentDetailDTO.getDocumentVersion()).isEqualTo(documentVersion);
        assertThat(documentDetailDTO.getTags()).hasSize(2);
        assertThat(documentDetailDTO.getTags().contains("TEST_UPDATE_DOCUMENT_TAG_1")).isTrue();
        assertThat(documentDetailDTO.getTags().contains("TEST_UPDATE_DOCUMENT_TAG_2")).isTrue();
        assertThat(documentDetailDTO.getType().getId()).isEqualTo(documentTypeId);
        assertThat(documentDetailDTO.getSpecification()).isNull();
        assertThat(documentDetailDTO.getChannel().getId()).isNotNull();
        assertThat(documentDetailDTO.getChannel().getName()).isEqualTo(channelName);
        assertThat(documentDetailDTO.getRelatedObject().getId()).isNotNull();
        assertThat(documentDetailDTO.getRelatedObject().getInvolvement()).isEqualTo("TEST_UPDATE");
        assertThat(documentDetailDTO.getDocumentRelationships()).hasSize(1);
        assertThat(documentDetailDTO.getDocumentRelationships().stream().findFirst().get().getId())
                .isEqualTo(DOCUMENT_RELATIONSHIP_ID);
        assertThat(documentDetailDTO.getCharacteristics()).hasSize(1);
        assertThat(documentDetailDTO.getCharacteristics().stream().findFirst().get().getId())
                .isEqualTo(DOCUMENT_CHARACTERISTIC_ID);
        assertThat(documentDetailDTO.getRelatedParties()).hasSize(1);
        assertThat(documentDetailDTO.getRelatedParties().stream().findFirst().get().getId()).isEqualTo(RELATED_PARTY_ID);
        assertThat(documentDetailDTO.getCategories()).hasSize(3);
        assertThat(documentDetailDTO.getAttachments()).hasSize(3);
        assertThat(documentDetailDTO.getAttachments().stream())
                .allMatch(el -> el.getMimeType().getId().equals(attachmentMimeTypeId));

        documentDetailDTO = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH + "/" + EXISTING_DOCUMENT_ID)
                .as(DocumentDetailDTO.class);

        assertThat(documentDetailDTO.getId()).isEqualTo(EXISTING_DOCUMENT_ID);
        assertThat(documentDetailDTO.getName()).isEqualTo(documentName);
        assertThat(documentDetailDTO.getDescription()).isEqualTo(documentDescription);
        assertThat(documentDetailDTO.getLifeCycleState()).isEqualTo(documentState);
        assertThat(documentDetailDTO.getDocumentVersion()).isEqualTo(documentVersion);
        assertThat(documentDetailDTO.getTags()).hasSize(2);
        assertThat(documentDetailDTO.getTags().contains("TEST_UPDATE_DOCUMENT_TAG_1")).isTrue();
        assertThat(documentDetailDTO.getTags().contains("TEST_UPDATE_DOCUMENT_TAG_2")).isTrue();
        assertThat(documentDetailDTO.getType().getId()).isEqualTo(documentTypeId);
        assertThat(documentDetailDTO.getSpecification()).isNull();
        assertThat(documentDetailDTO.getChannel().getId()).isNotNull();
        assertThat(documentDetailDTO.getChannel().getName()).isEqualTo(channelName);
        assertThat(documentDetailDTO.getRelatedObject().getId()).isNotNull();
        assertThat(documentDetailDTO.getRelatedObject().getInvolvement()).isEqualTo("TEST_UPDATE");
        assertThat(documentDetailDTO.getDocumentRelationships()).hasSize(1);
        assertThat(documentDetailDTO.getDocumentRelationships().stream().findFirst().get().getId())
                .isEqualTo(DOCUMENT_RELATIONSHIP_ID);
        assertThat(documentDetailDTO.getCharacteristics()).hasSize(1);
        assertThat(documentDetailDTO.getCharacteristics().stream().findFirst().get().getId())
                .isEqualTo(DOCUMENT_CHARACTERISTIC_ID);
        assertThat(documentDetailDTO.getRelatedParties()).hasSize(1);
        assertThat(documentDetailDTO.getRelatedParties().stream().findFirst().get().getId()).isEqualTo(RELATED_PARTY_ID);
        assertThat(documentDetailDTO.getCategories()).hasSize(3);
        assertThat(documentDetailDTO.getAttachments()).hasSize(3);
        assertThat(documentDetailDTO.getAttachments().stream())
                .allMatch(el -> el.getMimeType().getId().equals(attachmentMimeTypeId));
    }

    @Test
    @DisplayName("Updates collections in Document.")
    public void testSuccessfulUpdateCollectionsInDocument() {
        final String documentTypeId = "201";

        DocumentRelationshipCreateUpdateDTO dto1 = new DocumentRelationshipCreateUpdateDTO();
        dto1.setId("1");
        dto1.setType("TEST_TYPE_1");
        DocumentRelationshipCreateUpdateDTO dto2 = new DocumentRelationshipCreateUpdateDTO();
        dto2.setType("TEST_TYPE_2");
        Set<DocumentRelationshipCreateUpdateDTO> relationships = Set.of(dto1, dto2);

        DocumentCharacteristicCreateUpdateDTO existingCharacteristic = new DocumentCharacteristicCreateUpdateDTO();
        existingCharacteristic.setId("1");
        existingCharacteristic.setName("TEST_Name_1");
        DocumentCharacteristicCreateUpdateDTO newCharacteristic = new DocumentCharacteristicCreateUpdateDTO();
        newCharacteristic.setName("TEST_Name_2");
        Set<DocumentCharacteristicCreateUpdateDTO> characteristics = Set.of(existingCharacteristic, newCharacteristic);

        RelatedPartyRefCreateUpdateDTO existingRelatedParty = new RelatedPartyRefCreateUpdateDTO();
        existingRelatedParty.setId("1");
        existingRelatedParty.setName("TEST_Name_1");
        RelatedPartyRefCreateUpdateDTO newRelatedParty = new RelatedPartyRefCreateUpdateDTO();
        newRelatedParty.setName("TEST_Name_2");
        Set<RelatedPartyRefCreateUpdateDTO> relatedParties = Set.of(existingRelatedParty, newRelatedParty);

        CategoryCreateUpdateDTO existingCategory = new CategoryCreateUpdateDTO();
        existingCategory.setId("4");
        existingCategory.setName("TEST_Name_1");
        CategoryCreateUpdateDTO newCategory = new CategoryCreateUpdateDTO();
        newCategory.setName("TEST_Name_2");
        Set<CategoryCreateUpdateDTO> categories = Set.of(existingCategory, newCategory);

        AttachmentCreateUpdateDTO existingAttachment = new AttachmentCreateUpdateDTO();
        existingAttachment.setId("101");
        existingAttachment.setName("TEST_Name_1");
        existingAttachment.setMimeTypeId("152");
        AttachmentCreateUpdateDTO newAttachment = new AttachmentCreateUpdateDTO();
        newAttachment.setName("TEST_Name_2");
        newAttachment.setMimeTypeId("151");
        List<AttachmentCreateUpdateDTO> attachments = List.of(existingAttachment, newAttachment);

        DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
        documentCreateDTO.setName(NAME_OF_DOCUMENT_1);
        documentCreateDTO.setTypeId(documentTypeId);
        documentCreateDTO.setChannel(new ChannelCreateUpdateDTO());
        documentCreateDTO.setAttachments(attachments);
        documentCreateDTO.setDocumentRelationships(relationships);
        documentCreateDTO.setCharacteristics(characteristics);
        documentCreateDTO.setRelatedParties(relatedParties);
        documentCreateDTO.setCategories(categories);

        Response putResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .put(BASE_PATH + "/" + EXISTING_DOCUMENT_ID);

        putResponse.then().statusCode(201);

        DocumentDetailDTO documentDetailDTO = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH + "/" + EXISTING_DOCUMENT_ID)
                .as(DocumentDetailDTO.class);

        assertThat(documentDetailDTO.getId()).isEqualTo(EXISTING_DOCUMENT_ID);
        assertThat(documentDetailDTO.getName()).isEqualTo(NAME_OF_DOCUMENT_1);
        assertThat(documentDetailDTO.getDescription()).isNull();
        assertThat(documentDetailDTO.getLifeCycleState()).isNull();
        assertThat(documentDetailDTO.getDocumentVersion()).isNull();
        assertThat(documentDetailDTO.getTags()).isEmpty();
        assertThat(documentDetailDTO.getType().getId()).isEqualTo(documentTypeId);
        assertThat(documentDetailDTO.getSpecification()).isNull();
        assertThat(documentDetailDTO.getChannel().getId()).isNotNull();
        assertThat(documentDetailDTO.getChannel()).isNotNull();
        assertThat(documentDetailDTO.getRelatedObject()).isNull();

        assertThat(documentDetailDTO.getDocumentRelationships().size()).isEqualTo(2);
        List<DocumentRelationshipDTO> relationships1 = documentDetailDTO.getDocumentRelationships()
                .stream().filter(p -> p.getId().equals("1")).collect(Collectors.toList());
        assertThat(relationships1.size()).isEqualTo(1);
        DocumentRelationshipDTO existingDocumentRelationship = relationships1.get(0);
        assertThat(existingDocumentRelationship.getType()).isEqualTo("TEST_TYPE_1");
        List<DocumentRelationshipDTO> newRelationship = documentDetailDTO.getDocumentRelationships()
                .stream().filter(p -> !p.getId().equals("1")).collect(Collectors.toList());
        assertThat(newRelationship.size()).isEqualTo(1);
        DocumentRelationshipDTO existingDocumentRelationship2 = newRelationship.get(0);
        assertThat(existingDocumentRelationship2.getType()).isEqualTo("TEST_TYPE_2");

        assertThat(documentDetailDTO.getCharacteristics().size()).isEqualTo(2);
        List<DocumentCharacteristicDTO> list1 = documentDetailDTO.getCharacteristics()
                .stream().filter(p -> p.getId().equals("1")).collect(Collectors.toList());
        assertThat(list1.size()).isEqualTo(1);
        DocumentCharacteristicDTO existingCharacteristicDTO = list1.get(0);
        assertThat(existingCharacteristicDTO.getName()).isEqualTo("TEST_Name_1");
        List<DocumentCharacteristicDTO> list2 = documentDetailDTO.getCharacteristics()
                .stream().filter(p -> !p.getId().equals("1")).collect(Collectors.toList());
        assertThat(list2.size()).isEqualTo(1);
        DocumentCharacteristicDTO newCharacteristicDTO = list2.get(0);
        assertThat(newCharacteristicDTO.getName()).isEqualTo("TEST_Name_2");

        assertThat(documentDetailDTO.getRelatedParties().size()).isEqualTo(2);
        List<RelatedPartyRefDTO> listRelatedParties1 = documentDetailDTO.getRelatedParties()
                .stream().filter(p -> p.getId().equals("1")).collect(Collectors.toList());
        assertThat(listRelatedParties1.size()).isEqualTo(1);
        RelatedPartyRefDTO existingRelatedPartyDTO = listRelatedParties1.get(0);
        assertThat(existingRelatedPartyDTO.getName()).isEqualTo("TEST_Name_1");
        List<RelatedPartyRefDTO> listRelatedParties2 = documentDetailDTO.getRelatedParties()
                .stream().filter(p -> !p.getId().equals("1")).collect(Collectors.toList());
        assertThat(listRelatedParties2.size()).isEqualTo(1);
        RelatedPartyRefDTO newRelatedPartyDTO = listRelatedParties2.get(0);
        assertThat(newRelatedPartyDTO.getName()).isEqualTo("TEST_Name_2");

        /*
         * assertThat(documentDetailDTO.getCategories().size()).isEqualTo(1);
         * List<CategoryDTO> listCategories1 = documentDetailDTO.getCategories()
         * .stream().filter(p -> p.getId().equals("4")).collect(Collectors.toList());
         * assertThat(listCategories1.size()).isEqualTo(1);
         * CategoryDTO existingCategoryDTO = listCategories1.get(0);
         * assertThat(existingCategoryDTO.getName()).isEqualTo("TEST_Name_1");
         * List<CategoryDTO> listCategories2 = documentDetailDTO.getCategories()
         * .stream().filter(p -> !p.getId().equals("4")).collect(Collectors.toList());
         * assertThat(listCategories2.size()).isEqualTo(1);
         * CategoryDTO newCategoryDTO = listCategories2.get(0);
         * assertThat(newCategoryDTO.getName()).isEqualTo("TEST_Name_2");
         */
        assertThat(documentDetailDTO.getAttachments().size()).isEqualTo(3);
        List<AttachmentDTO> listAttachment1 = documentDetailDTO.getAttachments()
                .stream().filter(p -> p.getId().equals("101")).collect(Collectors.toList());
        assertThat(listAttachment1.size()).isEqualTo(1);
        AttachmentDTO existingAttachmentDTO = listAttachment1.get(0);
        assertThat(existingAttachmentDTO.getMimeType().getId()).isEqualTo("152");
        List<AttachmentDTO> listAttachment2 = documentDetailDTO.getAttachments()
                .stream().filter(p -> !p.getId().equals("101")).collect(Collectors.toList());
        assertThat(listAttachment2.size()).isEqualTo(2);
        AttachmentDTO newAttachmentDTO = listAttachment2.get(0);
        assertThat(newAttachmentDTO.getMimeType().getId()).isEqualTo("151");
    }

    @Test
    @DisplayName("Returns exception when trying to update nonexistent document.")
    public void testFailedUpdateDocumentById() {
        final String documentTypeId = "1";
        ChannelCreateUpdateDTO channelDTO = new ChannelCreateUpdateDTO();
        final String channelName = "TEST_CHANNEL_NAME";
        channelDTO.setName(channelName);
        final String attachmentMimeTypeId = "2";
        List<AttachmentCreateUpdateDTO> attachments = new ArrayList<>();
        AttachmentCreateUpdateDTO attachment = new AttachmentCreateUpdateDTO();
        final String attachmentName = "TEST_UPDATE_ATTACHMENT_NAME";
        attachment.setName(attachmentName);
        attachment.setMimeTypeId(attachmentMimeTypeId);
        attachments.add(attachment);

        DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
        documentCreateDTO.setName("TEST_UPDATE_DOCUMENT_NAME");
        documentCreateDTO.setTypeId(documentTypeId);
        documentCreateDTO.setChannel(channelDTO);
        documentCreateDTO.setAttachments(attachments);
        Response putResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .put(BASE_PATH + "/" + NOT_EXISTING_DOCUMENT_ID);

        putResponse.then().statusCode(NOT_FOUND.getStatusCode());
        RFCProblemDTO rfcProblemDTO = putResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("404");
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("Document with id " + NOT_EXISTING_DOCUMENT_ID + " was not found.");
        assertThat(rfcProblemDTO.getInstance()).isNull();
        assertThat(rfcProblemDTO.getTitle()).isEqualTo("TECHNICAL ERROR");
        assertThat(rfcProblemDTO.getType()).isEqualTo("REST_EXCEPTION");
    }

    @Test
    @DisplayName("Gets all channels.")
    public void testSuccessfulGetAllChannels() {
        Response getResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH + "/channels");
        getResponse.then().statusCode(OK.getStatusCode());

        List<ChannelDTO> channels = getResponse.as(getChannelDTOTypeRef());
        assertThat(channels.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tests the successful upload of multiple file attachments at once to an existing document for the quick upload feature.")
    void testSuccessfulMultipleFileUploads() {
        Response postResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .multiPart("file", Paths.get("src/test/resources/file1.txt").toFile())
                .multiPart("file", Paths.get("src/test/resources/sample.jpg").toFile())
                .multiPart("file", Paths.get("src/test/resources/sample2.jpg").toFile())
                .when()
                .post(BASE_PATH + "/files/upload/" + EXISTING_DOCUMENT_ID_WITHOUT_ATTACHMENTS);
        postResponse.then().statusCode(CREATED.getStatusCode());
    }

    @Test
    @DisplayName("Tests the failed upload of multiple file attachments at once to a nonexistent document.")
    public void testFailedMultipleFileUploads() {
        Response postResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .multiPart("file", Paths.get("src/test/resources/sample.jpg").toFile())
                .multiPart("file", Paths.get("src/test/resources/sample2.jpg").toFile())
                .when()
                .post(BASE_PATH + "/files/upload/" + NOT_EXISTING_DOCUMENT_ID);
        postResponse.then().statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("Test method for cleanup of failed files.")
    void testSuccessfulDbCleanupOfFailedAttachments() {
        documentController.clearFailedFilesFromDBPeriodically();
    }

    private TypeRef<List<ChannelDTO>> getChannelDTOTypeRef() {
        return new TypeRef<>() {
        };
    }

    private TypeRef<PageResultDTO<DocumentDetailDTO>> getDocumentDetailDTOTypeRef() {
        return new TypeRef<>() {
        };

    }

    @Test
    @DisplayName("Get File By Non-Existing Id Failed")
    public void testFailedGetFileById() {
        Response response = given()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .when()
                .get(BASE_PATH + "/file/" + NOT_EXISTING_DOCUMENT_ID);
        response.then().statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("Bulk Delete of existing document ids")
    public void testSuccessBulkDelete() {

        ArrayList<String> ids = new ArrayList<String>();
        ids.add(EXISTING_DOCUMENT_ID_4);

        Response deleteResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ids)
                .when()
                .delete(BASE_PATH + "/delete-bulk-documents");
        deleteResponse.then().statusCode(204);

    }

    @Test
    @DisplayName("Bulk Delete of non-existing document ids")
    public void testFailedBulkDelete() {

        ArrayList<String> ids = new ArrayList<String>();
        ids.add(NOT_EXISTING_DOCUMENT_ID);
        ids.add(NOT_EXISTING_DOCUMENT_ID_2);

        Response deleteResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ids)
                .when()
                .delete(BASE_PATH + "/delete-bulk-documents");
        deleteResponse.then().statusCode(404);

    }

    @Test
    @DisplayName("Bulk Update of existing document ids")
    public void testSuccessBulkUpdate() {
        DocumentCreateUpdateDTO doc1 = new DocumentCreateUpdateDTO();
        doc1.setId(EXISTING_DOCUMENT_ID);
        doc1.setName(UPDATED_DOCUMENT_NAME);
        doc1.setTypeId(UPDATED_DOCUMENT_TYPE);
        doc1.setDescription(UPDATED_DOCUMENT_DESCRIPTION);
        DocumentCreateUpdateDTO doc2 = new DocumentCreateUpdateDTO();
        doc2.setId(EXISTING_DOCUMENT_ID_2);
        doc2.setName(UPDATED_DOCUMENT_NAME);
        doc2.setTypeId(UPDATED_DOCUMENT_TYPE);
        doc1.setDescription(UPDATED_DOCUMENT_DESCRIPTION);
        List<DocumentCreateUpdateDTO> dtoList = new ArrayList<>();
        dtoList.add(doc1);
        dtoList.add(doc2);
        Response postResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(dtoList)
                .when()
                .put(BASE_PATH + "/bulkupdate");
        postResponse.then().statusCode(201);
    }

    @Test
    @DisplayName("Bulk Update of non-existing document ids")
    public void testFailedBulkUpdate() {
        DocumentCreateUpdateDTO doc1 = new DocumentCreateUpdateDTO();
        doc1.setId(NOT_EXISTING_DOCUMENT_ID);
        doc1.setName(UPDATED_DOCUMENT_NAME);
        doc1.setTypeId(UPDATED_DOCUMENT_TYPE);
        doc1.setDescription(UPDATED_DOCUMENT_DESCRIPTION);
        List<DocumentCreateUpdateDTO> dtoList = new ArrayList<>();
        dtoList.add(doc1);
        Response postResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(dtoList)
                .when()
                .put(BASE_PATH + "/bulkupdate");
        postResponse.then().statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("Bulk Delete of existing document's attachments")
    public void testSuccessfulDeleteAttachmentFilesInBulk() {
        File sampleFile1 = new File(SAMPLE_FILE_PATH_1);
        File sampleFile2 = new File(SAMPLE_FILE_PATH_2);
        Response putResponse1 = given()
                .multiPart(FORM_PARAM_FILE, sampleFile1)
                .when()
                .put(FILE_BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH_1);
        putResponse1.then().statusCode(201);
        Response putResponse2 = given()
                .multiPart(FORM_PARAM_FILE, sampleFile2)
                .when()
                .put(FILE_BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH_2);
        putResponse2.then().statusCode(201);

        List<String> attachmentIds = new ArrayList<>();
        attachmentIds.add(MINIO_FILE_PATH_1);
        attachmentIds.add(MINIO_FILE_PATH_2);

        Response deleteResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(attachmentIds)
                .when()
                .delete(BASE_PATH + "/file/delete-bulk-attachment");
        deleteResponse.then().statusCode(NO_CONTENT.getStatusCode());

        /*
         * Response deleteMinioResponse1 = given()
         * .when()
         * .delete(FILE_BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH_1).andReturn();
         * deleteMinioResponse1.then().statusCode(201);
         * Response deleteMinioResponse2 = given()
         * .when()
         * .delete(FILE_BASE_PATH + BUCKET_NAME + "/" + MINIO_FILE_PATH_2).andReturn();
         * deleteMinioResponse2.then().statusCode(201);
         */

    }

    @Test
    @DisplayName("Bulk Delete of non-existing document's attachments")
    public void testFailedDeleteAttachmentFilesInBulk() {
        List<String> attachmentIds = new ArrayList<>();
        attachmentIds.add(INVALID_MINIO_FILE_PATH_1);
        attachmentIds.add(INVALID_MINIO_FILE_PATH_2);

        Response deleteResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(attachmentIds)
                .when()
                .delete(BASE_PATH + "/file/delete-bulk-attachment");
        assertThat(deleteResponse.statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("Get All existing Document's Attachments As Zip")
    public void testSuccessfulGetAllDocumentAttachmentsAsZip() {
        Response getResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .when()
                .get(BASE_PATH + "/file/" + EXISTING_DOCUMENT_ID + "/attachments");
        getResponse.then().statusCode(200);
        getResponse.then().contentType(ZIP_CONTENT_TYPE);
    }

    @Test
    @DisplayName("Get All non-existing Document's Attachments As Zip")
    public void testFailedGetAllDocumentAttachmentsAsZip() {
        Response getResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .when()
                .get(BASE_PATH + "/file/" + NOT_EXISTING_DOCUMENT_ID + "/attachments");
        getResponse.then().statusCode(400);
    }

    @Test
    @DisplayName("Get existing document's with no attachments as zip")
    public void testGetAllDocumentWithNoAttachmentsAsZip() {
        Response getResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .when()
                .get(BASE_PATH + "/file/" + EXISTING_DOCUMENT_ID_WITHOUT_ATTACHMENTS + "/attachments");
        getResponse.then().statusCode(204);
    }

    @Test
    @DisplayName("Get Failed Attachment by Id")
    public void testGetFailedAttachmentsById() {

        Response getResponse = given()
                .auth()
                .oauth2(keycloakClient.getAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH + "/files/upload/failed/" + EXISTING_DOCUMENT_ID);
        getResponse.then().statusCode(200);
    }
}
