package tn.esprit.autoloc.web.dto;

import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.domain.RoleEmploye;

public record EmployeResponseDTO(
        Long idEmploye,
        String nom,
        String prenom,
        RoleEmploye role,
        Long agenceId,
        String agenceNom
) {
    public static EmployeResponseDTO from(Employe employe) {
        Long agenceId = employe.getAgence() == null ? null : employe.getAgence().getIdAgence();
        String agenceNom = employe.getAgence() == null ? null : employe.getAgence().getNom();

        return new EmployeResponseDTO(
                employe.getIdEmploye(),
                employe.getNom(),
                employe.getPrenom(),
                employe.getRole(),
                agenceId,
                agenceNom
        );
    }
}
