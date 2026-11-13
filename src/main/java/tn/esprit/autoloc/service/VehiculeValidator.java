package tn.esprit.autoloc.service;

import org.springframework.stereotype.Component;
import tn.esprit.autoloc.web.dto.VehiculeRequestDTO;
import tn.esprit.autoloc.web.exception.BusinessRuleException;

import java.math.BigDecimal;

/**
 * Validation métier des véhicules.
 *
 * Cette classe ne persiste rien et ne journalise rien : elle a une seule raison
 * de changer, les règles de validation des véhicules.
 */
@Component
public class VehiculeValidator {

    public void valider(VehiculeRequestDTO dto) {
        if (dto == null) {
            throw new BusinessRuleException("Les données du véhicule sont obligatoires");
        }
        if (dto.immatriculation() == null || dto.immatriculation().isBlank()) {
            throw new BusinessRuleException("L'immatriculation est obligatoire");
        }
        if (dto.marque() == null || dto.marque().isBlank()) {
            throw new BusinessRuleException("La marque est obligatoire");
        }
        if (dto.modele() == null || dto.modele().isBlank()) {
            throw new BusinessRuleException("Le modèle est obligatoire");
        }
        if (dto.categorie() == null) {
            throw new BusinessRuleException("La catégorie est obligatoire");
        }
        if (dto.statut() == null) {
            throw new BusinessRuleException("Le statut est obligatoire");
        }
        if (dto.tarifJournalier() == null
                || dto.tarifJournalier().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Le tarif journalier doit être strictement positif");
        }
    }
}
