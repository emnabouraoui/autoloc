package tn.esprit.autoloc.web.dto;

import java.math.BigDecimal;

public record TarificationResponseDTO(
        String type,
        BigDecimal tarifJournalier,
        int jours,
        double montant
) {
}
