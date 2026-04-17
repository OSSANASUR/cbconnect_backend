package com.ossanasur.cbconnect.security.scheduler;
import com.ossanasur.cbconnect.security.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
@Slf4j @Component @RequiredArgsConstructor
public class TokenCronJob {
    private final TokenRepository tokenRepository;
    @Scheduled(cron = "0 0 2 * * *") // Chaque nuit a 2h
    public void purgerTokensExpires() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Tokens expires purges");
    }
}
