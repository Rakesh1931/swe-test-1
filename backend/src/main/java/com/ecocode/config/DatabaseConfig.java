package com.ecocode.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties() {
            @Override
            public String determineUrl() {
                String rawUrl = super.determineUrl();
                return sanitizeUrl(rawUrl, this);
            }
        };
    }

    private String sanitizeUrl(String rawUrl, DataSourceProperties props) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return rawUrl;
        }

        // Standard JDBC format is already clean: jdbc:postgresql://host:port/database
        if (rawUrl.startsWith("jdbc:postgresql://") && !rawUrl.contains("@")) {
            return rawUrl;
        }

        try {
            // Strip any leading jdbc: prefix to parse as URI
            String uriString = rawUrl;
            if (uriString.startsWith("jdbc:")) {
                uriString = uriString.substring(5);
            }

            URI uri = new URI(uriString);
            String host = uri.getHost();
            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            String path = uri.getPath(); // e.g. /ecocode_51au
            String database = (path != null && path.length() > 1) ? path.substring(1) : "ecocode";

            // Extract user & password from userInfo if present (e.g. user:password@host)
            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isBlank()) {
                String[] parts = userInfo.split(":", 2);
                if (parts.length > 0 && (props.getUsername() == null || props.getUsername().isBlank())) {
                    props.setUsername(parts[0]);
                }
                if (parts.length > 1 && (props.getPassword() == null || props.getPassword().isBlank())) {
                    props.setPassword(parts[1]);
                }
            }

            String cleanJdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
            log.info("Sanitized database URL to standard JDBC: {}", cleanJdbcUrl);
            return cleanJdbcUrl;
        } catch (Exception e) {
            log.warn("Failed to parse URI from '{}', falling back to raw URL: {}", rawUrl, e.getMessage());
            return rawUrl;
        }
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}
