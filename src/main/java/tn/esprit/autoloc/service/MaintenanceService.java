package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Maintenance;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.MaintenanceRepository;
import tn.esprit.autoloc.repository.VehiculeRepository;
import tn.esprit.autoloc.web.dto.MaintenanceRequestDTO;
import tn.esprit.autoloc.web.dto.MaintenanceResponseDTO;
import tn.esprit.autoloc.web.exception.BusinessRuleException;
import tn.esprit.autoloc.web.exception.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final VehiculeRepository vehiculeRepository;

    public MaintenanceService(MaintenanceRepository maintenanceRepository, VehiculeRepository vehiculeRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.vehiculeRepository = vehiculeRepository;
    }

    public List<MaintenanceResponseDTO> findAll() {
        return maintenanceRepository.findAll().stream()
                .map(MaintenanceResponseDTO::from)
                .toList();
    }

    public MaintenanceResponseDTO findById(Long id) {
        return MaintenanceResponseDTO.from(getEntity(id));
    }

    @Transactional
    public MaintenanceResponseDTO create(MaintenanceRequestDTO dto) {
        validateDates(dto);
        Vehicule vehicule = getVehicule(dto.vehiculeId());
        Maintenance maintenance = new Maintenance();
        apply(maintenance, dto, vehicule);
        return MaintenanceResponseDTO.from(maintenanceRepository.save(maintenance));
    }

    @Transactional
    public MaintenanceResponseDTO update(Long id, MaintenanceRequestDTO dto) {
        validateDates(dto);
        Maintenance maintenance = getEntity(id);
        Vehicule vehicule = getVehicule(dto.vehiculeId());
        apply(maintenance, dto, vehicule);
        return MaintenanceResponseDTO.from(maintenanceRepository.save(maintenance));
    }

    @Transactional
    public void delete(Long id) {
        maintenanceRepository.delete(getEntity(id));
    }

    private Maintenance getEntity(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance", id));
    }

    private Vehicule getVehicule(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", id));
    }

    private void validateDates(MaintenanceRequestDTO dto) {
        if (dto.dateFin() != null && dto.dateFin().isBefore(dto.dateDebut())) {
            throw new BusinessRuleException("La date de fin doit être postérieure ou égale à la date de début");
        }
    }

    private void apply(Maintenance maintenance, MaintenanceRequestDTO dto, Vehicule vehicule) {
        maintenance.setDateDebut(dto.dateDebut());
        maintenance.setDateFin(dto.dateFin());
        maintenance.setDescription(dto.description() == null ? null : dto.description().trim());
        maintenance.setVehicule(vehicule);
    }
}
