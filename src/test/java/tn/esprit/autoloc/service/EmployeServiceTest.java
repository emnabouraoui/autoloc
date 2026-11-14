package tn.esprit.autoloc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.domain.RoleEmploye;
import tn.esprit.autoloc.repository.AgenceRepository;
import tn.esprit.autoloc.repository.EmployeRepository;
import tn.esprit.autoloc.web.exception.ResourceNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeServiceTest {

    @Mock
    private EmployeRepository employeRepository;

    @Mock
    private AgenceRepository agenceRepository;

    @InjectMocks
    private EmployeService employeService;

    @Test
    void affecteUnEmployeAUneAgence() {
        Employe employe = employe(1L);
        Agence agence = agence(2L, "AutoLoc Tunis");

        when(employeRepository.findById(1L)).thenReturn(Optional.of(employe));
        when(agenceRepository.findById(2L)).thenReturn(Optional.of(agence));
        when(employeRepository.save(employe)).thenReturn(employe);

        Employe resultat = employeService.affecterEmploye(1L, 2L);

        assertThat(resultat.getAgence()).isSameAs(agence);
        verify(employeRepository).save(employe);
    }

    @Test
    void desaffecteUnEmployeDeSonAgence() {
        Employe employe = employe(1L);
        employe.setAgence(agence(2L, "AutoLoc Tunis"));

        when(employeRepository.findById(1L)).thenReturn(Optional.of(employe));
        when(employeRepository.save(employe)).thenReturn(employe);

        Employe resultat = employeService.desaffecterEmploye(1L);

        assertThat(resultat.getAgence()).isNull();
        verify(employeRepository).save(employe);
    }

    @Test
    void refuseUneAgenceInexistante() {
        when(employeRepository.findById(1L)).thenReturn(Optional.of(employe(1L)));
        when(agenceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeService.affecterEmploye(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Employe employe(Long id) {
        Employe employe = new Employe();
        employe.setIdEmploye(id);
        employe.setNom("Bouraoui");
        employe.setPrenom("Emna");
        employe.setRole(RoleEmploye.AGENT);
        return employe;
    }

    private Agence agence(Long id, String nom) {
        Agence agence = new Agence();
        agence.setIdAgence(id);
        agence.setNom(nom);
        agence.setVille("Tunis");
        agence.setAdresse("12 rue de la République");
        return agence;
    }
}
