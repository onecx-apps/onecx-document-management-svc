package org.onecx.document.management.domain.daos;

import jakarta.enterprise.context.ApplicationScoped;

import org.onecx.document.management.domain.models.entities.DocumentTypes;
import org.tkit.quarkus.jpa.daos.AbstractDAO;

/**
 * DocumentTypeDAO class.
 */
@ApplicationScoped
public class DocumentTypeDAO extends AbstractDAO<DocumentTypes> {
}
