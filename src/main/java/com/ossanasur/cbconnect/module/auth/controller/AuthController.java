package com.ossanasur.cbconnect.module.auth.controller;

import com.ossanasur.cbconnect.module.auth.service.UtilisateurService;
import com.ossanasur.cbconnect.security.dto.request.ActivateAccountRequest;
import com.ossanasur.cbconnect.security.dto.request.LoginRequest;
import com.ossanasur.cbconnect.security.dto.request.OtpResendRequest;
import com.ossanasur.cbconnect.security.dto.request.OtpVerifyRequest;
import com.ossanasur.cbconnect.security.dto.response.ActivationInfoResponse;
import com.ossanasur.cbconnect.security.dto.response.LoginResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Login, logout, refresh token")
public class AuthController {

    private final UtilisateurService utilisateurService;

    @PostMapping("/login")
    @Operation(summary = "Connexion a CBConnect")
    public ResponseEntity<DataResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(utilisateurService.login(request));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verifier le code OTP de connexion (2FA)")
    public ResponseEntity<DataResponse<LoginResponse>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(utilisateurService.verifyOtpAndIssueTokens(
                request.otpTrackingId(), request.code(), false));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Renvoyer un nouveau code OTP (cooldown 60s)")
    public ResponseEntity<DataResponse<LoginResponse>> resendOtp(@Valid @RequestBody OtpResendRequest request) {
        return ResponseEntity.ok(utilisateurService.resendLoginOtp(request.otpTrackingId()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Deconnexion")
    public ResponseEntity<DataResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return ResponseEntity.ok(utilisateurService.logout(token));
    }

    @GetMapping("/activation/{token}")
    @Operation(summary = "Valider un lien d'activation et obtenir les infos pre-remplies")
    public ResponseEntity<DataResponse<ActivationInfoResponse>> getActivationInfo(@PathVariable String token) {
        return ResponseEntity.ok(DataResponse.success(utilisateurService.validateActivationToken(token)));
    }

    @PostMapping("/activation")
    @Operation(summary = "Definir le mot de passe et activer le compte")
    public ResponseEntity<DataResponse<Void>> activate(
            @Valid @RequestBody ActivateAccountRequest request) {
        return ResponseEntity.ok(utilisateurService.activerCompte(request));
    }
}
