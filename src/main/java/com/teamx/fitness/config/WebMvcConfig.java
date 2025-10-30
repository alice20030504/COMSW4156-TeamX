package com.teamx.fitness.config;

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

  /**
   * Client ID interceptor for validating client tokens.
   */
  @Autowired
  private ClientIdInterceptor clientIdInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(clientIdInterceptor)
        .addPathPatterns("/api/**")
        .excludePathPatterns("/api/register", "/api/login");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOrigins("http://localhost:3000")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
  }
}
