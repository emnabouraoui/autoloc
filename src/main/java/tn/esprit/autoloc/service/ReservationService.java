package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.StatutReservation;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.ClientRepository;
import tn.esprit.autoloc.repository.ReservationRepository;
import tn.esprit.autoloc.repository.VehiculeRepository;
import tn.esprit.autoloc.web.dto.ReservationRequestDTO;
import tn.esprit.autoloc.web.dto.ReservationResponseDTO;
import tn.esprit.autoloc.web.exception.BusinessRuleException;
import tn.esprit.autoloc.web.exception.ConflictException;
import tn.esprit.autoloc.web.exception.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final List<StatutReservation> ACTIVE_STATUSES =
            List.of(StatutReservation.EN_ATTENTE, StatutReservation.CONFIRMEE);

    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ClientRepository clientRepository,
                              VehiculeRepository vehiculeRepository) {
        this.reservationRepository = reservationRepository;
        this.clientRepository = clientRepository;
        this.vehiculeRepository = vehiculeRepository;
    }

    public List<ReservationResponseDTO> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponseDTO::from)
                .toList();
    }

    public ReservationResponseDTO findById(Long id) {
        return ReservationResponseDTO.from(getEntity(id));
    }

    @Transactional
    public ReservationResponseDTO create(ReservationRequestDTO dto) {
        validateDates(dto);
        Client client = getClient(dto.clientId());
        Vehicule vehicule = getVehicule(dto.vehiculeId());
        checkAvailability(vehicule, dto, null);

        Reservation reservation = new Reservation();
        apply(reservation, dto, client, vehicule);
        return ReservationResponseDTO.from(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponseDTO update(Long id, ReservationRequestDTO dto) {
        Reservation reservation = getEntity(id);
        validateDates(dto);
        Client client = getClient(dto.clientId());
        Vehicule vehicule = getVehicule(dto.vehiculeId());
        checkAvailability(vehicule, dto, id);

        apply(reservation, dto, client, vehicule);
        return ReservationResponseDTO.from(reservationRepository.save(reservation));
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.delete(getEntity(id));
    }

    private Reservation getEntity(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    private Client getClient(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    private Vehicule getVehicule(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", id));
    }

    private void validateDates(ReservationRequestDTO dto) {
        if (dto.dateFin().isBefore(dto.dateDebut())) {
            throw new BusinessRuleException("La date de fin doit être postérieure ou égale à la date de début");
        }
    }

    private void checkAvailability(Vehicule vehicule, ReservationRequestDTO dto, Long excludedReservationId) {
        if (!ACTIVE_STATUSES.contains(dto.statut())) {
            return;
        }

        if (vehicule.getStatut() == StatutVehicule.MAINTENANCE) {
            throw new BusinessRuleException("Le véhicule est en maintenance et ne peut pas être réservé");
        }

        long overlapping = excludedReservationId == null
                ? reservationRepository.countOverlapping(
                        dto.vehiculeId(), dto.dateDebut(), dto.dateFin(), ACTIVE_STATUSES)
                : reservationRepository.countOverlappingExcluding(
                        dto.vehiculeId(), dto.dateDebut(), dto.dateFin(), ACTIVE_STATUSES, excludedReservationId);

        if (overlapping > 0) {
            throw new ConflictException("Le véhicule est déjà réservé sur cette période");
        }
    }

    private void apply(Reservation reservation, ReservationRequestDTO dto, Client client, Vehicule vehicule) {
        reservation.setDateDebut(dto.dateDebut());
        reservation.setDateFin(dto.dateFin());
        reservation.setStatut(dto.statut());
        reservation.setClient(client);
        reservation.setVehicule(vehicule);
    }
}
