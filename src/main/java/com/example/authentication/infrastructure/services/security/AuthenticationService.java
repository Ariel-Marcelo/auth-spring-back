package com.example.authentication.infrastructure.services.security;

import com.example.authentication.infrastructure.api.dto.AuthResponse;
import com.example.authentication.infrastructure.api.dto.LoginRequest;
import com.example.authentication.infrastructure.persistence.entities.RefreshToken;
import com.example.authentication.infrastructure.persistence.entities.User;
import com.example.authentication.infrastructure.repositories.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        Set<String> permissions = extractPermissions(user);
        String accessToken = jwtService.generateToken(user.getUsername(), permissions);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        addRefreshTokenCookie(response, refreshToken.getToken(), Duration.ofDays(7).getSeconds());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    public AuthResponse refreshToken(String refreshTokenValue, HttpServletResponse response) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new RuntimeException("Refresh token cookie is missing");
        }

        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token not found in database"));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        Set<String> permissions = extractPermissions(user);
        String newAccessToken = jwtService.generateToken(user.getUsername(), permissions);

        addRefreshTokenCookie(response, newRefreshToken.getToken(), Duration.ofDays(7).getSeconds());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    public void logout(String refreshTokenValue, HttpServletResponse response) {
        if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
            refreshTokenService.findByToken(refreshTokenValue)
                    .ifPresent(refreshTokenService::revokeToken);
        }

        // Clear the HttpOnly cookie by setting maxAge = 0
        addRefreshTokenCookie(response, "", 0);
    }

    private Set<String> extractPermissions(User user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false) // Set to true when running HTTPS in production
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(maxAgeSeconds)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
