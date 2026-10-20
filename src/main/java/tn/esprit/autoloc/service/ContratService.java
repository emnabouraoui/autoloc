package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.repository.ContratRepository;
import tn.esprit.autoloc.repository.ReservationRepository;
import tn.esprit.autoloc.web.dto.ContratRequestDTO;
import tn.esprit.autoloc.web.dto.ContratResponseDTO;
import tn.esprit.autoloc.web.exception.BusinessRuleException;
import tn.esprit.autoloc.web.exception.ConflictException;
import tn.esprit.autoloc.web.exception.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ContratService {

    private final ContratRepository contratRepository;
    private final ReservationRepository reservationRepository;

    public ContratService(ContratRepository contratRepository, ReservationRepository reservationRepository) {
        this.contratRepository = contratRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ContratResponseDTO> findAll() {
        return contratRepository.findAll().stream()
                .map(ContratResponseDTO::from)
                .toList();
    }

    public ContratResponseDTO findById(Long id) {
        return ContratResponseDTO.from(getEntity(id));
    }

    @Transactional
    public ContratResponseDTO create(ContratRequestDTO dto) {
        Reservation reservation = getReservation(dto.reservationId());
        if (contratRepository.existsByReservation_IdReservation(reservation.getIdReservation())) {
            throw new ConflictException("Un contrat existe déjà pour cette réservation");
        }

        Contrat contrat = new Contrat();
        apply(contrat, dto, reservation);
        Contrat saved = contratRepository.save(contrat);
        reservation.setContrat(saved);
        return ContratResponseDTO.from(saved);
    }

    @Transactional
    public ContratResponseDTO update(Long id, ContratRequestDTO dto) {
        Contrat contrat = getEntity(id);
        if (!contrat.getReservation().getIdReservation().equals(dto.reservationId())) {
            throw new BusinessRuleException("La réservation d'un contrat ne peut pas être modifiée");
        }

        apply(contrat, dto, contrat.getReservation());
        return ContratResponseDTO.from(contratRepository.save(contrat));
    }

    @Transactional
    public void delete(Long id) {
        contratRepository.delete(getEntity(id));
    }

    private Contrat getEntity(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", id));
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    private void apply(Contrat contrat, ContratRequestDTO dto, Reservation reservation) {
        contrat.setDateSignature(dto.dateSignature());
        contrat.setMontantTotal(dto.montantTotal());
        contrat.setValide(dto.valide());
        contrat.setReservation(reservation);
    }
}
