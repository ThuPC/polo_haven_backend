package com.datn.backend.configuration;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class SecurityUtil {

    public static String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("🔍 Debug: authentication = " + authentication);

            if (authentication instanceof JwtAuthenticationToken) {
                Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
                String userId = jwt.getClaimAsString("userId");
                System.out.println("🔍 Debug: userId = " + userId);
                return userId;
            }

            System.err.println("❌ Authentication không phải JwtAuthenticationToken");
            return null;
        } catch (Exception e) {
            System.err.println("❌ Error in SecurityUtil: " + e.getMessage());
            return null;
        }
    }
}