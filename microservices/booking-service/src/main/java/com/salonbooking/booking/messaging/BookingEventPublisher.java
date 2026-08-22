package com.salonbooking.booking.messaging;

import com.salonbooking.api.event.BookingEvent;

/**
 * Objavljivanje dogadjaja o terminima.
 *
 * Iza interfejsa je zato sto poslovna logika ne treba da zna da je broker bas
 * Kafka - a i zato sto u testovima ubacujemo laznu implementaciju, pa testovi
 * rade bez pokretanja Kafke.
 */
public interface BookingEventPublisher {

    void publish(BookingEvent event);
}
