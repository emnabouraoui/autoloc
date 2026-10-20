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

    public VehiculeService(VehiculeRepository vehiculeRepository,
                           AgenceRepository agenceRepository,
                           EquipementRepository equipementRepository) {
        this.vehiculeRepository = vehiculeRepository;
        this.agenceRepository = agenceRepository;
        this.equipementRepository = equipementRepository;
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
        String immatriculation = dto.immatriculation().trim();
        if (vehiculeRepository.existsByImmatriculationIgnoreCase(immatriculation)) {
            throw new ConflictException("Un véhicule avec cette immatriculation existe déjà");
        }

        Vehicule vehicule = new Vehicule();
        apply(vehicule, dto);
        return VehiculeResponseDTO.from(vehiculeRepository.save(vehicule));
    }

    @Transactional
    public VehiculeResponseDTO update(Long id, VehiculeRequestDTO dto) {
        Vehicule vehicule = getEntity(id);
        String immatriculation = dto.immatriculation().trim();
        if (!vehicule.getImmatriculation().equalsIgnoreCase(immatriculation)
                && vehiculeRepository.existsByImmatriculationIgnoreCase(immatriculation)) {
            throw new ConflictException("Un véhicule avec cette immatriculation existe déjà");
        }

        apply(vehicule, dto);
        return VehiculeResponseDTO.from(vehiculeRepository.save(vehicule));
    }

    @Transactional
    public void delete(Long id) {
        vehiculeRepository.delete(getEntity(id));
    }

    private Vehicule getEntity(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", id));
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
            Agence agence = agenceRepository.findById(dto.agenceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agence", dto.agenceId()));
            vehicule.setAgence(agence);
        }

        Set<Equipement> equipements = new HashSet<>();
        if (dto.equipementIds() != null) {
            for (Long equipementId : dto.equipementIds()) {
                Equipement equipement = equipementRepository.findById(equipementId)
                        .orElseThrow(() -> new ResourceNotFoundException("Equipement", equipementId));
                equipements.add(equipement);
            }
        }
        vehicule.setEquipements(equipements);
    }
}
