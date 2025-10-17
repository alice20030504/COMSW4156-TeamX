package com.teamx.fitness.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_logs")
public class ApiLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "client_id", nullable = false)
  private String clientId;

  @Column(nullable = false)
  private String endpoint;

  @Column(name = "http_method", nullable = false)
  private String httpMethod;

  @Column(nullable = false)
  private LocalDateTime timestamp;

  @PrePersist
  protected void onCreate() {
    if (timestamp == null) {
      timestamp = LocalDateTime.now();
    }
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getClientId() { return clientId; }
  public void setClientId(String clientId) { this.clientId = clientId; }

  public String getEndpoint() { return endpoint; }
  public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

  public String getHttpMethod() { return httpMethod; }
  public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

  public LocalDateTime getTimestamp() { return timestamp; }
  public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}