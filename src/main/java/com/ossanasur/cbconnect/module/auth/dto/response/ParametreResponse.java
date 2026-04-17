package com.ossanasur.cbconnect.module.auth.dto.response;
import com.ossanasur.cbconnect.common.enums.TypeParametre;
import java.util.UUID;
public record ParametreResponse(UUID parametreTrackingId, TypeParametre typeParametre, String cle, String valeur, String description) {}
