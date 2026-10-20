package tn.esprit.autoloc.web.dto;

import tn.esprit.autoloc.domain.Maintenance;

import java.time.LocalDate;

public record MaintenanceResponseDTO(
        Long idMaintenance,
        LocalDate dateDebut,
        LocalDate dateFin,
        String description,
        Long vehiculeId,
        String immatriculation
) {
    public static MaintenanceResponseDTO from(Maintenance maintenance) {
        Long vehiculeId = maintenance.getVehicule() == null ? null : maintenance.getVehicule().getIdVehicule();
        String immatriculation = maintenance.getVehicule() == null ? null : maintenance.getVehicule().getImmatriculation();

        return new MaintenanceResponseDTO(
                maintenance.getIdMaintenance(),
                maintenance.getDateDebut(),
                maintenance.getDateFin(),
                maintenance.getDescription(),
                vehiculeId,
                immatriculation
        );
    }
}
