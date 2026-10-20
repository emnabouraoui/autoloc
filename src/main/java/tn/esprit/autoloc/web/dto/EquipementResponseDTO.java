package tn.esprit.autoloc.web.dto;

import tn.esprit.autoloc.domain.Equipement;

public record EquipementResponseDTO(
        Long idEquipement,
        String libelle
) {
    public static EquipementResponseDTO from(Equipement equipement) {
        return new EquipementResponseDTO(equipement.getIdEquipement(), equipement.getLibelle());
    }
}
