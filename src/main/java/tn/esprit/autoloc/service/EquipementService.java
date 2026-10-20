package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.repository.EquipementRepository;
import tn.esprit.autoloc.web.dto.EquipementRequestDTO;
import tn.esprit.autoloc.web.dto.EquipementResponseDTO;
import tn.esprit.autoloc.web.exception.ConflictException;
import tn.esprit.autoloc.web.exception.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EquipementService {

    private final EquipementRepository equipementRepository;

    public EquipementService(EquipementRepository equipementRepository) {
        this.equipementRepository = equipementRepository;
    }

    public List<EquipementResponseDTO> findAll() {
        return equipementRepository.findAll().stream()
                .map(EquipementResponseDTO::from)
                .toList();
    }

    public EquipementResponseDTO findById(Long id) {
        return EquipementResponseDTO.from(getEntity(id));
    }

    @Transactional
    public EquipementResponseDTO create(EquipementRequestDTO dto) {
        String libelle = dto.libelle().trim();
        if (equipementRepository.existsByLibelleIgnoreCase(libelle)) {
            throw new ConflictException("Un équipement avec ce libellé existe déjà");
        }

        Equipement equipement = new Equipement();
        equipement.setLibelle(libelle);
        return EquipementResponseDTO.from(equipementRepository.save(equipement));
    }

    @Transactional
    public EquipementResponseDTO update(Long id, EquipementRequestDTO dto) {
        Equipement equipement = getEntity(id);
        String libelle = dto.libelle().trim();
        if (!equipement.getLibelle().equalsIgnoreCase(libelle)
                && equipementRepository.existsByLibelleIgnoreCase(libelle)) {
            throw new ConflictException("Un équipement avec ce libellé existe déjà");
        }

        equipement.setLibelle(libelle);
        return EquipementResponseDTO.from(equipementRepository.save(equipement));
    }

    @Transactional
    public void delete(Long id) {
        equipementRepository.delete(getEntity(id));
    }

    private Equipement getEntity(Long id) {
        return equipementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipement", id));
    }
}
