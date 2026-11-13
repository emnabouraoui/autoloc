package tn.esprit.autoloc.service;

import org.junit.jupiter.api.Test;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.web.dto.VehiculeRequestDTO;
import tn.esprit.autoloc.web.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehiculeValidatorTest {

    private final VehiculeValidator validator = new VehiculeValidator();

    @Test
    void accepteUnVehiculeValide() {
        assertThatCode(() -> validator.valider(vehicule("AB-123-CD", BigDecimal.valueOf(100))))
                .doesNotThrowAnyException();
    }

    @Test
    void refuseUnTarifNonPositif() {
        assertThatThrownBy(() -> validator.valider(vehicule("AB-123-CD", BigDecimal.ZERO)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("tarif");
    }

    @Test
    void refuseUneImmatriculationVide() {
        assertThatThrownBy(() -> validator.valider(vehicule(" ", BigDecimal.valueOf(100))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("immatriculation");
    }

    private VehiculeRequestDTO vehicule(String immatriculation, BigDecimal tarifJournalier) {
        return new VehiculeRequestDTO(
                immatriculation,
                "Renault",
                "Clio",
                CategorieVehicule.CITADINE,
                tarifJournalier,
                StatutVehicule.DISPONIBLE,
                null,
                Set.of()
        );
    }
}
