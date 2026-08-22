package com.salonbooking.booking.messaging;

import com.salonbooking.api.event.BookingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;


@Component
public class BookingEventPublisherImpl implements BookingEventPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(BookingEventPublisherImpl.class);

    private static final String BINDING = "bookingEvents-out-0";

    private final StreamBridge streamBridge;

    public BookingEventPublisherImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public void publish(BookingEvent event) {
        var message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getBookingId())
                .build();

        streamBridge.send(BINDING, message);
        LOG.info("Objavljen dogadjaj {} za termin id={}", event.getType(), event.getBookingId());
    }
}
