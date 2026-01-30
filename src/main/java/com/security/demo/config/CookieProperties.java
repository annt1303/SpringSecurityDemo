package com.security.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.cookie")
@Getter
@Setter
public class CookieProperties {

    private boolean secure;
    private String sameSite;

    private Cookie refresh;
    private Cookie device;

    @Getter @Setter
    public static class Cookie {
        private String name;
        private int maxAgeDays;
    }
}

