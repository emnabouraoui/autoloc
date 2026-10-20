package tn.esprit.autoloc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.StatutReservation;

import java.time.LocalDate;
import java.util.Collection;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            select count(r) from Reservation r
            where r.vehicule.idVehicule = :vehiculeId
              and r.statut in :statuts
              and r.dateDebut <= :dateFin
              and r.dateFin >= :dateDebut
            """)
    long countOverlapping(@Param("vehiculeId") Long vehiculeId,
                          @Param("dateDebut") LocalDate dateDebut,
                          @Param("dateFin") LocalDate dateFin,
                          @Param("statuts") Collection<StatutReservation> statuts);

    @Query("""
            select count(r) from Reservation r
            where r.vehicule.idVehicule = :vehiculeId
              and r.statut in :statuts
              and r.dateDebut <= :dateFin
              and r.dateFin >= :dateDebut
              and r.idReservation <> :excludeId
            """)
    long countOverlappingExcluding(@Param("vehiculeId") Long vehiculeId,
                                   @Param("dateDebut") LocalDate dateDebut,
                                   @Param("dateFin") LocalDate dateFin,
                                   @Param("statuts") Collection<StatutReservation> statuts,
                                   @Param("excludeId") Long excludeId);
}
