package org.onecx.document.management.domain.daos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.onecx.document.management.domain.criteria.DocumentSearchCriterias;
import org.onecx.document.management.domain.models.entities.*;
import org.onecx.document.management.domain.models.entities.DocumentSpecifications;
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
     * Finds a {@link PageResult} of {@link Document} matching the given
     * {@link DocumentSearchCriterias}.
     *
     * @param criteria the {@link DocumentSearchCriterias}
     * @return the {@link PageResult} of {@link Document}
     */
    public PageResult<Document> findBySearchCriteria(DocumentSearchCriterias criteria) {
        if (criteria == null) {
            throw new DAOException(ErrorKeys.ERROR_FIND_DOCUMENT_SEARCH_CRITERIA_REQUIRED, new NullPointerException());
        }
        try {
            CriteriaQuery<Document> cq = createSearchCriteriaQuery(criteria);
            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();

        } catch (Exception exception) {
            throw new DAOException(ErrorKeys.ERROR_FIND_DOCUMENT_BY_CRITERIA, exception);
        }
    }

    /**
     *
     * @param id the String
     * @return a {@link Document} with all fields. Including these marked as lazy
     *         fetched.
     */
    public Document findDocumentById(String id) {
        EntityGraph<Document> entityGraph = (EntityGraph<Document>) em.getEntityGraph("Document.loadAll");
        return findById(id, entityGraph);
    }

    /**
     *
     * @param id the String
     * @return a {@link List<Document>} contains given {@link DocumentTypes} id
     */
    public List<Document> findDocumentsWithDocumentTypeId(String id) {
        var criteriaBuilder = this.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Document> criteriaQuery = criteriaBuilder.createQuery(Document.class);
        Root<Document> root = criteriaQuery.from(Document.class);
        criteriaQuery.where(
                criteriaBuilder.equal(root.get(Document_.TYPE).get(TraceableEntity_.ID), id));
        TypedQuery<Document> typedQuery = em.createQuery(criteriaQuery);
        return typedQuery.getResultList();
    }

    /**
     *
     * @param id the String
     * @return a {@link List<Document>} contains given {@link DocumentSpecifications}
     *         id
     */
    public List<Document> findDocumentsWithDocumentSpecificationId(String id) {
        var criteriaBuilder = this.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Document> criteriaQuery = criteriaBuilder.createQuery(Document.class);
        Root<Document> root = criteriaQuery.from(Document.class);
        criteriaQuery.where(
                criteriaBuilder.equal(root.get(Document_.SPECIFICATION).get(TraceableEntity_.ID), id));
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

    private CriteriaQuery<Document> createSearchCriteriaQuery(DocumentSearchCriterias criteria) {

        var entityManager = getEntityManager();
        var cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Document> cq = cb.createQuery(Document.class);
        Root<Document> root = cq.from(Document.class);
        List<Predicate> predicates = new ArrayList<>();
        cq.orderBy(cb.desc(root.get(AbstractTraceableEntity_.MODIFICATION_DATE)));
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
            predicates.add(root.get(Document_.TYPE).get(TraceableEntity_.ID).in(criteria.getDocumentTypeId()));
        }
        if (isNotEmpty(criteria.getChannelName())) {
            predicates.add(cb.equal(cb.lower(root.get(Document_.CHANNEL).get(Channel_.NAME)),
                    criteria.getChannelName().toLowerCase()));
        }
        if (Objects.nonNull(criteria.getStartDate())) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(AbstractTraceableEntity_.CREATION_DATE),
                    (criteria.getStartDate())));
        }
        if (Objects.nonNull(criteria.getEndDate())) {
            predicates.add(cb.lessThanOrEqualTo(root.get(AbstractTraceableEntity_.CREATION_DATE),
                    (criteria.getEndDate())));
        }
        if (Objects.nonNull(criteria.getCreateBy())) {
            predicates.add(cb.equal(root.get(AbstractTraceableEntity_.CREATION_USER), criteria.getCreateBy()));
        }

        if (isNotEmpty(criteria.getObjectReferenceId())) {
            predicates.add(
                    cb.like(cb.lower(root.get(Document_.RELATED_OBJECT).get(RelatedObjectRef_.OBJECT_REFERENCE_ID)),
                            stringPattern(criteria.getObjectReferenceId())));
        }
        if (isNotEmpty(criteria.getObjectReferenceType())) {
            predicates.add(cb.like(
                    cb.lower(root.get(Document_.RELATED_OBJECT).get(RelatedObjectRef_.OBJECT_REFERENCE_TYPE)),
                    stringPattern(criteria.getObjectReferenceType())));
        }

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return cq;

    }

    public List<Document> findAllDocumentsBySearchCriteria(DocumentSearchCriterias criteria) {
        if (criteria == null) {
            throw new DAOException(ErrorKeys.ERROR_FIND_DOCUMENT_SEARCH_CRITERIA_REQUIRED, new NullPointerException());
        }
        try {
            CriteriaQuery<Document> cq = createSearchCriteriaQuery(criteria);
            TypedQuery<Document> typedQuery = em.createQuery(cq);
            return typedQuery.getResultList();
        } catch (Exception exception) {
            throw new DAOException(ErrorKeys.ERROR_FIND_DOCUMENT_BY_CRITERIA, exception);
        }
    }
}
