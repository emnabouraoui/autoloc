package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.repository.AgenceRepository;
import tn.esprit.autoloc.repository.EmployeRepository;
import tn.esprit.autoloc.web.dto.EmployeRequestDTO;
import tn.esprit.autoloc.web.dto.EmployeResponseDTO;
import tn.esprit.autoloc.web.exception.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final AgenceRepository agenceRepository;

    public EmployeService(EmployeRepository employeRepository, AgenceRepository agenceRepository) {
        this.employeRepository = employeRepository;
        this.agenceRepository = agenceRepository;
    }

    public List<EmployeResponseDTO> findAll() {
        return employeRepository.findAll().stream()
                .map(EmployeResponseDTO::from)
                .toList();
    }

    public EmployeResponseDTO findById(Long id) {
        return EmployeResponseDTO.from(getEntity(id));
    }

    @Transactional
    public EmployeResponseDTO create(EmployeRequestDTO dto) {
        Employe employe = new Employe();
        apply(employe, dto);
        return EmployeResponseDTO.from(employeRepository.save(employe));
    }

    @Transactional
    public EmployeResponseDTO update(Long id, EmployeRequestDTO dto) {
        Employe employe = getEntity(id);
        apply(employe, dto);
        return EmployeResponseDTO.from(employeRepository.save(employe));
    }

    @Transactional
    public void delete(Long id) {
        employeRepository.delete(getEntity(id));
    }

    private Employe getEntity(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", id));
    }

    private void apply(Employe employe, EmployeRequestDTO dto) {
        employe.setNom(dto.nom().trim());
        employe.setPrenom(dto.prenom().trim());
        employe.setRole(dto.role());

        Agence agence = agenceRepository.findById(dto.agenceId())
                .orElseThrow(() -> new ResourceNotFoundException("Agence", dto.agenceId()));
        employe.setAgence(agence);
    }
}
