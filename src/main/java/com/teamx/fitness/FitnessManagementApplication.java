package com.teamx.fitness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Personal Fitness Management Service.
 * This service provides comprehensive fitness tracking and management capabilities
 * for individual users and research analysts.
 */
@SuppressWarnings({"checkstyle:HideUtilityClassConstructor", "PMD.UseUtilityClass"})
@SpringBootApplication
public class FitnessManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitnessManagementApplication.class, args);
    }
}
