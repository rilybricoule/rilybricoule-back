package com.sbsolutions.rilybricoule.security.domain.port.out;

public interface AuditLogPort {

    void logLoginSuccess(String email, String ipAddress, String userAgent);

    void logLoginFailure(String email, String ipAddress, String userAgent, String reason);

    void logRegister(String email, String ipAddress, String userAgent);

    void logTokenRefresh(String email, String ipAddress, String userAgent);

    void logTokenRefreshFailure(String ipAddress, String userAgent, String reason);

    void logLogout(String email, String ipAddress, String userAgent);

    void logInvalidTokenAttempt(String ipAddress, String userAgent, String reason);
}
