package com.salonbooking.booking.integration;

import com.salonbooking.api.catalog.ServiceOffering;
import com.salonbooking.api.salon.Salon;
import com.salonbooking.api.staff.AvailabilityResponse;

import java.time.LocalDateTime;

/**
 * Sve sto booking-service treba od drugih mikroservisa, na jednom mestu.
 *
 * Zasto interfejs a ne direktni pozivi iz servisnog sloja: poslovna logika
 * zakazivanja ne treba da zna da se iza ovoga krije REST. Zahvaljujuci tome
 * u testovima ubacujemo laznu implementaciju i testiramo logiku bez dizanja
 * ostalih servisa.
 */
public interface BookingIntegration {

    Salon getSalon(long salonId);

    ServiceOffering getService(long serviceId);

    AvailabilityResponse checkStaffAvailability(long staffId, LocalDateTime start, LocalDateTime end);
}
