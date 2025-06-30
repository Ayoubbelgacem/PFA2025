package com.example.backend.service;

import com.example.backend.model.RefreshToken;
import com.example.backend.model.Utilisateur;
import com.example.backend.repository.RefreshTokenRepository;
import com.example.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.jwt.refreshExpirationMs:604800000}") // 7 jours
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UtilisateurRepository utilisateurRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UtilisateurRepository utilisateurRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Crée ou renouvelle un refresh token pour un utilisateur donné
     */
    public RefreshToken createRefreshToken(Long userId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec ID : " + userId));

        // Vérifie si un token existe déjà
        Optional<RefreshToken> existingTokenOpt = refreshTokenRepository
                .findAll()
                .stream()
                .filter(token -> token.getUtilisateur().getId().equals(userId))
                .findFirst();

        RefreshToken token = existingTokenOpt.orElse(new RefreshToken());
        token.setUtilisateur(utilisateur);
        token.setToken(UUID.randomUUID().toString());
        token.setExpirationDate(Instant.now().plusMillis(refreshTokenDurationMs));

        return refreshTokenRepository.save(token);
    }

    /**
     * Recherche un token dans la base de données
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Vérifie que le token n'est pas expiré
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpirationDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expiré. Veuillez vous reconnecter.");
        }
        return token;
    }

    /**
     * Supprime tous les refresh tokens liés à un utilisateur
     */
    public void deleteByUtilisateur(Utilisateur utilisateur) {
        refreshTokenRepository.deleteByUtilisateur(utilisateur);
    }

    /**
     * Supprime les tokens par ID utilisateur
     */
    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUtilisateurId(userId);
    }
}
