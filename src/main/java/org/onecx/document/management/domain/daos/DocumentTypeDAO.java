package org.onecx.document.management.domain.daos;

import javax.enterprise.context.ApplicationScoped;

import org.onecx.document.management.domain.models.entities.DocumentType;
import org.tkit.quarkus.jpa.daos.AbstractDAO;

/**
 * DocumentTypeDAO class.
 */
@ApplicationScoped
public class DocumentTypeDAO extends AbstractDAO<DocumentType> {
}
