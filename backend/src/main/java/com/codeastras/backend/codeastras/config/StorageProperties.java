package com.codeastras.backend.codeastras.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "codeastras.storage")
@Getter
@Setter
public class StorageProperties {
    private String root;       // e.g. C:/Users/Animesh/codeastras
    private String projects;   // e.g. C:/Users/Animesh/codeastras/projects
    private String sessions;   // e.g. C:/Users/Animesh/codeastras/sessions
}
