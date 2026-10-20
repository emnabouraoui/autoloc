package tn.esprit.autoloc.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgenceRequestDTO(
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 100) String ville,
        @NotBlank @Size(max = 255) String adresse,
        @Size(max = 20) String telephone
) {
}
