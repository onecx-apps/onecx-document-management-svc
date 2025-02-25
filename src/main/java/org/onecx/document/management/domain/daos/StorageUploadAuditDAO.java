package org.onecx.document.management.domain.daos;

import static org.onecx.document.management.domain.models.entities.StorageUploadAudit_.DOCUMENT_ID;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.onecx.document.management.domain.models.entities.StorageUploadAudit;
import org.tkit.quarkus.jpa.daos.AbstractDAO;

@ApplicationScoped
public class StorageUploadAuditDAO extends AbstractDAO<StorageUploadAudit> {

    public List<StorageUploadAudit> findFailedAttachmentsByDocumentId(String documentId) {
        var criteriaBuilder = this.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<StorageUploadAudit> criteriaQuery = criteriaBuilder.createQuery(StorageUploadAudit.class);
        Root<StorageUploadAudit> root = criteriaQuery.from(StorageUploadAudit.class);
        criteriaQuery.where(criteriaBuilder.equal(root.get(DOCUMENT_ID), documentId));
        TypedQuery<StorageUploadAudit> typedQuery = em.createQuery(criteriaQuery);
        return typedQuery.getResultList();
    }

}
