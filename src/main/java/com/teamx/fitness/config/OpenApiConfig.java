package com.teamx.fitness.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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
                        + "Register a profile with five core fields (`name`, `weight`, `height`, "
                        + "`birthDate`, `goal`) plus `gender`, then use the generated client ID to "
                        + "access personal endpoints.\n\n"
                        + "### Calling the API\n"
                        + "- `POST /api/persons` does **not** require the `X-Client-ID` header. "
                        + "The service returns a unique ID such as `mobile-id7`.\n"
                        + "- All other personal endpoints expect that ID in the `X-Client-ID` header.\n"
                        + "- Research endpoints remain unchanged and still use the `X-Client-ID` "
                        + "prefix rules (mobile vs research).\n\n"
                        + "Swagger no longer injects authorization dialogs or extra headers; copy "
                        + "the generated ID into the header when exploring protected endpoints."));
  }
}

