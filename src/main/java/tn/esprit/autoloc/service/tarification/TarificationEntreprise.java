package tn.esprit.autoloc.service.tarification;

import org.springframework.stereotype.Component;

@Component("entreprise")
public class TarificationEntreprise implements StrategieTarification {

    private static final double REDUCTION = 0.85;

    @Override
    public double calculer(double tarifJournalier, int jours) {
        return tarifJournalier * jours * REDUCTION;
    }
}
