package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.AgenceRepository;
import tn.esprit.autoloc.repository.EquipementRepository;
import tn.esprit.autoloc.repository.VehiculeRepository;
import tn.esprit.autoloc.web.dto.VehiculeRequestDTO;
import tn.esprit.autoloc.web.dto.VehiculeResponseDTO;
import tn.esprit.autoloc.web.exception.ConflictException;
import tn.esprit.autoloc.web.exception.ResourceNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final AgenceRepository agenceRepository;
    private final EquipementRepository equipementRepository;
    private final VehiculeValidator vehiculeValidator;
    private final JournalisationService journalisationService;

    public VehiculeService(VehiculeRepository vehiculeRepository,
                           AgenceRepository agenceRepository,
                           EquipementRepository equipementRepository,
                           VehiculeValidator vehiculeValidator,
                           JournalisationService journalisationService) {
        this.vehiculeRepository = vehiculeRepository;
        this.agenceRepository = agenceRepository;
        this.equipementRepository = equipementRepository;
        this.vehiculeValidator = vehiculeValidator;
        this.journalisationService = journalisationService;
    }

    public List<VehiculeResponseDTO> findAll() {
        return vehiculeRepository.findAll().stream()
                .map(VehiculeResponseDTO::from)
                .toList();
    }

    public VehiculeResponseDTO findById(Long id) {
        return VehiculeResponseDTO.from(getEntity(id));
    }

    @Transactional
    public VehiculeResponseDTO create(VehiculeRequestDTO dto) {
        vehiculeValidator.valider(dto);

        String immatriculation = dto.immatriculation().trim();
        if (vehiculeRepository.existsByImmatriculationIgnoreCase(immatriculation)) {
            throw new ConflictException("Un véhicule avec cette immatriculation existe déjà");
        }

        Vehicule vehicule = new Vehicule();
        apply(vehicule, dto);
        Vehicule saved = vehiculeRepository.save(vehicule);
        journalisationService.journaliserCreationVehicule(saved.getIdVehicule(), saved.getImmatriculation());
        return VehiculeResponseDTO.from(saved);
    }

    @Transactional
    public VehiculeResponseDTO update(Long id, VehiculeRequestDTO dto) {
        vehiculeValidator.valider(dto);

        Vehicule vehicule = getEntity(id);
        String immatriculation = dto.immatriculation().trim();
        if (!vehicule.getImmatriculation().equalsIgnoreCase(immatriculation)
                && vehiculeRepository.existsByImmatriculationIgnoreCase(immatriculation)) {
            throw new ConflictException("Un véhicule avec cette immatriculation existe déjà");
        }

        apply(vehicule, dto);
        Vehicule saved = vehiculeRepository.save(vehicule);
        journalisationService.journaliserModificationVehicule(saved.getIdVehicule(), saved.getImmatriculation());
        return VehiculeResponseDTO.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        Vehicule vehicule = getEntity(id);
        Long idVehicule = vehicule.getIdVehicule();
        String immatriculation = vehicule.getImmatriculation();
        vehiculeRepository.delete(vehicule);
        journalisationService.journaliserSuppressionVehicule(idVehicule, immatriculation);
    }

    /**
     * Affecte ou transfère un véhicule vers une agence.
     */
    @Transactional
    public Vehicule affecterVehicule(Long idVehicule, Long idAgence) {
        Vehicule vehicule = getEntity(idVehicule);
        vehicule.setAgence(getAgence(idAgence));

        Vehicule saved = vehiculeRepository.save(vehicule);
        journalisationService.journaliserModificationVehicule(
                saved.getIdVehicule(), saved.getImmatriculation());
        return saved;
    }

    /**
     * Ajoute un équipement au véhicule.
     * L'opération est idempotente : un équipement déjà affecté n'est pas ajouté deux fois.
     */
    @Transactional
    public Vehicule affecterEquipement(Long idVehicule, Long idEquipement) {
        Vehicule vehicule = getEntity(idVehicule);
        Equipement equipement = getEquipement(idEquipement);

        boolean dejaAffecte = vehicule.getEquipements().stream()
                .anyMatch(e -> e.getIdEquipement().equals(idEquipement));
        if (!dejaAffecte) {
            vehicule.getEquipements().add(equipement);
        }

        Vehicule saved = vehiculeRepository.save(vehicule);
        journalisationService.journaliserModificationVehicule(
                saved.getIdVehicule(), saved.getImmatriculation());
        return saved;
    }

    /**
     * Retire un équipement du véhicule.
     * L'opération est idempotente : retirer un équipement absent ne provoque pas d'erreur.
     */
    @Transactional
    public Vehicule desaffecterEquipement(Long idVehicule, Long idEquipement) {
        Vehicule vehicule = getEntity(idVehicule);
        getEquipement(idEquipement);
        vehicule.getEquipements().removeIf(
                e -> e.getIdEquipement().equals(idEquipement));

        Vehicule saved = vehiculeRepository.save(vehicule);
        journalisationService.journaliserModificationVehicule(
                saved.getIdVehicule(), saved.getImmatriculation());
        return saved;
    }

    private Vehicule getEntity(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", id));
    }

    private Agence getAgence(Long id) {
        return agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", id));
    }

    private Equipement getEquipement(Long id) {
        return equipementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipement", id));
    }

    private void apply(Vehicule vehicule, VehiculeRequestDTO dto) {
        vehicule.setImmatriculation(dto.immatriculation().trim());
        vehicule.setMarque(dto.marque().trim());
        vehicule.setModele(dto.modele().trim());
        vehicule.setCategorie(dto.categorie());
        vehicule.setTarifJournalier(dto.tarifJournalier());
        vehicule.setStatut(dto.statut());

        if (dto.agenceId() == null) {
            vehicule.setAgence(null);
        } else {
            vehicule.setAgence(getAgence(dto.agenceId()));
        }

        Set<Equipement> equipements = new HashSet<>();
        if (dto.equipementIds() != null) {
            for (Long equipementId : dto.equipementIds()) {
                equipements.add(getEquipement(equipementId));
            }
        }
        vehicule.setEquipements(equipements);
    }
}
