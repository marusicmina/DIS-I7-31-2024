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
