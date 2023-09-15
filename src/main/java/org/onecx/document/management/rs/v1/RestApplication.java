package org.onecx.document.management.rs.v1;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlow;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlows;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@OpenAPIDefinition(info = @Info(title = "Document-Management API", description = "OneCX document management", version = "1.0"), security = @SecurityRequirement(name = "OIDC"), components = @Components(securitySchemes = {
        @SecurityScheme(securitySchemeName = "OIDC", type = SecuritySchemeType.OAUTH2, flows = @OAuthFlows(password = @OAuthFlow(tokenUrl = "http://keycloak-app/realms/OneCX/protocol/openid-connect/token"))) }))
@ApplicationPath("/")
public class RestApplication extends Application {
}