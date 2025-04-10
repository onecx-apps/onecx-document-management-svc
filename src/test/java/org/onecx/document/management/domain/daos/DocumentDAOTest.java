package org.onecx.document.management.domain.daos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onecx.document.management.domain.criteria.DocumentSearchCriterias;
import org.tkit.quarkus.jpa.exceptions.DAOException;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class DocumentDAOTest {

    @Test
    @DisplayName("Throw DAOException for criteria equal null.")
    void shouldThrowDAOExceptionWhenTryFindDocumentsByCriteriaEqualNull() {
        // given
        DocumentDAO documentDAO = new DocumentDAO() {
            @Override
            protected EntityManager getEntityManager() {
                return null;
            }
        };
        DocumentSearchCriterias searchCriteria = null;
        // when
        DAOException thrownException = Assertions.assertThrows(DAOException.class, () -> {
            documentDAO.findBySearchCriteria(searchCriteria);
        });
        // then
        assertThat(thrownException.key).isEqualTo(DocumentDAO.ErrorKeys.ERROR_FIND_DOCUMENT_SEARCH_CRITERIA_REQUIRED);
    }

    @Test
    @DisplayName("Throw DAOException for empty criteria.")
    void shouldThrowDAOExceptionWhenTryFindDocumentByEmptyCriteria() {
        // given
        DocumentDAO documentDAO = new DocumentDAO() {
            @Override
            protected EntityManager getEntityManager() {
                return null;
            }
        };
        DocumentSearchCriterias searchCriteria = new DocumentSearchCriterias();
        // when
        DAOException thrownException = Assertions.assertThrows(DAOException.class, () -> {
            documentDAO.findBySearchCriteria(searchCriteria);
        });
        // then
        assertThat(thrownException.key).isEqualTo(DocumentDAO.ErrorKeys.ERROR_FIND_DOCUMENT_BY_CRITERIA);
    }

    @Test
    @DisplayName("Search criteria. Fails to perform a search with null criteria.")
    void testFailedSearchWithNullCriteria() {
        DocumentDAO documentDAO = new DocumentDAO() {
            @Override
            protected EntityManager getEntityManager() {
                return null;
            }
        };
        DocumentSearchCriterias criteria = null;
        DAOException exception = assertThrows(DAOException.class,
                () -> documentDAO.findBySearchCriteria(criteria));
        assertEquals(DocumentDAO.ErrorKeys.ERROR_FIND_DOCUMENT_SEARCH_CRITERIA_REQUIRED, exception.getMessageKey());
    }

    @Test
    @DisplayName("Search criteria. Fails to perform a search of all documents with null criteria.")
    void testFailedShowAllDocumentsWithNullCriteria() {
        DocumentDAO documentDAO = new DocumentDAO() {
            @Override
            protected EntityManager getEntityManager() {
                return null;
            }
        };
        DocumentSearchCriterias criteria = null;
        DAOException exception = assertThrows(DAOException.class,
                () -> documentDAO.findAllDocumentsBySearchCriteria(criteria));
        assertEquals(DocumentDAO.ErrorKeys.ERROR_FIND_DOCUMENT_SEARCH_CRITERIA_REQUIRED, exception.getMessageKey());
    }

    @Test
    @DisplayName("Search criteria. Test fails when we mock an exception.")
    void testFailedSearchWithCriteriaMockException() {
        DocumentDAO documentDAO = new DocumentDAO() {
            @Override
            protected EntityManager getEntityManager() {
                return null;
            }
        };
        DocumentSearchCriterias criteria = new DocumentSearchCriterias();

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
    void testFailedShowAllDocumentsWithCriteriaMockException() {
        DocumentDAO documentDAO = new DocumentDAO() {
            @Override
            protected EntityManager getEntityManager() {
                return null;
            }
        };
        DocumentSearchCriterias criteria = new DocumentSearchCriterias();

        EntityManager entityManagerMock = Mockito.mock(EntityManager.class);
        Mockito.when(entityManagerMock.getCriteriaBuilder()).thenThrow(new RuntimeException());

        DAOException exception = assertThrows(DAOException.class,
                () -> documentDAO.findAllDocumentsBySearchCriteria(criteria));

        assertEquals(DocumentDAO.ErrorKeys.ERROR_FIND_DOCUMENT_BY_CRITERIA, exception.getMessageKey());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof RuntimeException);
    }
}
