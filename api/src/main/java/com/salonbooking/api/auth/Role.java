package com.salonbooking.api.auth;

/**
 * Uloge korisnika u sistemu. STAFF (zaposleni u salonu) se vodi odvojeno u
 * staff-service-u (ima svoj domenski model - specijalizacija, raspored...),
 * ovde su samo uloge relevantne za autentifikaciju/autorizaciju naloga.
 */
public enum Role {
    CUSTOMER,
    SALON_OWNER,
    ADMIN
}
