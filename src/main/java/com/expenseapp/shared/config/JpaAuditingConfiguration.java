package com.expenseapp.shared.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@ConditionalOnProperty(prefix = "app.jpa", name = "auditing", havingValue = "true", matchIfMissing = true)
@EnableJpaAuditing
public class JpaAuditingConfiguration {
}
