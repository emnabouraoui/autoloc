package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.repository.AgenceRepository;
import tn.esprit.autoloc.web.dto.AgenceRequestDTO;
import tn.esprit.autoloc.web.dto.AgenceResponseDTO;
import tn.esprit.autoloc.web.exception.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AgenceService {

    private final AgenceRepository agenceRepository;

    public AgenceService(AgenceRepository agenceRepository) {
        this.agenceRepository = agenceRepository;
    }

    public List<AgenceResponseDTO> findAll() {
        return agenceRepository.findAll().stream()
                .map(AgenceResponseDTO::from)
                .toList();
    }

    public AgenceResponseDTO findById(Long id) {
        return AgenceResponseDTO.from(getEntity(id));
    }

    @Transactional
    public AgenceResponseDTO create(AgenceRequestDTO dto) {
        Agence agence = new Agence();
        apply(agence, dto);
        return AgenceResponseDTO.from(agenceRepository.save(agence));
    }

    @Transactional
    public AgenceResponseDTO update(Long id, AgenceRequestDTO dto) {
        Agence agence = getEntity(id);
        apply(agence, dto);
        return AgenceResponseDTO.from(agenceRepository.save(agence));
    }

    @Transactional
    public void delete(Long id) {
        agenceRepository.delete(getEntity(id));
    }

    private Agence getEntity(Long id) {
        return agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", id));
    }

    private void apply(Agence agence, AgenceRequestDTO dto) {
        agence.setNom(dto.nom().trim());
        agence.setVille(dto.ville().trim());
        agence.setAdresse(dto.adresse().trim());
        agence.setTelephone(dto.telephone() == null ? null : dto.telephone().trim());
    }
}
