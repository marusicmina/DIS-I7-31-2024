package com.salonbooking.api.event;

/**
 * Vrste dogadjaja koje booking-service objavljuje na Kafku.
 *
 * Dogadjaj opisuje nesto sto se VEC desilo (proslo vreme), zato imena nisu
 * naredbe ("posalji mejl") nego cinjenice. To je sustinska razlika u odnosu na
 * sinhroni poziv: booking-service ne zna niti ga zanima ko slusa. Danas je to
 * notification-service, sutra i review-service, a booking se ne menja.
 */
public enum BookingEventType {
    BOOKING_CREATED,
    BOOKING_CANCELLED,
    BOOKING_COMPLETED
}
