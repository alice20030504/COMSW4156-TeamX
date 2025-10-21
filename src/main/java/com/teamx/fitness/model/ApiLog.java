/**
 * <p>Part of the Fitness Management Service.</p>
 *
 * <p>Entity class for logging API requests including method, endpoint,
 * timestamp, and response status for auditing and debugging purposes.</p>
 *
 * @checkstyle 2025-10-21 by alice
 * @version 1.0
 */

package com.teamx.fitness.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_logs")
public class ApiLog {

  /** Unique identifier for the API log entry. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Client ID making the API request. */
  @Column(name = "client_id", nullable = false)
  private String clientId;

  /** API endpoint that was called. */
  @Column(nullable = false)
  private String endpoint;

  /** HTTP method used for the API request (GET, POST, etc.). */
  @Column(name = "http_method", nullable = false)
  private String httpMethod;

  /** Timestamp when the API request was made. */
  @Column(nullable = false)
  private LocalDateTime timestamp;

  /** Request payload sent by the client, stored as text. */
  @Column(name = "request_payload", columnDefinition = "TEXT")
  private String requestPayload;

  /** HTTP response status code returned by the server. */
  @Column(name = "response_status")
  private Integer responseStatus;

  /** Time taken to process the request, in milliseconds. */
  @Column(name = "response_time")
  private Long responseTime;

  /** IP address of the client making the request. */
  @Column(name = "ip_address")
  private String ipAddress;

  /** User-Agent header from the client. */
  @Column(name = "user_agent")
  private String userAgent;

  /** Error message if the API request resulted in an error. */
  @Column(name = "error_message")
  private String errorMessage;

  /**
   * Automatically sets the timestamp to the current time before persisting if not already set.
   */
  @PrePersist
  protected void onCreate() {
    if (timestamp == null) {
      timestamp = LocalDateTime.now();
    }
  }

  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }

  public String getClientId() {
    return clientId;
  }
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getEndpoint() {
    return endpoint;
  }
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getHttpMethod() {
    return httpMethod;
  }
  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getRequestPayload() {
    return requestPayload;
  }
  public void setRequestPayload(String requestPayload) {
    this.requestPayload = requestPayload;
  }

  public Integer getResponseStatus() {
    return responseStatus;
  }
  public void setResponseStatus(Integer responseStatus) {
    this.responseStatus = responseStatus;
  }

  public Long getResponseTime() {
    return responseTime;
  }
  public void setResponseTime(Long responseTime) {
    this.responseTime = responseTime;
  }

  public String getIpAddress() {
    return ipAddress;
  }
  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }
  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}