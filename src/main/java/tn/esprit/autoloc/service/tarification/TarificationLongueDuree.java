package tn.esprit.autoloc.service.tarification;

import org.springframework.stereotype.Component;

@Component("longueDuree")
public class TarificationLongueDuree implements StrategieTarification {

    private static final double REDUCTION = 0.80;

    @Override
    public double calculer(double tarifJournalier, int jours) {
        return tarifJournalier * jours * REDUCTION;
    }
}
