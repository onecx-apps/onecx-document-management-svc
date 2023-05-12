package org.tkit.document.management.domain.daos;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.tkit.document.management.domain.models.entities.Attachment;
import org.tkit.document.management.domain.models.entities.Attachment_;
import org.tkit.document.management.domain.models.entities.SupportedMimeType;
import org.tkit.document.management.domain.models.entities.SupportedMimeType_;
import org.tkit.quarkus.jpa.daos.AbstractDAO;

/**
 * AttachmentDAO class.
 */
@ApplicationScoped
public class AttachmentDAO extends AbstractDAO<Attachment> {

    /**
     *
     * @param id the String
     * @return a {@link List<Attachment>} contains given {@link SupportedMimeType} id
     */
    public List<Attachment> findAttachmentsWithSupportedMimeTypeId(String id) {
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Attachment> criteriaQuery = criteriaBuilder.createQuery(Attachment.class);
        Root<Attachment> root = criteriaQuery.from(Attachment.class);
        criteriaQuery.where(
                criteriaBuilder.equal(root.get(Attachment_.MIME_TYPE).get(SupportedMimeType_.ID), id));
        TypedQuery<Attachment> typedQuery = em.createQuery(criteriaQuery);
        return typedQuery.getResultList();
    }
}
