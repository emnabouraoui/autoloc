package tn.esprit.autoloc.web.dto;

import tn.esprit.autoloc.domain.ModePaiement;
import tn.esprit.autoloc.domain.Paiement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaiementResponseDTO(
        Long idPaiement,
        BigDecimal montant,
        LocalDateTime datePaiement,
        ModePaiement modePaiement,
        Long contratId
) {
    public static PaiementResponseDTO from(Paiement paiement) {
        return new PaiementResponseDTO(
                paiement.getIdPaiement(),
                paiement.getMontant(),
                paiement.getDatePaiement(),
                paiement.getModePaiement(),
                paiement.getContrat() == null ? null : paiement.getContrat().getIdContrat()
        );
    }
}
