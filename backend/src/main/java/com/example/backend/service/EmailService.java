package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetPasswordEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Réinitialisation de votre mot de passe");
        message.setText("Bonjour,\n\nCliquez sur le lien suivant pour réinitialiser votre mot de passe :\n"
                + resetLink + "\n\nSi vous n'êtes pas à l'origine de cette demande, ignorez cet email.");

        mailSender.send(message);
    }
}
