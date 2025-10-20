package com.teamx.fitness.exception;

public class ClientUnauthorizedException extends RuntimeException {

  public ClientUnauthorizedException(String message) {
    super(message);
  }

  public ClientUnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}
