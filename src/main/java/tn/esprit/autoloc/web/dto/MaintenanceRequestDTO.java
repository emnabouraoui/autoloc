package tn.esprit.autoloc.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MaintenanceRequestDTO(
        @NotNull LocalDate dateDebut,
        LocalDate dateFin,
        @Size(max = 500) String description,
        @NotNull Long vehiculeId
) {
}
