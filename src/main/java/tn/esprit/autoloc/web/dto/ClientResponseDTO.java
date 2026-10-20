package tn.esprit.autoloc.web.dto;

import tn.esprit.autoloc.domain.Client;

import java.time.LocalDate;

public record ClientResponseDTO(
        Long idClient,
        String nom,
        String prenom,
        String email,
        String telephone,
        String numPermis,
        LocalDate dateInscription
) {
    public static ClientResponseDTO from(Client client) {
        return new ClientResponseDTO(
                client.getIdClient(),
                client.getNom(),
                client.getPrenom(),
                client.getEmail(),
                client.getTelephone(),
                client.getNumPermis(),
                client.getDateInscription()
        );
    }
}
