package tn.esprit.autoloc.web.dto;

import jakarta.validation.constraints.NotNull;
import tn.esprit.autoloc.domain.StatutReservation;

import java.time.LocalDate;

public record ReservationRequestDTO(
        @NotNull LocalDate dateDebut,
        @NotNull LocalDate dateFin,
        @NotNull StatutReservation statut,
        @NotNull Long clientId,
        @NotNull Long vehiculeId
) {
}
