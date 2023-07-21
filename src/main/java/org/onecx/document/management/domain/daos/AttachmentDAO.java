package org.onecx.document.management.domain.daos;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.onecx.document.management.domain.models.entities.Attachment;
import org.onecx.document.management.domain.models.entities.Attachment_;
import org.onecx.document.management.domain.models.entities.SupportedMimeType;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

/**
 * AttachmentDAO class.
 */
@ApplicationScoped
public class AttachmentDAO extends AbstractDAO<Attachment> {

    /**
     *
     * @param id the String
     * @return a {@link List<Attachment>} contains given {@link SupportedMimeType}
     *         id
     */
    public List<Attachment> findAttachmentsWithSupportedMimeTypeId(String id) {
        var criteriaBuilder = this.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Attachment> criteriaQuery = criteriaBuilder.createQuery(Attachment.class);
        Root<Attachment> root = criteriaQuery.from(Attachment.class);
        criteriaQuery.where(
                criteriaBuilder.equal(root.get(Attachment_.MIME_TYPE).get(TraceableEntity_.ID), id));
        TypedQuery<Attachment> typedQuery = em.createQuery(criteriaQuery);
        return typedQuery.getResultList();
    }

    public void deleteAttachmentsBasedOnFileUploadStatus() {
        var criteriaBuilder = em.getCriteriaBuilder();
        CriteriaDelete<Attachment> deleteQuery = criteriaBuilder.createCriteriaDelete(Attachment.class);
        Root<Attachment> root = deleteQuery.from(Attachment.class);
        deleteQuery.where(
                criteriaBuilder.equal(root.get(Attachment_.STORAGE_UPLOAD_STATUS), false));
        em.createQuery(deleteQuery).executeUpdate();
    }

}
