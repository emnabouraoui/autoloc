package tn.esprit.autoloc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Centralise la journalisation applicative.
 *
 * Les services métier ne dépendent plus directement de System.out ou d'un
 * framework de logs : ils passent par ce composant dédié.
 */
@Service
public class JournalisationService {

    private static final Logger log = LoggerFactory.getLogger(JournalisationService.class);

    public void journaliserCreationVehicule(Long idVehicule, String immatriculation) {
        log.info("Création du véhicule id={} immatriculation={}", idVehicule, immatriculation);
    }

    public void journaliserModificationVehicule(Long idVehicule, String immatriculation) {
        log.info("Modification du véhicule id={} immatriculation={}", idVehicule, immatriculation);
    }

    public void journaliserSuppressionVehicule(Long idVehicule, String immatriculation) {
        log.info("Suppression du véhicule id={} immatriculation={}", idVehicule, immatriculation);
    }
}
