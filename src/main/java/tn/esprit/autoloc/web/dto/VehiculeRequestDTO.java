package tn.esprit.autoloc.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.StatutVehicule;

import java.math.BigDecimal;
import java.util.Set;

public record VehiculeRequestDTO(
        @NotBlank @Size(max = 20) String immatriculation,
        @NotBlank @Size(max = 50) String marque,
        @NotBlank @Size(max = 50) String modele,
        @NotNull CategorieVehicule categorie,
        @NotNull @Positive BigDecimal tarifJournalier,
        @NotNull StatutVehicule statut,
        Long agenceId,
        Set<Long> equipementIds
) {
}
