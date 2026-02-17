package com.example.RecordStore.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.RecordStore.repository")
@EntityScan("com.example.RecordStore.model")
public class JpaConfig {
}
