
package com.smartlms.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        String redirectUrl;

        switch (role) {
            case "ROLE_ADMIN":
                redirectUrl = "/admin/dashboard";
                break;

            case "ROLE_TEACHER":
                redirectUrl = "/teacher/dashboard";
                break;

            case "ROLE_STUDENT":
                redirectUrl = "/student/dashboard";
                break;

            default:
                redirectUrl = "/login?error";
        }

        response.sendRedirect(redirectUrl);
    }
}
