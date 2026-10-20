package tn.esprit.autoloc.web.dto;

import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.StatutReservation;

import java.time.LocalDate;

public record ReservationResponseDTO(
        Long idReservation,
        LocalDate dateDebut,
        LocalDate dateFin,
        StatutReservation statut,
        Long clientId,
        String clientNom,
        String clientPrenom,
        Long vehiculeId,
        String immatriculation,
        String marque,
        String modele,
        Long contratId
) {
    public static ReservationResponseDTO from(Reservation reservation) {
        Long clientId = reservation.getClient() == null ? null : reservation.getClient().getIdClient();
        String clientNom = reservation.getClient() == null ? null : reservation.getClient().getNom();
        String clientPrenom = reservation.getClient() == null ? null : reservation.getClient().getPrenom();
        Long vehiculeId = reservation.getVehicule() == null ? null : reservation.getVehicule().getIdVehicule();
        String immatriculation = reservation.getVehicule() == null ? null : reservation.getVehicule().getImmatriculation();
        String marque = reservation.getVehicule() == null ? null : reservation.getVehicule().getMarque();
        String modele = reservation.getVehicule() == null ? null : reservation.getVehicule().getModele();
        Long contratId = reservation.getContrat() == null ? null : reservation.getContrat().getIdContrat();

        return new ReservationResponseDTO(
                reservation.getIdReservation(),
                reservation.getDateDebut(),
                reservation.getDateFin(),
                reservation.getStatut(),
                clientId,
                clientNom,
                clientPrenom,
                vehiculeId,
                immatriculation,
                marque,
                modele,
                contratId
        );
    }
}
