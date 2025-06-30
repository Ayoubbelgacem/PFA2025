package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.model.*;
import com.example.backend.repository.UtilisateurRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UtilisateurRepository utilisateurRepository;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserDetailsService userDetailsService,
                       PasswordEncoder passwordEncoder,
                       UtilisateurRepository utilisateurRepository,
                       RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.utilisateurRepository = utilisateurRepository;
        this.refreshTokenService = refreshTokenService;
    }

    public ResponseEntity<?> loginService(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail()).orElseThrow();
            String jwt = jwtService.generateToken(utilisateur.getEmail());
            String refreshToken = refreshTokenService.createRefreshToken(utilisateur.getId()).getToken();

            return ResponseEntity.ok(new AuthResponse(jwt, refreshToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null));
        }
    }

    public ResponseEntity<?> registerService(RegisterRequest request) {
        try {
            if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Erreur: l'email est déjà utilisé !"));
            }

            RoleUtilisateur role;
            try {
                role = request.getRole() == null
                        ? RoleUtilisateur.COLLABORATEUR
                        : RoleUtilisateur.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(new MessageResponse("Erreur: rôle invalide"));
            }

            Utilisateur user = new Utilisateur();
            user.setNom(request.getNom());
            user.setPrenom(request.getPrenom());
            user.setEmail(request.getEmail());
            user.setMotDePasse(passwordEncoder.encode(request.getPassword()));
            user.setRole(role);

            utilisateurRepository.save(user);
            String jwt = jwtService.generateToken(user.getEmail());
            String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();

            return ResponseEntity.ok(new AuthResponse(jwt, refreshToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erreur lors de l'enregistrement: " + e.getMessage()));
        }
    }

    public ResponseEntity<?> forgotPasswordService(String email) {
        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Aucun utilisateur trouvé avec cet email."));
        }

        Utilisateur user = userOpt.get();
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        utilisateurRepository.save(user);

        String resetLink = "http://localhost:4200/reset-password?token=" + resetToken;
        emailService.sendResetPasswordEmail(email, resetLink);

        return ResponseEntity.ok(new MessageResponse("Lien de réinitialisation envoyé à votre email."));
    }

    public ResponseEntity<?> resetPasswordService(String token, String newPassword) {
        Optional<Utilisateur> userOpt = utilisateurRepository.findByResetToken(token);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lien invalide ou expiré."));
        }

        Utilisateur user = userOpt.get();
        user.setMotDePasse(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        utilisateurRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Mot de passe réinitialisé avec succès."));
    }

    public ResponseEntity<?> refreshTokenService(TokenRefreshRequest request) {
        String requestToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUtilisateur)
                .map(user -> {
                    String newAccessToken = jwtService.generateToken(user.getEmail());
                    return ResponseEntity.ok(new AuthResponse(newAccessToken, requestToken));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(null, null)));
    }

    // Nouvelle méthode pour récupérer l'utilisateur connecté directement depuis le SecurityContextHolder
    private Utilisateur getCurrentUserFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Utilisateur utilisateur) {
            return utilisateur;
        }
        throw new RuntimeException("Utilisateur non connecté ou invalide");
    }

    public ResponseEntity<?> getCurrentUserService() {
        try {
            Utilisateur user = getCurrentUserFromContext();
            UtilisateurDTO dto = mapToDto(user);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Utilisateur non connecté"));
        }
    }

    @Transactional
    public ResponseEntity<?> updateProfileService(UpdateUserRequest updatedUserRequest) {
        try {
            Utilisateur user = getCurrentUserFromContext();

            user.setNom(updatedUserRequest.getNom());
            user.setPrenom(updatedUserRequest.getPrenom());
            user.setEmail(updatedUserRequest.getEmail());

            if (updatedUserRequest.getPhotoProfil() != null &&
                    updatedUserRequest.getPhotoProfil().startsWith("data:image")) {
                user.setPhotoProfil(updatedUserRequest.getPhotoProfil());
            }

            utilisateurRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("Profil mis à jour avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> deleteAccountService() {
        try {
            Utilisateur user = getCurrentUserFromContext();

            refreshTokenService.deleteByUtilisateur(user);
            utilisateurRepository.delete(user);

            SecurityContextHolder.clearContext(); // Nettoie le contexte après suppression

            return ResponseEntity.ok(new MessageResponse("Compte supprimé avec succès."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    public UtilisateurDTO mapToDto(Utilisateur user) {
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setPhotoProfil(user.getPhotoProfil());
        if (user.getDateInscription() != null)
            dto.setDateInscription(user.getDateInscription().toString());
        return dto;
    }
}
