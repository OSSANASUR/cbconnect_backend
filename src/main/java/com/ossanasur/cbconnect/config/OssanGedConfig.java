package com.ossanasur.cbconnect.config;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration OssanGED by OSSANASUR — proxy vers le moteur Paperless-ngx reconfiguré.
 */
@Configuration
public class OssanGedConfig {
    @Value("${ossanged.base-url:http://localhost}") private String baseUrl;
    @Value("${ossanged.username:admin}") private String username;
    @Value("${ossanged.password:admin}") private String password;
    @Value("${ossanged.api-token:}")    private String apiToken;

    @Bean public OkHttpClient ossanGedHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
    }

    public String getBaseUrl()  { return baseUrl; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getApiToken() { return apiToken; }
}
