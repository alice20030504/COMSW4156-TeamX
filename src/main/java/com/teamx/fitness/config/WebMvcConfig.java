package com.teamx.fitness.config;

import com.teamx.fitness.logging.ApiLoggingInterceptor;
import com.teamx.fitness.security.ClientIdInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for CORS and request interceptors.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  /** Cache max age for CORS preflight responses. */
  private static final long CORS_MAX_AGE_SECONDS = 3600L;

  /** Client ID interceptor for validating client tokens. */
  private final ClientIdInterceptor clientIdInterceptor;

  /** API logging interceptor. */
  private final ApiLoggingInterceptor apiLoggingInterceptor;

  /**
   * Creates the MVC configuration with required interceptors.
   *
   * @param clientIdInterceptor validates the X-Client-ID header
   * @param apiLoggingInterceptor logs API invocations for auditing
   */
  @Autowired
  public WebMvcConfig(
      ClientIdInterceptor clientIdInterceptor,
      ApiLoggingInterceptor apiLoggingInterceptor) {
    this.clientIdInterceptor = clientIdInterceptor;
    this.apiLoggingInterceptor = apiLoggingInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(clientIdInterceptor).addPathPatterns("/api/**");
    registry.addInterceptor(apiLoggingInterceptor).addPathPatterns("/api/**");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins("*")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .exposedHeaders("X-Client-ID")
        .maxAge(CORS_MAX_AGE_SECONDS);
  }
}
