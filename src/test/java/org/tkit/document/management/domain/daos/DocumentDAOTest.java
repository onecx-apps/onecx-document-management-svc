package org.tkit.document.management.domain.daos;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tkit.document.management.domain.criteria.DocumentSearchCriteria;
import org.tkit.quarkus.jpa.exceptions.DAOException;

public class DocumentDAOTest {

    @Test
    @DisplayName("Throw DAOException for criteria equal null.")
    public void shouldThrowDAOExceptionWhenTryFindDocumentsByCriteriaEqualNull() {
        //given
        DocumentDAO documentDAO = new DocumentDAO() {
            @Override
            protected EntityManager getEntityManager() {
                return null;
            }
        };
        DocumentSearchCriteria searchCriteria = null;
        //when
        DAOException thrownException = Assertions.assertThrows(DAOException.class, () -> {
            documentDAO.findBySearchCriteria(searchCriteria);
        });
        //then
        assertThat(thrownException.key).isEqualTo(DocumentDAO.ErrorKeys.ERROR_FIND_DOCUMENT_SEARCH_CRITERIA_REQUIRED);
    }

    @Test
    @DisplayName("Throw DAOException for empty criteria.")
    public void shouldThrowDAOExceptionWhenTryFindDocumentByEmptyCriteria() {
        //given
        DocumentDAO documentDAO = new DocumentDAO() {
            @Override
            protected EntityManager getEntityManager() {
                return null;
            }
        };
        DocumentSearchCriteria searchCriteria = new DocumentSearchCriteria();
        //when
        DAOException thrownException = Assertions.assertThrows(DAOException.class, () -> {
            documentDAO.findBySearchCriteria(searchCriteria);
        });
        //then
        assertThat(thrownException.key).isEqualTo(DocumentDAO.ErrorKeys.ERROR_FIND_DOCUMENT_BY_CRITERIA);
    }
}
