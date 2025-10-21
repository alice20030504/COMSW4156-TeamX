package com.teamx.fitness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Personal Fitness Management Service.
 * This service provides comprehensive fitness tracking and management capabilities
 * for individual users and research analysts.
 */
@SpringBootApplication
@SuppressWarnings({"checkstyle:FinalClass", "checkstyle:HideUtilityClassConstructor"})
public class FitnessManagementApplication {

    public FitnessManagementApplication() {
        // no-op constructor required for Spring proxying
    }

    public static void main(String[] args) {
        SpringApplication.run(FitnessManagementApplication.class, args);
    }
}
