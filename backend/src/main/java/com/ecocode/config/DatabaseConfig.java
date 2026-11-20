package com.ecocode.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties() {
            @Override
            public String determineUrl() {
                String url = super.determineUrl();
                if (url != null) {
                    if (url.startsWith("postgres://")) {
                        url = "jdbc:postgresql://" + url.substring("postgres://".length());
                    } else if (url.startsWith("postgresql://")) {
                        url = "jdbc:" + url;
                    }
                }
                return url;
            }
        };
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}
