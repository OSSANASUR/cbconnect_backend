package com.ossanasur.cbconnect.security.dto.response;
import java.util.Map;
public record LoginResponse(Map<String, Object> tokens, UserInfoResponse user) {}
