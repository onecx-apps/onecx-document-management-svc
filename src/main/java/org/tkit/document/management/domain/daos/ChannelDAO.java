package org.tkit.document.management.domain.daos;

import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.tkit.document.management.domain.models.entities.Channel;
import org.tkit.quarkus.jpa.daos.AbstractDAO;

/**
 * ChannelDAO class.
 */
@ApplicationScoped
public class ChannelDAO extends AbstractDAO<Channel> {

    public Stream<Channel> findAllSortedByNameAsc() {
        var criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Channel> criteriaQuery = criteriaBuilder.createQuery(Channel.class);
        Root<Channel> root = criteriaQuery.from(Channel.class);
        criteriaQuery.orderBy(criteriaBuilder.asc(root.get("name")));
        TypedQuery<Channel> typedQuery = em.createQuery(criteriaQuery);
        return typedQuery.getResultList().stream();
    }

}
