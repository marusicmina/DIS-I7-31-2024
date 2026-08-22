package com.salonbooking.booking.integration;

import com.salonbooking.api.catalog.ServiceOffering;
import com.salonbooking.api.salon.Salon;
import com.salonbooking.api.staff.AvailabilityResponse;

import java.time.LocalDateTime;


public interface BookingIntegration {

    Salon getSalon(long salonId);

    ServiceOffering getService(long serviceId);

    AvailabilityResponse checkStaffAvailability(long staffId, LocalDateTime start, LocalDateTime end);
}
