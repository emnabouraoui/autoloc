package tn.esprit.autoloc.web.dto;

import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public record VehiculeResponseDTO(
        Long idVehicule,
        String immatriculation,
        String marque,
        String modele,
        CategorieVehicule categorie,
        BigDecimal tarifJournalier,
        StatutVehicule statut,
        Long agenceId,
        String agenceNom,
        List<EquipementResponseDTO> equipements
) {
    public static VehiculeResponseDTO from(Vehicule vehicule) {
        Long agenceId = vehicule.getAgence() == null ? null : vehicule.getAgence().getIdAgence();
        String agenceNom = vehicule.getAgence() == null ? null : vehicule.getAgence().getNom();
        List<EquipementResponseDTO> equipements = vehicule.getEquipements().stream()
                .sorted(Comparator.comparing(Equipement::getIdEquipement))
                .map(EquipementResponseDTO::from)
                .toList();

        return new VehiculeResponseDTO(
                vehicule.getIdVehicule(),
                vehicule.getImmatriculation(),
                vehicule.getMarque(),
                vehicule.getModele(),
                vehicule.getCategorie(),
                vehicule.getTarifJournalier(),
                vehicule.getStatut(),
                agenceId,
                agenceNom,
                equipements
        );
    }
}
