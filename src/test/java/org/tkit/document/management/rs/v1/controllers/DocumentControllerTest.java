package org.tkit.document.management.rs.v1.controllers;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.tkit.document.management.domain.models.enums.LifeCycleState;
import org.tkit.document.management.rs.v1.models.*;
import org.tkit.document.management.test.AbstractTest;
import org.tkit.document.management.utils.JWTUtils;
import org.tkit.quarkus.rs.models.PageResultDTO;
import org.tkit.quarkus.rs.models.TraceableDTO;
import org.tkit.quarkus.test.WithDBData;

import io.minio.errors.*;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WithDBData(value = { "document-management-test-data.xls" }, deleteBeforeInsert = true, rinseAndRepeat = true)
public class DocumentControllerTest extends AbstractTest {
    /*
     * @Inject
     * private FileService fileService;
     */

    private static final String BASE_PATH = "/v1/document";
    private static final String BASE_FILE_PATH = "/v1/files";
    private static final String EXISTING_DOCUMENT_ID = "51";
    private static final String NOT_EXISTING_DOCUMENT_ID = "1000";
    private static final String NAME_OF_DOCUMENT_1 = "document_1";
    private static final String DESCRIPTION_OF_DOCUMENT_1 = "description_1";
    private static final String VERSION_OF_DOCUMENT_1 = "v_1";
    private static final LifeCycleState STATUS_OF_DOCUMENT_1 = LifeCycleState.DRAFT;
    private static final String CHANNEL_ID_OF_DOCUMENT_1 = "1";
    private static final String RELATED_OBJECT_ID_OF_DOCUMENT_1 = "1";
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

    private static String default_valid_token;
    private static String token_role;

    String bucket = "test-bucket";

    @BeforeAll
    public static void setUp() throws Exception {
        default_valid_token = JWTUtils.generateTokenString("/META-INF/resources/test_tokens/test_token_1.json",
                null);
        token_role = JWTUtils.generateTokenString("/META-INF/resources/test_tokens/test_token_2.json", null);
    }

    /*
     * @BeforeAll
     * public void createBucket() throws ServerException, InsufficientDataException, ErrorResponseException, IOException,
     * NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
     *
     * fileService.checkAndCreateBucket(bucket);
     *
     * }
     */
    @Test
    @DisplayName("Create bucket for given.")
    public void testCreateBucket() {

        Response response = given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .put(BASE_FILE_PATH + "/bucket/" + bucket);

        response.then().statusCode(201);

    }

    @Test
    @DisplayName("Returns all documents with no criteria given.")
    public void testSuccessfulGetWithoutCriteria() {
        Response response = given().header("Authorization", "bearer " + default_valid_token)
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO documents = response.as(PageResultDTO.class);
        assertThat(documents.getStream().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns all documents with no criteria given with set page size.")
    public void testSuccessfulGetWithoutCriteriaWithPageSize() {
        Response response = given().header("Authorization", "bearer " + default_valid_token)
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
        Response response = given().header("Authorization", "bearer " + default_valid_token)
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

        Response response = given().header("Authorization", "bearer " + default_valid_token)
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
                .isEqualTo(attachmentIds);
    }

    @Test
    @DisplayName("Returns exception when trying to get document for a nonexistent id.")
    public void testFailedGetDocument() {
        Response response = given().header("Authorization", "bearer " + default_valid_token)
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
        Response response = given().header("Authorization", "bearer " + default_valid_token)
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
    @DisplayName("Search criteria. Returns empty list when trying to find documents for nonexistent param.")
    public void testSuccessfulSearchCriteriaFindDocumentsByNotExistingParam() {
        Response response = given().header("Authorization", "bearer " + default_valid_token)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("id", NOT_EXISTING_DOCUMENT_ID)
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Search criteria. Finds documents by name.")
    public void testSuccessfulSearchCriteriaFindDocumentsByName() {
        Response response = given().header("Authorization", "bearer " + default_valid_token)
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
    @DisplayName("Search criteria. Finds documents by first letters of name.")
    public void testSuccessfulSearchCriteriaFindDocumentsByFirstLetterOfName() {
        Response response = given().header("Authorization", "bearer " + default_valid_token)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("name", "docu")
                .when()
                .get(BASE_PATH);

        response.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = response.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream().size()).isEqualTo(2);
        assertThat(documents.getStream().stream()).allMatch(el -> el.getName().startsWith("docu"));
    }

    @Test
    @DisplayName("Search criteria. Finds documents by state.")
    public void testSuccessfulSearchCriteriaFindDocumentsByState() {
        Response response = given().header("Authorization", "bearer " + default_valid_token)
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
    @DisplayName("Search criteria. Finds documents by type.")
    public void testSuccessfulSearchCriteriaFindDocumentsByType() {
        Response response = given().header("Authorization", "bearer " + default_valid_token)
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
    @DisplayName("Search criteria. Finds documents by channel.")
    public void testSuccessfulSearchCriteriaFindDocumentsByChannel() {
        Response response = given().header("Authorization", "bearer " + default_valid_token)
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
    @DisplayName("Deletes document by id.")
    public void testSuccessfulDeletesDocumentById() {
        Response deleteResponse = given().header("Authorization", "bearer " + default_valid_token)
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .delete(BASE_PATH + "/" + EXISTING_DOCUMENT_ID);
        deleteResponse.then().statusCode(NO_CONTENT.getStatusCode());

        Response getResponse = given().header("Authorization", "bearer " + default_valid_token)
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH);
        getResponse.then().statusCode(200);
        PageResultDTO<DocumentDetailDTO> documents = getResponse.as(getDocumentDetailDTOTypeRef());
        assertThat(documents.getStream().size()).isEqualTo(1);
    }

    /*
     * @Test
     *
     * @DisplayName("Returns exception when trying to delete document for a nonexistent id.")
     * public void testFailedDeleteDocumentById() {
     * Response deleteResponse = given()
     * .accept(MediaType.APPLICATION_JSON)
     * .when()
     * .delete(BASE_PATH + "/" + NOT_EXISTING_DOCUMENT_ID);
     * deleteResponse.then().statusCode(NOT_FOUND.getStatusCode());
     * RFCProblemDTO rfcProblemDTO = deleteResponse.as(RFCProblemDTO.class);
     * assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("404");
     * assertThat(rfcProblemDTO.getDetail()).isEqualTo("Document with id " + NOT_EXISTING_DOCUMENT_ID + " was not found.");
     * assertThat(rfcProblemDTO.getInstance()).isNull();
     * assertThat(rfcProblemDTO.getTitle()).isEqualTo("TECHNICAL ERROR");
     * assertThat(rfcProblemDTO.getType()).isEqualTo("REST_EXCEPTION");
     * }
     */

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

        Response postResponse = given().header("Authorization", "bearer " + default_valid_token)
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

        Response postResponse = given().header("Authorization", "bearer " + default_valid_token)
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

        Response postResponse = given().header("Authorization", "bearer " + default_valid_token)
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

        Response postResponse = given().header("Authorization", "bearer " + default_valid_token)
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

        Response postResponse = given().header("Authorization", "bearer " + default_valid_token)
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

    /*
     * @Test
     *
     * @DisplayName("Saves Document  without attachments.")
     * public void testFailedCreateDocumentWithoutAttachments() {
     * final String documentName = "TEST_DOCUMENT_NAME";
     * final String channelName = "TEST_CHANNEL_NAME";
     * final Long documentTypeId = 2L;
     * ChannelCreateUpdateDTO channelDTO = new ChannelCreateUpdateDTO();
     * channelDTO.setName(channelName);
     *
     * DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
     * documentCreateDTO.setName(documentName);
     * documentCreateDTO.setTypeId(documentTypeId);
     * documentCreateDTO.setChannel(channelDTO);
     * documentCreateDTO.setAttachments(null);
     *
     * Response postResponse = given()
     * .contentType(MediaType.APPLICATION_JSON)
     * .body(documentCreateDTO)
     * .when()
     * .post(BASE_PATH);
     *
     * postResponse.then().statusCode(BAD_REQUEST.getStatusCode());
     * RFCProblemDTO rfcProblemDTO = postResponse.as(RFCProblemDTO.class);
     * assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("400");
     * assertThat(rfcProblemDTO.getDetail()).isEqualTo("Attachments must not be equal null or empty.");
     * assertThat(rfcProblemDTO.getInstance()).isNull();
     * assertThat(rfcProblemDTO.getTitle()).isEqualTo("TECHNICAL ERROR");
     * assertThat(rfcProblemDTO.getType()).isEqualTo("VALIDATION_EXCEPTION");
     * }
     */
    /*
     * @Test
     *
     * @DisplayName("Saves Document  without mimeType in attachment.")
     * public void testFailedCreateDocumentWithoutMimeTypeInAttachment() {
     * final String documentName = "TEST_DOCUMENT_NAME";
     * final String attachmentName = "TEST_ATTACHMENT_NAME";
     * final String channelName = "TEST_CHANNEL_NAME";
     * final Long documentTypeId = 2L;
     * ChannelCreateUpdateDTO channelDTO = new ChannelCreateUpdateDTO();
     * channelDTO.setName(channelName);
     * AttachmentCreateUpdateDTO attachment = new AttachmentCreateUpdateDTO();
     * attachment.setName(attachmentName);
     * attachment.setMimeTypeId(null);
     * List<AttachmentCreateUpdateDTO> attachments = new ArrayList<>();
     * attachments.add(attachment);
     *
     * DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
     * documentCreateDTO.setName(documentName);
     * documentCreateDTO.setTypeId(documentTypeId);
     * documentCreateDTO.setChannel(channelDTO);
     * documentCreateDTO.setAttachments(attachments);
     *
     * Response postResponse = given()
     * .contentType(MediaType.APPLICATION_JSON)
     * .body(documentCreateDTO)
     * .when()
     * .post(BASE_PATH);
     *
     * postResponse.then().statusCode(BAD_REQUEST.getStatusCode());
     * RFCProblemDTO rfcProblemDTO = postResponse.as(RFCProblemDTO.class);
     * assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("400");
     * assertThat(rfcProblemDTO.getDetail()).contains(".mimeTypeId: must not be null");
     * assertThat(rfcProblemDTO.getInstance()).isNull();
     * assertThat(rfcProblemDTO.getTitle()).isEqualTo("TECHNICAL ERROR");
     * assertThat(rfcProblemDTO.getType()).isEqualTo("VALIDATION_EXCEPTION");
     * }
     */
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

        Response postResponse = given().header("Authorization", "bearer " + default_valid_token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .post(BASE_PATH);

        postResponse.then().statusCode(NOT_FOUND.getStatusCode());
        RFCProblemDTO rfcProblemDTO = postResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("404");
        assertThat(rfcProblemDTO.getDetail()).isEqualTo("Document type of id " + documentTypeId + " does not exist.");
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
        // final String documentSpecificationName = "TEST_SPECIFICATION_NAME";
        // DocumentSpecificationCreateUpdateDTO documentSpecificationCreateUpdateDTO = new DocumentSpecificationCreateUpdateDTO();
        // documentSpecificationCreateUpdateDTO.setName(documentSpecificationName);
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

        Response postResponse = given().header("Authorization", "bearer " + default_valid_token)
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

        Response postResponse = given().header("Authorization", "bearer " + default_valid_token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .post(BASE_PATH);

        postResponse.then().statusCode(NOT_FOUND.getStatusCode());
        RFCProblemDTO rfcProblemDTO = postResponse.as(RFCProblemDTO.class);
        assertThat(rfcProblemDTO.getStatus().toString()).isEqualTo("404");
        assertThat(rfcProblemDTO.getDetail())
                .isEqualTo("Supported mime type of id " + attachmentMimeTypeId + " does not exist.");
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

        Response postResponse = given().header("Authorization", "bearer " + default_valid_token)
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

    /*
     * @Test
     *
     * @DisplayName("Updates basic and required fields in Document.")
     * public void testSuccessfulUpdateBasicAndRequiredFieldsInDocument() {
     * Set<String> tags = new HashSet<>();
     * tags.add("TEST_UPDATE_DOCUMENT_TAG_1");
     * tags.add("TEST_UPDATE_DOCUMENT_TAG_2");
     * final Long documentTypeId = 201L;
     * ChannelCreateUpdateDTO channelDTO = new ChannelCreateUpdateDTO();
     * final String channelName = "TEST_CHANNEL_NAME";
     * channelDTO.setName(channelName);
     * final Long attachmentMimeTypeId = 152L;
     * List<AttachmentCreateUpdateDTO> attachments = new ArrayList<>();
     * AttachmentCreateUpdateDTO attachment = new AttachmentCreateUpdateDTO();
     * final String attachmentName = "TEST_UPDATE_ATTACHMENT_NAME";
     * attachment.setName(attachmentName);
     * attachment.setMimeTypeId(attachmentMimeTypeId);
     * attachments.add(attachment);
     * RelatedObjectRefCreateUpdateDTO relatedObjectRefCreateUpdateDTO = new RelatedObjectRefCreateUpdateDTO();
     * relatedObjectRefCreateUpdateDTO.setInvolvement("TEST_UPDATE");
     *
     * DocumentCreateUpdateDTO documentCreateDTO = new DocumentCreateUpdateDTO();
     * final String documentName = "TEST_UPDATE_DOCUMENT_NAME";
     * final String documentDescription = "TEST_UPDATE_DOCUMENT_DESCRIPTION";
     * final LifeCycleState documentState = LifeCycleState.ARCHIVED;
     * final String documentVersion = "TEST_UPDATE_DOCUMENT_VERSION";
     * documentCreateDTO.setName(documentName);
     * documentCreateDTO.setDescription(documentDescription);
     * documentCreateDTO.setLifeCycleState(documentState);
     * documentCreateDTO.setDocumentVersion(documentVersion);
     * documentCreateDTO.setTags(tags);
     * documentCreateDTO.setTypeId(documentTypeId);
     * documentCreateDTO.setChannel(channelDTO);
     * documentCreateDTO.setAttachments(attachments);
     * documentCreateDTO.setRelatedObject(relatedObjectRefCreateUpdateDTO);
     *
     * Response putResponse = given().header("Authorization","bearer " + default_valid_token)
     * .contentType(MediaType.APPLICATION_JSON)
     * .body(documentCreateDTO)
     * .when()
     * .put(BASE_PATH + "/" + EXISTING_DOCUMENT_ID);
     *
     * putResponse.then().statusCode(201);
     * DocumentDetailDTO documentDetailDTO = putResponse.as(DocumentDetailDTO.class);
     *
     * assertThat(documentDetailDTO.getId()).isEqualTo(EXISTING_DOCUMENT_ID);
     * assertThat(documentDetailDTO.getName()).isEqualTo(documentName);
     * assertThat(documentDetailDTO.getDescription()).isEqualTo(documentDescription);
     * assertThat(documentDetailDTO.getLifeCycleState()).isEqualTo(documentState);
     * assertThat(documentDetailDTO.getDocumentVersion()).isEqualTo(documentVersion);
     * assertThat(documentDetailDTO.getTags().size()).isEqualTo(2);
     * assertThat(documentDetailDTO.getTags().contains("TEST_UPDATE_DOCUMENT_TAG_1")).isTrue();
     * assertThat(documentDetailDTO.getTags().contains("TEST_UPDATE_DOCUMENT_TAG_2")).isTrue();
     * assertThat(documentDetailDTO.getType().getId()).isEqualTo(documentTypeId);
     * assertThat(documentDetailDTO.getSpecification()).isNull();
     * assertThat(documentDetailDTO.getChannel().getId()).isNotNull();
     * assertThat(documentDetailDTO.getChannel().getName()).isEqualTo(channelName);
     * assertThat(documentDetailDTO.getRelatedObject().getId()).isNotNull();
     * assertThat(documentDetailDTO.getRelatedObject().getInvolvement()).isEqualTo("TEST_UPDATE");
     * assertThat(documentDetailDTO.getDocumentRelationships().size()).isEqualTo(1);
     * assertThat(documentDetailDTO.getDocumentRelationships().stream().findFirst().get().getId()).isEqualTo(
     * DOCUMENT_RELATIONSHIP_ID);
     * assertThat(documentDetailDTO.getCharacteristics().size()).isEqualTo(1);
     * assertThat(documentDetailDTO.getCharacteristics().stream().findFirst().get().getId()).isEqualTo(
     * DOCUMENT_CHARACTERISTIC_ID);
     * assertThat(documentDetailDTO.getRelatedParties().size()).isEqualTo(1);
     * assertThat(documentDetailDTO.getRelatedParties().stream().findFirst().get().getId()).isEqualTo(RELATED_PARTY_ID);
     * assertThat(documentDetailDTO.getCategories().size()).isEqualTo(3);
     * assertThat(documentDetailDTO.getAttachments().size()).isEqualTo(3);
     * assertThat(documentDetailDTO.getAttachments().stream()).allMatch(el ->
     * el.getMimeType().getId().equals(attachmentMimeTypeId));
     *
     * documentDetailDTO = given().header("Authorization","bearer " + default_valid_token)
     * .accept(MediaType.APPLICATION_JSON)
     * .when()
     * .get(BASE_PATH + "/" + EXISTING_DOCUMENT_ID)
     * .as(DocumentDetailDTO.class);
     *
     * assertThat(documentDetailDTO.getId()).isEqualTo(EXISTING_DOCUMENT_ID);
     * assertThat(documentDetailDTO.getName()).isEqualTo(documentName);
     * assertThat(documentDetailDTO.getDescription()).isEqualTo(documentDescription);
     * assertThat(documentDetailDTO.getLifeCycleState()).isEqualTo(documentState);
     * assertThat(documentDetailDTO.getDocumentVersion()).isEqualTo(documentVersion);
     * assertThat(documentDetailDTO.getTags().size()).isEqualTo(2);
     * assertThat(documentDetailDTO.getTags().contains("TEST_UPDATE_DOCUMENT_TAG_1")).isTrue();
     * assertThat(documentDetailDTO.getTags().contains("TEST_UPDATE_DOCUMENT_TAG_2")).isTrue();
     * assertThat(documentDetailDTO.getType().getId()).isEqualTo(documentTypeId);
     * assertThat(documentDetailDTO.getSpecification()).isNull();
     * assertThat(documentDetailDTO.getChannel().getId()).isNotNull();
     * assertThat(documentDetailDTO.getChannel().getName()).isEqualTo(channelName);
     * assertThat(documentDetailDTO.getRelatedObject().getId()).isNotNull();
     * assertThat(documentDetailDTO.getRelatedObject().getInvolvement()).isEqualTo("TEST_UPDATE");
     * assertThat(documentDetailDTO.getDocumentRelationships().size()).isEqualTo(1);
     * assertThat(documentDetailDTO.getDocumentRelationships().stream().findFirst().get().getId()).isEqualTo(
     * DOCUMENT_RELATIONSHIP_ID);
     * assertThat(documentDetailDTO.getCharacteristics().size()).isEqualTo(1);
     * assertThat(documentDetailDTO.getCharacteristics().stream().findFirst().get().getId()).isEqualTo(
     * DOCUMENT_CHARACTERISTIC_ID);
     * assertThat(documentDetailDTO.getRelatedParties().size()).isEqualTo(1);
     * assertThat(documentDetailDTO.getRelatedParties().stream().findFirst().get().getId()).isEqualTo(RELATED_PARTY_ID);
     * assertThat(documentDetailDTO.getCategories().size()).isEqualTo(3);
     * assertThat(documentDetailDTO.getAttachments().size()).isEqualTo(1);
     * assertThat(documentDetailDTO.getAttachments().stream()).allMatch(el ->
     * el.getMimeType().getId().equals(attachmentMimeTypeId));
     * }
     */
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
        existingCategory.setId("1");
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

        Response putResponse = given().header("Authorization", "bearer " + default_valid_token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentCreateDTO)
                .when()
                .put(BASE_PATH + "/" + EXISTING_DOCUMENT_ID);

        putResponse.then().statusCode(201);

        DocumentDetailDTO documentDetailDTO = given().header("Authorization", "bearer " + default_valid_token)
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

        assertThat(documentDetailDTO.getCategories().size()).isEqualTo(2);
        List<CategoryDTO> listCategories1 = documentDetailDTO.getCategories()
                .stream().filter(p -> p.getId().equals("1")).collect(Collectors.toList());
        assertThat(listCategories1.size()).isEqualTo(1);
        CategoryDTO existingCategoryDTO = listCategories1.get(0);
        assertThat(existingCategoryDTO.getName()).isEqualTo("TEST_Name_1");
        List<CategoryDTO> listCategories2 = documentDetailDTO.getCategories()
                .stream().filter(p -> !p.getId().equals("1")).collect(Collectors.toList());
        assertThat(listCategories2.size()).isEqualTo(1);
        CategoryDTO newCategoryDTO = listCategories2.get(0);
        assertThat(newCategoryDTO.getName()).isEqualTo("TEST_Name_2");

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
        Response putResponse = given().header("Authorization", "bearer " + default_valid_token)
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
        Response getResponse = given().header("Authorization", "bearer " + default_valid_token)
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH + "/channels");
        getResponse.then().statusCode(OK.getStatusCode());

        List<ChannelDTO> channels = getResponse.as(getChannelDTOTypeRef());
        assertThat(channels.size()).isEqualTo(2);
    }

    private TypeRef<List<ChannelDTO>> getChannelDTOTypeRef() {
        return new TypeRef<>() {
        };
    }

    private TypeRef<PageResultDTO<DocumentDetailDTO>> getDocumentDetailDTOTypeRef() {
        return new TypeRef<>() {
        };

    }
}
