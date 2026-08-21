package com.salonbooking.api.booking;

/**
 * Zivotni ciklus jednog termina.
 *
 * CONFIRMED -> COMPLETED   (termin je odrzan)
 * CONFIRMED -> CANCELLED   (klijent ili salon otkazao)
 *
 * Vazno: samo CONFIRMED i COMPLETED termini zauzimaju vreme zaposlenog.
 * Otkazan termin oslobadja slot, pa se preko njega moze zakazati novi.
 */
public enum BookingStatus {
    CONFIRMED,
    CANCELLED,
    COMPLETED
}
