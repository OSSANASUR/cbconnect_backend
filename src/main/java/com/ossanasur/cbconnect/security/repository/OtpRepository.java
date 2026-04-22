package com.ossanasur.cbconnect.security.repository;

import com.ossanasur.cbconnect.security.entity.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<OTP, Integer> {

    @Query("""
        SELECT o FROM OTP o
        WHERE o.otpTrackingId = :trackingId
          AND o.used = false
          AND o.activeData = true
          AND o.deletedData = false
        """)
    Optional<OTP> findActiveByTrackingId(@Param("trackingId") UUID trackingId);

    @Modifying
    @Query("""
        UPDATE OTP o SET o.used = true, o.updatedAt = :now
        WHERE o.utilisateur.utilisateurTrackingId = :userTrackingId
          AND o.purpose = :purpose
          AND o.used = false
        """)
    int invalidateActiveOtpsForUser(
        @Param("userTrackingId") UUID userTrackingId,
        @Param("purpose") String purpose,
        @Param("now") LocalDateTime now);
}
