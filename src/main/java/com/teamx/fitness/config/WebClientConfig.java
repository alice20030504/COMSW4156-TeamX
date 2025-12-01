package com.teamx.fitness.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for WebClient used to call external services.
 * 
 * <p>Provides a WebClient.Builder bean for creating WebClient instances
 * to communicate with the Python mealplan service.</p>
 */
@Configuration
public class WebClientConfig {

  /**
   * Creates a WebClient.Builder bean for building WebClient instances.
   * Spring Boot auto-configures this, but we provide it explicitly for clarity.
   *
   * @return WebClient.Builder instance
   */
  @Bean
  public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
  }
}

