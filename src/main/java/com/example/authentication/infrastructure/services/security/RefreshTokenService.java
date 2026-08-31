package com.example.authentication.infrastructure.services.security;

import com.example.authentication.infrastructure.persistence.entities.RefreshToken;
import com.example.authentication.infrastructure.persistence.entities.User;
import com.example.authentication.infrastructure.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Creates a new RefreshToken for a user, revoking any previously active tokens (Rotation).
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Rotation: Revoke any existing active refresh tokens for this user
        refreshTokenRepository.revokeAllByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Verifies that the RefreshToken is not expired or revoked.
     */
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked() || token.isExpired()) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new RuntimeException("Refresh token was expired or revoked. Please make a new login request.");
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }
}
