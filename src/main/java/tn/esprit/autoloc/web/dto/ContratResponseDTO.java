package tn.esprit.autoloc.web.dto;

import tn.esprit.autoloc.domain.Contrat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratResponseDTO(
        Long idContrat,
        LocalDate dateSignature,
        BigDecimal montantTotal,
        boolean valide,
        Long reservationId
) {
    public static ContratResponseDTO from(Contrat contrat) {
        return new ContratResponseDTO(
                contrat.getIdContrat(),
                contrat.getDateSignature(),
                contrat.getMontantTotal(),
                contrat.isValide(),
                contrat.getReservation() == null ? null : contrat.getReservation().getIdReservation()
        );
    }
}
