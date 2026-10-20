package tn.esprit.autoloc.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tn.esprit.autoloc.domain.ModePaiement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaiementRequestDTO(
        @NotNull @Positive BigDecimal montant,
        @NotNull LocalDateTime datePaiement,
        @NotNull ModePaiement modePaiement,
        @NotNull Long contratId
) {
}
