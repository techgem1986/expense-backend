package com.expenseapp.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
public class OpenApiConfiguration {

    @Value("${app.jwt.secret:dev}")
    private String jwtSecret;

    @Bean
    public OpenAPI expenseAppOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Expense Management Service API")
                        .version("1.0.0")
                        .description("RESTful API for managing personal expenses, budgets, recurring transactions, and financial analytics. " +
                                "This API provides endpoints for user authentication, transaction management, budget tracking, " +
                                "category management, alerts, and spending analytics.")
                        .contact(new Contact()
                                .name("Expense App Support")
                                .email("support@expenseapp.com")
                                .url("https://github.com/expenseapp"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server"),
                        new Server()
                                .url("https://api.expenseapp.com")
                                .description("Production server")
                ))
                .schemaRequirement("Bearer Authentication", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token obtained from /api/auth/login endpoint"));
    }
}