package com.salonbooking.booking.services;

import com.salonbooking.api.booking.Booking;
import com.salonbooking.booking.persistence.BookingEntity;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public Booking entityToApi(BookingEntity entity, String serviceAddress) {
        return new Booking(
                entity.getId(),
                entity.getClientId(),
                entity.getSalonId(),
                entity.getStaffId(),
                entity.getServiceId(),
                entity.getServiceName(),
                entity.getPrice(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStatus(),
                entity.getNote(),
                entity.getCreatedAt(),
                serviceAddress
        );
    }
}
