package com.salonbooking.api.staff;


public class AvailabilityResponse {

    private long staffId;
    private boolean available;

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
