package com.ossanasur.cbconnect.security.repository;
import com.ossanasur.cbconnect.security.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {
    Optional<Token> findByAccessTokenOrRefreshToken(String accessToken, String refreshToken);
    boolean existsTokenByAccessTokenOrRefreshToken(String at, String rt);
    @Query("SELECT COUNT(t) > 0 FROM Token t WHERE (t.accessToken = :token OR t.refreshToken = :token) AND t.isValid = true")
    boolean existsActiveToken(@Param("token") String token);
    @Modifying @Transactional
    @Query("UPDATE Token t SET t.isValid = false WHERE t.user.utilisateurTrackingId = :uid AND t.historiqueId <> :currentId")
    void invalidateOtherTokens(@Param("uid") UUID userId, @Param("currentId") Integer currentId);
    @Modifying @Transactional
    @Query("UPDATE Token t SET t.accessToken = :newAt WHERE t.historiqueId = :id")
    void updateAccessToken(@Param("id") Integer id, @Param("newAt") String newAccessToken);
    @Modifying @Transactional
    @Query("DELETE FROM Token t WHERE t.expireAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
