package tn.esprit.autoloc.service.tarification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.autoloc.web.exception.BusinessRuleException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class TarificationServiceTest {

    private TarificationService service;

    @BeforeEach
    void setUp() {
        service = new TarificationService(Map.of(
                "standard", new TarificationStandard(),
                "weekend", new TarificationWeekend(),
                "longueDuree", new TarificationLongueDuree(),
                "entreprise", new TarificationEntreprise()
        ));
    }

    @Test
    void calculeLeTarifStandard() {
        assertThat(service.calculer("standard", 100.0, 3))
                .isCloseTo(300.0, within(0.0001));
    }

    @Test
    void appliqueLaReductionWeekend() {
        assertThat(service.calculer("weekend", 100.0, 3))
                .isCloseTo(270.0, within(0.0001));
    }

    @Test
    void appliqueLaReductionLongueDuree() {
        assertThat(service.calculer("longueDuree", 100.0, 3))
                .isCloseTo(240.0, within(0.0001));
    }

    @Test
    void appliqueLaReductionEntreprise() {
        assertThat(service.calculer("entreprise", 100.0, 3))
                .isCloseTo(255.0, within(0.0001));
    }

    @Test
    void accepteUnTypeSansTenirCompteDeLaCasse() {
        assertThat(service.calculer("STANDARD", 100.0, 3))
                .isCloseTo(300.0, within(0.0001));
    }

    @Test
    void refuseUnTypeInconnu() {
        assertThatThrownBy(() -> service.calculer("inconnu", 100.0, 3))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inconnu")
                .hasMessageContaining("standard");
    }

    @Test
    void refuseUnNombreDeJoursInvalide() {
        assertThatThrownBy(() -> service.calculer("standard", 100.0, 0))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("jours");
    }

    @Test
    void refuseUnTarifInvalide() {
        assertThatThrownBy(() -> service.calculer("standard", 0.0, 3))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("tarif");
    }

    @Test
    void ajouteUneNouvelleStrategieSansModifierLeService() {
        Map<String, StrategieTarification> strategies = new HashMap<>();
        strategies.put("standard", new TarificationStandard());
        strategies.put("promo", (tarifJournalier, jours) -> tarifJournalier * jours * 0.50);

        TarificationService serviceAvecPromo = new TarificationService(strategies);

        assertThat(serviceAvecPromo.calculer("promo", 100.0, 2))
                .isCloseTo(100.0, within(0.0001));
    }

    @Test
    void exposeLesTypesDisponibles() {
        assertThat(service.typesDisponibles())
                .containsExactly("entreprise", "longueDuree", "standard", "weekend");
    }
}
