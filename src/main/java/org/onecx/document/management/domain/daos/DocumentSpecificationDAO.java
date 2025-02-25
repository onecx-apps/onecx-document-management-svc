package org.onecx.document.management.domain.daos;

import jakarta.enterprise.context.ApplicationScoped;

import org.onecx.document.management.domain.models.entities.DocumentSpecification;
import org.tkit.quarkus.jpa.daos.AbstractDAO;

/**
 * DocumentSpecificationDAO class.
 */
@ApplicationScoped
public class DocumentSpecificationDAO extends AbstractDAO<DocumentSpecification> {
}
