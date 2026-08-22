package com.salonbooking.notification.services;

import com.salonbooking.api.event.BookingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;


@Service
public class NotificationSender {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationSender.class);
    private static final DateTimeFormatter HUMAN = DateTimeFormatter.ofPattern("dd.MM.yyyy. 'u' HH:mm");

    public void sendBookingConfirmation(BookingEvent event) {
        String message = String.format(
                "Postovani, Vas termin je potvrdjen. Usluga: %s, vreme: %s, cena: %s RSD.",
                event.getServiceName(), event.getStartTime().format(HUMAN), event.getPrice());
        deliver(event, "Potvrda termina", message);
    }

    public void sendBookingCancellation(BookingEvent event) {
        String message = String.format(
                "Postovani, Vas termin za uslugu %s zakazan za %s je otkazan.",
                event.getServiceName(), event.getStartTime().format(HUMAN));
        deliver(event, "Otkazivanje termina", message);
    }

    public void sendThankYou(BookingEvent event) {
        String message = String.format(
                "Hvala Vam na poseti! Nadamo se da ste zadovoljni uslugom %s. "
                        + "Vas utisak mozete ostaviti kroz aplikaciju.",
                event.getServiceName());
        deliver(event, "Hvala na poseti", message);
    }

    private void deliver(BookingEvent event, String subject, String body) {
        LOG.info("[NOTIFIKACIJA] klijent={} | termin={} | {} -> {}",
                event.getClientId(), event.getBookingId(), subject, body);
    }
}
