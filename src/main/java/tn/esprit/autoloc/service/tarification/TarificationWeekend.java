package tn.esprit.autoloc.service.tarification;

import org.springframework.stereotype.Component;

@Component("weekend")
public class TarificationWeekend implements StrategieTarification {

    private static final double REDUCTION = 0.90;

    @Override
    public double calculer(double tarifJournalier, int jours) {
        return tarifJournalier * jours * REDUCTION;
    }
}
