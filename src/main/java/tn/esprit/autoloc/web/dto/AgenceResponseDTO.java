package tn.esprit.autoloc.web.dto;

import tn.esprit.autoloc.domain.Agence;

public record AgenceResponseDTO(
        Long idAgence,
        String nom,
        String ville,
        String adresse,
        String telephone
) {
    public static AgenceResponseDTO from(Agence agence) {
        return new AgenceResponseDTO(
                agence.getIdAgence(),
                agence.getNom(),
                agence.getVille(),
                agence.getAdresse(),
                agence.getTelephone()
        );
    }
}
