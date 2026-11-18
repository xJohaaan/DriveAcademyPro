package com.driveacademy.pro.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Redirige a cada usuario a su dashboard según el rol con el que inició sesión.
 */
@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private static final Map<String, String> DESTINOS = Map.of(
            "ROLE_ADMIN", "/admin/dashboard",
            "ROLE_INSTRUCTOR", "/instructor/dashboard",
            "ROLE_RECEPCIONISTA", "/recepcion/dashboard",
            "ROLE_ESTUDIANTE", "/estudiante/dashboard"
    );

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String destino = obtenerDestino(authentication);
        response.sendRedirect(request.getContextPath() + destino);
    }

    public static String obtenerDestino(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String destino = DESTINOS.get(authority.getAuthority());
            if (destino != null) {
                return destino;
            }
        }
        return "/acceso-denegado";
    }
}
