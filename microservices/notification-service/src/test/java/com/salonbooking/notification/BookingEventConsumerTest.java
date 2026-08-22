package com.salonbooking.notification;

import com.salonbooking.api.event.BookingEvent;
import com.salonbooking.api.event.BookingEventType;
import com.salonbooking.notification.messaging.BookingEventConsumer;
import com.salonbooking.notification.services.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;


class BookingEventConsumerTest {

    private NotificationSender sender;
    private Consumer<BookingEvent> consumer;

    @BeforeEach
    void setUp() {
        sender = mock(NotificationSender.class);
        consumer = new BookingEventConsumer(sender).bookingEvents();
    }

    @Test
    void createdEvent_sendsConfirmation() {
        BookingEvent event = event(BookingEventType.BOOKING_CREATED);

        consumer.accept(event);

        verify(sender).sendBookingConfirmation(event);
        verify(sender, never()).sendBookingCancellation(any());
        verify(sender, never()).sendThankYou(any());
    }

    @Test
    void cancelledEvent_sendsCancellation() {
        BookingEvent event = event(BookingEventType.BOOKING_CANCELLED);

        consumer.accept(event);

        verify(sender).sendBookingCancellation(event);
        verify(sender, never()).sendBookingConfirmation(any());
    }

    @Test
    void completedEvent_sendsThankYou() {
        BookingEvent event = event(BookingEventType.BOOKING_COMPLETED);

        consumer.accept(event);

        verify(sender).sendThankYou(event);
    }

    @Test
    void eventWithoutType_isIgnoredWithoutError() {
        BookingEvent event = event(null);

        consumer.accept(event);

        verifyNoInteractions(sender);
    }

    @Test
    void nullEvent_isIgnoredWithoutError() {
        consumer.accept(null);

        verifyNoInteractions(sender);
    }

    @Test
    void eventsAreProcessedInOrderTheyArrive() {
        BookingEvent created = event(BookingEventType.BOOKING_CREATED);
        BookingEvent cancelled = event(BookingEventType.BOOKING_CANCELLED);

        consumer.accept(created);
        consumer.accept(cancelled);

        InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).sendBookingConfirmation(created);
        inOrder.verify(sender).sendBookingCancellation(cancelled);
    }

    private BookingEvent event(BookingEventType type) {
        return new BookingEvent(
                type, 1L, 55L, 1L, 10L, 100L,
                "Zensko sisanje", new BigDecimal("1500.00"),
                LocalDateTime.of(2026, 9, 7, 10, 0),
                LocalDateTime.of(2026, 9, 7, 10, 45),
                LocalDateTime.of(2026, 9, 1, 12, 0));
    }
}
