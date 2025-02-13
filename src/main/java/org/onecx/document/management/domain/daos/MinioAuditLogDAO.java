package org.onecx.document.management.domain.daos;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;

import org.onecx.document.management.domain.models.entities.MinioAuditLog;
import org.tkit.quarkus.jpa.daos.AbstractDAO;

@ApplicationScoped
public class MinioAuditLogDAO extends AbstractDAO<MinioAuditLog> {

    /**
     * executes a select query to return all records present in the MinioAuditLog
     * table
     *
     * @return List of records as MinioAuditLog object
     */
    public List<MinioAuditLog> getAllRecords() {
        var criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<MinioAuditLog> selectQuery = criteriaBuilder.createQuery(MinioAuditLog.class);
        selectQuery.from(MinioAuditLog.class);
        TypedQuery<MinioAuditLog> typedQuery = em.createQuery(selectQuery);
        return typedQuery.getResultList();
    }
}
