package tn.esprit.autoloc.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipementRequestDTO(
        @NotBlank @Size(max = 100) String libelle
) {
}
