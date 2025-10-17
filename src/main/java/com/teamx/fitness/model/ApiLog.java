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

  @Column(name = "request_payload", columnDefinition = "TEXT")
  private String requestPayload;

  @Column(name = "response_status")
  private Integer responseStatus;

  @Column(name = "response_time")
  private Long responseTime;

  @Column(name = "ip_address")
  private String ipAddress;

  @Column(name = "user_agent")
  private String userAgent;

  @Column(name = "error_message")
  private String errorMessage;

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

  public String getRequestPayload() { return requestPayload; }
  public void setRequestPayload(String requestPayload) { this.requestPayload = requestPayload; }

  public Integer getResponseStatus() { return responseStatus; }
  public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }

  public Long getResponseTime() { return responseTime; }
  public void setResponseTime(Long responseTime) { this.responseTime = responseTime; }

  public String getIpAddress() { return ipAddress; }
  public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

  public String getUserAgent() { return userAgent; }
  public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

  public String getErrorMessage() { return errorMessage; }
  public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}