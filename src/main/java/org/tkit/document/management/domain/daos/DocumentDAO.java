package org.tkit.document.management.domain.daos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.tkit.document.management.domain.criteria.DocumentSearchCriteria;
import org.tkit.document.management.domain.models.entities.*;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.AbstractTraceableEntity_;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

/**
 * DocumentDAO class.
 */
@ApplicationScoped
public class DocumentDAO extends AbstractDAO<Document> {

    public enum ErrorKeys {
        ERROR_FIND_DOCUMENT_BY_CRITERIA,
        ERROR_FIND_DOCUMENT_SEARCH_CRITERIA_REQUIRED;
    }

    /**
     * Finds a {@link PageResult} of {@link Document} matching the given {@link DocumentSearchCriteria}.
     *
     * @param criteria the {@link DocumentSearchCriteria}
     * @return the {@link PageResult} of {@link Document}
     */
    public PageResult<Document> findBySearchCriteria(DocumentSearchCriteria criteria) {
        if (criteria == null) {
            throw new DAOException(ErrorKeys.ERROR_FIND_DOCUMENT_SEARCH_CRITERIA_REQUIRED, new NullPointerException());
        }
        try {
            EntityManager entityManager = getEntityManager();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Document> cq = cb.createQuery(Document.class);
            Root<Document> root = cq.from(Document.class);

            List<Predicate> predicates = new ArrayList<>();

            if (Objects.nonNull(criteria.getId())) {
                predicates.add(cb.equal(root.get(TraceableEntity_.ID), criteria.getId()));
            }
            if (isNotEmpty(criteria.getName())) {
                predicates.add(cb.like(cb.lower(root.get(Document_.NAME)), stringPattern(criteria.getName())));
            }
            if (!criteria.getLifeCycleState().isEmpty()) {
                predicates.add(root.get(Document_.LIFE_CYCLE_STATE).in(criteria.getLifeCycleState()));
            }
            if (!criteria.getDocumentTypeId().isEmpty()) {
                predicates.add(root.get(Document_.TYPE).get(DocumentType_.ID).in(criteria.getDocumentTypeId()));
            }
            if (isNotEmpty(criteria.getChannelName())) {
                predicates.add(cb.equal(cb.lower(root.get(Document_.CHANNEL).get(Channel_.NAME)),
                        criteria.getChannelName().toLowerCase()));
            }
            if (Objects.nonNull(criteria.getStartDate())) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get(AbstractTraceableEntity_.CREATION_DATE), (criteria.getStartDate())));
            }
            if (Objects.nonNull(criteria.getEndDate())) {
                predicates.add(cb.lessThanOrEqualTo(root.get(AbstractTraceableEntity_.CREATION_DATE), (criteria.getEndDate())));
            }
            if (Objects.nonNull(criteria.getCreateBy())) {
                predicates.add(cb.equal(root.get(AbstractTraceableEntity_.CREATION_USER), criteria.getCreateBy()));
            }

            if (isNotEmpty(criteria.getObjectReferenceId())) {
                predicates.add(cb.like(cb.lower(root.get(Document_.RELATED_OBJECT).get(RelatedObjectRef_.OBJECT_REFERENCE_ID)),
                        stringPattern(criteria.getObjectReferenceId())));
            }
            if (isNotEmpty(criteria.getObjectReferenceType())) {
                predicates
                        .add(cb.like(cb.lower(root.get(Document_.RELATED_OBJECT).get(RelatedObjectRef_.OBJECT_REFERENCE_TYPE)),
                                stringPattern(criteria.getObjectReferenceType())));
            }

            if (!predicates.isEmpty()) {
                cq.where(cb.and(predicates.toArray(new Predicate[0])));
            }

            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();

        } catch (Exception exception) {
            throw new DAOException(ErrorKeys.ERROR_FIND_DOCUMENT_BY_CRITERIA, exception);
        }
    }

    /**
     *
     * @param id the String
     * @return a {@link Document} with all fields. Including these marked as lazy fetched.
     */
    public Document findDocumentById(String id) {
        EntityGraph entityGraph = em.getEntityGraph("Document.loadAll");
        return findById(id, entityGraph);
    }

    /**
     *
     * @param id the String
     * @return a {@link List<Document>} contains given {@link DocumentType} id
     */
    public List<Document> findDocumentsWithDocumentTypeId(String id) {
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Document> criteriaQuery = criteriaBuilder.createQuery(Document.class);
        Root<Document> root = criteriaQuery.from(Document.class);
        criteriaQuery.where(
                criteriaBuilder.equal(root.get(Document_.TYPE).get(DocumentType_.ID), id));
        TypedQuery<Document> typedQuery = em.createQuery(criteriaQuery);
        return typedQuery.getResultList();
    }

    /**
     *
     * @param id the String
     * @return a {@link List<Document>} contains given {@link DocumentSpecification} id
     */
    public List<Document> findDocumentsWithDocumentSpecificationId(String id) {
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Document> criteriaQuery = criteriaBuilder.createQuery(Document.class);
        Root<Document> root = criteriaQuery.from(Document.class);
        criteriaQuery.where(
                criteriaBuilder.equal(root.get(Document_.SPECIFICATION).get(DocumentSpecification_.ID), id));
        TypedQuery<Document> typedQuery = em.createQuery(criteriaQuery);
        return typedQuery.getResultList();
    }

    /**
     *
     * @param value the String
     * @return true if value is not null and not blank.
     */
    private boolean isNotEmpty(String value) {
        return value != null && !value.isBlank();
    }

    /**
     *
     * @param value the String
     * @return String in lower case and matches after the first letters.
     */
    private String stringPattern(String value) {
        return (value.toLowerCase() + "%");
    }
}
