package com.expenseapp.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfiguration {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

        // Configure scheduler properties
        schedulerFactoryBean.setSchedulerName("ExpenseAppScheduler");
        schedulerFactoryBean.setApplicationContextSchedulerContextKey("applicationContext");

        // Auto-startup the scheduler
        schedulerFactoryBean.setAutoStartup(true);

        return schedulerFactoryBean;
    }
}