package org.onecx.document.management.rs.v1;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

@OpenAPIDefinition(info = @Info(title = "document-management", description = "1000kit document management", version = "1.0"))
@ApplicationPath("/")
public class JaxRsActivator extends Application {
}
