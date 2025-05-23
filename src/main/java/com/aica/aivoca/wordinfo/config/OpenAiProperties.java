package com.aica.aivoca.wordinfo.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "openai.api")
public class OpenAiProperties {
    private String key;
    private String url;

    @PostConstruct
    public void debug() {
        System.out.println("🔍 KEY: " + key);
        System.out.println("🔍 URL: " + url);
    }
}
