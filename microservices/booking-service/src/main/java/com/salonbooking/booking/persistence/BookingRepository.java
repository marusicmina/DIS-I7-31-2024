package com.salonbooking.booking.persistence;

import com.salonbooking.api.booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findByClientId(long clientId);

    List<BookingEntity> findByStaffId(long staffId);

    List<BookingEntity> findBySalonId(long salonId);

    /**
     * Da li zaposleni vec ima termin koji se preklapa sa trazenim intervalom?
     *
     * Dva intervala se preklapaju ako jedan pocinje pre nego sto se drugi zavrsi,
     * I zavrsava se posle nego sto drugi pocne:
     *
     *     postojeci:  |-------|
     *     novi:            |-------|      preklapa se
     *
     *     postojeci:  |-------|
     *     novi:               |-------|   NE preklapa se (dodiruju se)
     *
     * Zato strogo < i > : termin koji pocinje tacno kad se prethodni zavrsava
     * je sasvim u redu i mora da prodje.
     *
     * Otkazani termini se ne racunaju - oni su oslobodili slot.
     */
    @Query("""
            select count(b) > 0 from BookingEntity b
            where b.staffId = :staffId
              and b.status <> :cancelled
              and b.startTime < :end
              and b.endTime > :start
            """)
    boolean existsOverlappingBooking(@Param("staffId") long staffId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      @Param("cancelled") BookingStatus cancelled);
}
