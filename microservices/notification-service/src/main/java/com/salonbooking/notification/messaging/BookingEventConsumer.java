package com.salonbooking.notification.messaging;

import com.salonbooking.api.event.BookingEvent;
import com.salonbooking.notification.services.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;


@Configuration
public class BookingEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(BookingEventConsumer.class);

    private final NotificationSender sender;

    public BookingEventConsumer(NotificationSender sender) {
        this.sender = sender;
    }

    @Bean
    public Consumer<BookingEvent> bookingEvents() {
        return event -> {
            if (event == null || event.getType() == null) {
                LOG.warn("Primljen dogadjaj bez tipa - preskacem");
                return;
            }

            LOG.debug("Primljen dogadjaj {} za termin id={}", event.getType(), event.getBookingId());

            switch (event.getType()) {
                case BOOKING_CREATED -> sender.sendBookingConfirmation(event);
                case BOOKING_CANCELLED -> sender.sendBookingCancellation(event);
                case BOOKING_COMPLETED -> sender.sendThankYou(event);
            }
        };
    }
}
