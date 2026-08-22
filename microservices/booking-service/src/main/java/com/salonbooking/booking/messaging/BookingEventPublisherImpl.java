package com.salonbooking.booking.messaging;

import com.salonbooking.api.event.BookingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Salje dogadjaje na Kafku preko Spring Cloud Stream-a.
 *
 * StreamBridge salje poruku na binding "bookingEvents-out-0", cije se mapiranje
 * na konkretan Kafka topic definise u application.yml. Zahvaljujuci tome ime
 * topic-a nije zakucano u kodu, a zamena Kafke drugim brokerom (npr. RabbitMQ)
 * svela bi se na promenu jedne zavisnosti i konfiguracije.
 *
 * Poruke se salju sa kljucem = bookingId (setHeader KEY). Kafka garantuje
 * redosled unutar jedne particije, a poruke sa istim kljucem uvek idu u istu
 * particiju - tako se za jedan termin dogadjaji CREATED -> CANCELLED nikada
 * ne mogu obraditi obrnutim redosledom.
 */
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
