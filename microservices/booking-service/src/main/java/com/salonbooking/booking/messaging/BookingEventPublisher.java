package com.salonbooking.booking.messaging;

import com.salonbooking.api.event.BookingEvent;


public interface BookingEventPublisher {

    void publish(BookingEvent event);
}
