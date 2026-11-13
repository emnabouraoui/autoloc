package tn.esprit.autoloc.service.tarification;

import org.springframework.stereotype.Service;
import tn.esprit.autoloc.web.exception.BusinessRuleException;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Service de calcul tarifaire.
 *
 * Il ne contient aucune formule : il sélectionne la stratégie adaptée dans la
 * Map injectée par Spring. Ajouter une politique revient donc à ajouter un
 * nouveau bean StrategieTarification, sans modifier cette classe (OCP).
 */
@Service
public class TarificationService {

    private final Map<String, StrategieTarification> strategies;

    public TarificationService(Map<String, StrategieTarification> strategies) {
        this.strategies = Map.copyOf(strategies);
    }

    public double calculer(String type, double tarifJournalier, int jours) {
        if (tarifJournalier <= 0) {
            throw new BusinessRuleException("Le tarif journalier doit être strictement positif");
        }
        if (jours <= 0) {
            throw new BusinessRuleException("Le nombre de jours doit être strictement positif");
        }
        if (type == null || type.isBlank()) {
            throw new BusinessRuleException("Le type de tarification est obligatoire");
        }

        String typeRecherche = type.trim();
        StrategieTarification strategie = strategies.get(typeRecherche);
        if (strategie == null) {
            strategie = strategies.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(typeRecherche))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElseThrow(() -> new BusinessRuleException(
                            "Type de tarification inconnu : " + type
                                    + ". Types disponibles : " + String.join(", ", typesDisponibles())));
        }

        return strategie.calculer(tarifJournalier, jours);
    }

    public Set<String> typesDisponibles() {
        return Collections.unmodifiableSet(new TreeSet<>(strategies.keySet()));
    }
}
