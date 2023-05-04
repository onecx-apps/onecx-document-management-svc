package org.tkit.document.management.domain.daos;

import javax.enterprise.context.ApplicationScoped;

import org.tkit.document.management.domain.models.entities.DocumentSpecification;
import org.tkit.quarkus.jpa.daos.AbstractDAO;

/**
 * DocumentSpecificationDAO class.
 */
@ApplicationScoped
public class DocumentSpecificationDAO extends AbstractDAO<DocumentSpecification> {
}
