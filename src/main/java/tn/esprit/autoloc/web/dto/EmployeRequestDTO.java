package tn.esprit.autoloc.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tn.esprit.autoloc.domain.RoleEmploye;

public record EmployeRequestDTO(
        @NotBlank @Size(max = 50) String nom,
        @NotBlank @Size(max = 50) String prenom,
        @NotNull RoleEmploye role,
        @NotNull Long agenceId
) {
}
