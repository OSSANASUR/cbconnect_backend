package com.ossanasur.cbconnect.config;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;
@Configuration
public class PaperlessConfig {
    @Value("${paperless.base-url:http://localhost:8000}") private String baseUrl;
    @Value("${paperless.username:admin}") private String username;
    @Value("${paperless.password:admin}") private String password;
    @Bean public OkHttpClient paperlessHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
    }
    public String getBaseUrl() { return baseUrl; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
