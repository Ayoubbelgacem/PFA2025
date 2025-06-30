package com.example.backend.dto;

import com.example.backend.model.RoleUtilisateur;
import lombok.Data;

@Data
public class UtilisateurDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private RoleUtilisateur role;
    private String photoProfil;
    private String dateInscription; // ou Timestamp si tu préfères
}
