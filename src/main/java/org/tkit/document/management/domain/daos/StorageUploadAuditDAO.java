package org.tkit.document.management.domain.daos;

import org.tkit.document.management.domain.models.entities.StorageUploadAudit;
import org.tkit.quarkus.jpa.daos.AbstractDAO;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

import static org.tkit.document.management.domain.models.entities.StorageUploadAudit_.DOCUMENT_ID;

@ApplicationScoped
public class StorageUploadAuditDAO extends AbstractDAO<StorageUploadAudit>{

    public List<StorageUploadAudit> findFailedAttachmentsByDocumentId(String documentId)
    {
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<StorageUploadAudit> criteriaQuery = criteriaBuilder.createQuery(StorageUploadAudit.class);
        Root<StorageUploadAudit> root = criteriaQuery.from(StorageUploadAudit.class);
        criteriaQuery.where(criteriaBuilder.equal(root.get(DOCUMENT_ID), documentId));
        TypedQuery<StorageUploadAudit> typedQuery = em.createQuery(criteriaQuery);
        return typedQuery.getResultList();
    }

}
