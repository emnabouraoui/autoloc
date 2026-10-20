package tn.esprit.autoloc.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratRequestDTO(
        @NotNull LocalDate dateSignature,
        @NotNull @Positive BigDecimal montantTotal,
        boolean valide,
        @NotNull Long reservationId
) {
}
