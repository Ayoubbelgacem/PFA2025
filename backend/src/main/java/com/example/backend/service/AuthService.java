package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.model.*;
import com.example.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
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

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail()).orElseThrow();
        String jwt = jwtService.generateToken(utilisateur.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(utilisateur.getId()).getToken();

        return new AuthResponse(jwt, refreshToken);
    }

    public Object register(RegisterRequest request) {
        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            return new MessageResponse("Erreur: l'email est déjà utilisé !");
        }

        RoleUtilisateur role;
        try {
            role = RoleUtilisateur.valueOf(
                    request.getRole() != null ? request.getRole().toLowerCase() : "collaborateur"
            );
        } catch (IllegalArgumentException e) {
            return new MessageResponse("Erreur: rôle invalide (utilisez admin ou collaborateur)");
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

        return new AuthResponse(jwt, refreshToken);
    }

    public Object processForgotPassword(String email) {
        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return new MessageResponse("Aucun utilisateur trouvé avec cet email.");
        }

        Utilisateur user = userOpt.get();
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        utilisateurRepository.save(user);

        String resetLink = "http://localhost:4200/reset-password?token=" + resetToken;
        emailService.sendResetPasswordEmail(email, resetLink);

        return new MessageResponse("Un lien de réinitialisation a été envoyé à votre adresse email.");
    }

    public Object resetPassword(String token, String newPassword) {
        Optional<Utilisateur> userOpt = utilisateurRepository.findByResetToken(token);
        if (userOpt.isEmpty()) {
            return new MessageResponse("Lien de réinitialisation invalide ou expiré.");
        }

        Utilisateur user = userOpt.get();
        user.setMotDePasse(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        utilisateurRepository.save(user);

        return new MessageResponse("Mot de passe réinitialisé avec succès.");
    }

    public String generateAccessToken(String email) {
        return jwtService.generateToken(email);
    }
}
