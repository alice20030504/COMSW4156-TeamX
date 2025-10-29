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
                    "## Personal Fitness Management API\n\n"
                        + "A comprehensive REST API for personal fitness management supporting "
                        + "multiple client types.\n\n"
                        + "### Authentication & Access Control\n\n"
                        + "**Client Authentication:**\n"
                        + "- All API endpoints require the `X-Client-ID` header\n"
                        + "- Mobile App Clients: `mobile-*` (e.g., `mobile-app1`, `mobile-app2`)\n"
                        + "- Research Tool Clients: `research-*` (e.g., `research-tool1`)\n\n"
                        + "**User Authentication:**\n"
                        + "- Personal endpoints require user ID and birth date for authentication\n"
                        + "- Only authenticated users can access their own data\n\n"
                        + "### API Endpoint Groups\n\n"
                        + "**Personal Controller:**\n"
                        + "- User account management (create, read, update, delete)\n"
                        + "- Health calculations (BMI, calories, age)\n"
                        + "- Personal health check\n"
                        + "- Access: Mobile clients + authenticated users\n\n"
                        + "**Research Controller:**\n"
                        + "- Aggregated, anonymized data for research\n"
                        + "- Demographic statistics and trends\n"
                        + "- Population health metrics\n"
                        + "- Access: Research clients only (403 Forbidden for mobile clients)\n\n"
                        + "### Data Protection\n"
                        + "- Data Isolation: Each client can only access their own data\n"
                        + "- Privacy: Research endpoints return anonymized data only\n"
                        + "- No PII: Personally Identifiable Information is never exposed in research endpoints"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "ClientId",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Client-ID")
                        .description(
                            "Client identification header required for all endpoints.\n\n"
                                + "Format:\n"
                                + "- Mobile clients: `mobile-*` (e.g., `mobile-app1`)\n"
                                + "- Research clients: `research-*` (e.g., `research-tool1`)\n\n"
                                + "Access Control:\n"
                                + "- Mobile clients: Personal endpoints only\n"
                                + "- Research clients: All endpoints")))
        .addSecurityItem(new SecurityRequirement().addList("ClientId"));
  }

  @Bean
  public OperationCustomizer customizeOperations() {
    return (operation, handlerMethod) -> {
      if (operation.getParameters() == null) {
        operation.setParameters(new java.util.ArrayList<>());
      }

      boolean hasClientIdParam =
          operation.getParameters().stream().anyMatch(p -> "X-Client-ID".equals(p.getName()));

      if (!hasClientIdParam) {
        Parameter clientIdParam =
            new Parameter()
                .in("header")
                .name("X-Client-ID")
                .description("Client identification header (mobile-* or research-*)")
                .required(true)
                .example("mobile-app1")
                .schema(new io.swagger.v3.oas.models.media.StringSchema());
        operation.addParametersItem(clientIdParam);
      }

      return operation;
    };
  }
}

