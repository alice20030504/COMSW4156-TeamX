package com.teamx.fitness.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Fitness Management Service API")
                .version("1.0.0")
                .description(
                    "REST API for fitness management supporting multiple client types.\n\n"
                        + "## Client Authentication\n"
                        + "All API endpoints (except root and Swagger UI) require the"
                        + " `X-Client-ID` header.\n\n"
                        + "### Client ID Format:\n"
                        + "- **Mobile App Clients**: `mobile-*` (e.g., `mobile-app1`,"
                        + " `mobile-app2`)\n"
                        + "- **Research Tool Clients**: `research-*` (e.g., `research-tool1`)\n\n"
                        + "### Access Control:\n"
                        + "- **Mobile clients**: Can access `/api/persons` endpoints. Cannot access"
                        + " `/api/research` endpoints (403 Forbidden).\n"
                        + "- **Research clients**: Can access both `/api/persons` and `/api/research`"
                        + " endpoints.\n\n"
                        + "### Data Isolation:\n"
                        + "Each client's data is isolated. A client can only view, create, update, or"
                        + " delete their own data."))
        .components(
            new Components()
                .addSecuritySchemes(
                    "ClientId",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Client-ID")
                        .description(
                            "Client identification header. Format: 'mobile-*' for mobile clients or"
                                + " 'research-*' for research tools.")))
        .addSecurityItem(new SecurityRequirement().addList("ClientId"));
  }

  @Bean
  public OperationCustomizer customizeOperations() {
    return (operation, handlerMethod) -> {
      // Add X-Client-ID as a required parameter to all operations
      if (operation.getParameters() == null) {
        operation.setParameters(new java.util.ArrayList<>());
      }

      // Check if X-Client-ID parameter is not already added
      boolean hasClientIdParam =
          operation.getParameters().stream()
              .anyMatch(p -> "X-Client-ID".equals(p.getName()));

      if (!hasClientIdParam) {
        Parameter clientIdParam =
            new Parameter()
                .in("header")
                .name("X-Client-ID")
                .description("Client identification (mobile-* or research-*)")
                .required(true)
                .example("mobile-app1")
                .schema(new io.swagger.v3.oas.models.media.StringSchema());
        operation.addParametersItem(clientIdParam);
      }

      return operation;
    };
  }
}
