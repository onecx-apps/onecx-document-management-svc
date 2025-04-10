package org.onecx.document.management.domain.daos;

import jakarta.enterprise.context.ApplicationScoped;

import org.onecx.document.management.domain.models.entities.SupportedMimeTypes;
import org.tkit.quarkus.jpa.daos.AbstractDAO;

/**
 * SupportedMimeTypeDAO class.
 */
@ApplicationScoped
public class SupportedMimeTypeDAO extends AbstractDAO<SupportedMimeTypes> {
}
