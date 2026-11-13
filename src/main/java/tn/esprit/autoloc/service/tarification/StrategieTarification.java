package tn.esprit.autoloc.service.tarification;

/**
 * Contrat commun à toutes les politiques tarifaires (GoF Strategy).
 *
 * Chaque implémentation encapsule une seule formule de calcul.
 */
@FunctionalInterface
public interface StrategieTarification {

    double calculer(double tarifJournalier, int jours);
}
