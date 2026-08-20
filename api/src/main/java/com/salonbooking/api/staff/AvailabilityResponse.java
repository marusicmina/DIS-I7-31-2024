package com.salonbooking.api.staff;

/**
 * Odgovor na pitanje "da li je zaposleni raspoloziv u trazenom terminu".
 *
 * Vazno za razumevanje granica izmedju servisa: staff-service odgovara SAMO na
 * osnovu radnog vremena i statusa zaposlenog. On ne zna nista o vec zakazanim
 * terminima - te podatke drzi booking-service u svojoj bazi i sam proverava
 * preklapanje. Tako svaki servis odlucuje iskljucivo o podacima koje poseduje.
 */
public class AvailabilityResponse {

    private long staffId;
    private boolean available;

    /** Objasnjenje kada available = false, npr. "Zaposleni ne radi nedeljom". */
    private String reason;

    public AvailabilityResponse() {
    }

    public AvailabilityResponse(long staffId, boolean available, String reason) {
        this.staffId = staffId;
        this.available = available;
        this.reason = reason;
    }

    public long getStaffId() {
        return staffId;
    }

    public void setStaffId(long staffId) {
        this.staffId = staffId;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
