package com.ossanasur.cbconnect.module.auth.service;
import com.ossanasur.cbconnect.module.auth.dto.request.UtilisateurRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.UtilisateurResponse;
import com.ossanasur.cbconnect.security.dto.request.*;
import com.ossanasur.cbconnect.security.dto.response.LoginResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import java.util.List;
import java.util.UUID;
public interface UtilisateurService {
    DataResponse<UtilisateurResponse> creer(UtilisateurRequest request, String loginAuteur);
    DataResponse<UtilisateurResponse> modifier(UUID trackingId, UtilisateurRequest request, String loginAuteur);
    DataResponse<UtilisateurResponse> getByTrackingId(UUID trackingId);
    DataResponse<List<UtilisateurResponse>> getAll();
    DataResponse<Void> supprimer(UUID trackingId, String loginAuteur);
    DataResponse<Void> changerPassword(UUID trackingId, ChangePasswordRequest request, String loginAuteur);
    DataResponse<Void> activerCompte(String verificationCode);
    DataResponse<LoginResponse> login(LoginRequest request);
    DataResponse<LoginResponse> verifyOtpAndIssueTokens(UUID otpTrackingId, String code, boolean isMobile);
    DataResponse<LoginResponse> resendLoginOtp(UUID otpTrackingId);
    DataResponse<Void> logout(String token);
}
