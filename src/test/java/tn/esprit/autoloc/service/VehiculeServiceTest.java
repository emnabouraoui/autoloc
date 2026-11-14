package tn.esprit.autoloc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.AgenceRepository;
import tn.esprit.autoloc.repository.EquipementRepository;
import tn.esprit.autoloc.repository.VehiculeRepository;
import tn.esprit.autoloc.web.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehiculeServiceTest {

    @Mock
    private VehiculeRepository vehiculeRepository;

    @Mock
    private AgenceRepository agenceRepository;

    @Mock
    private EquipementRepository equipementRepository;

    @Mock
    private VehiculeValidator vehiculeValidator;

    @Mock
    private JournalisationService journalisationService;

    @InjectMocks
    private VehiculeService vehiculeService;

    @Test
    void affecteUnVehiculeAUneAgence() {
        Vehicule vehicule = vehicule(1L);
        Agence agence = agence(2L, "AutoLoc Sfax");

        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(agenceRepository.findById(2L)).thenReturn(Optional.of(agence));
        when(vehiculeRepository.save(vehicule)).thenReturn(vehicule);

        Vehicule resultat = vehiculeService.affecterVehicule(1L, 2L);

        assertThat(resultat.getAgence()).isSameAs(agence);
    }

    @Test
    void affecteUnEquipementAuVehicule() {
        Vehicule vehicule = vehicule(1L);
        Equipement equipement = equipement(10L, "Siège bébé");

        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(equipementRepository.findById(10L)).thenReturn(Optional.of(equipement));
        when(vehiculeRepository.save(vehicule)).thenReturn(vehicule);

        Vehicule resultat = vehiculeService.affecterEquipement(1L, 10L);

        assertThat(resultat.getEquipements()).containsExactly(equipement);
    }

    @Test
    void affecterDeuxFoisLeMemeEquipementResteIdempotent() {
        Vehicule vehicule = vehicule(1L);
        Equipement equipement = equipement(10L, "Siège bébé");

        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(equipementRepository.findById(10L)).thenReturn(Optional.of(equipement));
        when(vehiculeRepository.save(vehicule)).thenReturn(vehicule);

        vehiculeService.affecterEquipement(1L, 10L);
        vehiculeService.affecterEquipement(1L, 10L);

        assertThat(vehicule.getEquipements()).hasSize(1);
    }

    @Test
    void desaffecteUnEquipementDuVehicule() {
        Vehicule vehicule = vehicule(1L);
        Equipement equipement = equipement(10L, "Siège bébé");
        vehicule.getEquipements().add(equipement);

        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(equipementRepository.findById(10L)).thenReturn(Optional.of(equipement));
        when(vehiculeRepository.save(vehicule)).thenReturn(vehicule);

        Vehicule resultat = vehiculeService.desaffecterEquipement(1L, 10L);

        assertThat(resultat.getEquipements()).isEmpty();
    }

    @Test
    void refuseUnEquipementInexistant() {
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule(1L)));
        when(equipementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehiculeService.affecterEquipement(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Vehicule vehicule(Long id) {
        Vehicule vehicule = new Vehicule();
        vehicule.setIdVehicule(id);
        vehicule.setImmatriculation("AB-123-CD");
        vehicule.setMarque("Renault");
        vehicule.setModele("Clio");
        vehicule.setCategorie(CategorieVehicule.CITADINE);
        vehicule.setTarifJournalier(BigDecimal.valueOf(100));
        vehicule.setStatut(StatutVehicule.DISPONIBLE);
        return vehicule;
    }

    private Agence agence(Long id, String nom) {
        Agence agence = new Agence();
        agence.setIdAgence(id);
        agence.setNom(nom);
        agence.setVille("Sfax");
        agence.setAdresse("Avenue Habib Bourguiba");
        return agence;
    }

    private Equipement equipement(Long id, String libelle) {
        Equipement equipement = new Equipement();
        equipement.setIdEquipement(id);
        equipement.setLibelle(libelle);
        return equipement;
    }
}
