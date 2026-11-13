package tn.esprit.autoloc.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TarificationRequestDTO(
        @NotBlank String type,
        @NotNull @Positive BigDecimal tarifJournalier,
        @NotNull @Positive Integer jours
) {
}
