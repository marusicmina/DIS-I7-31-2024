package com.salonbooking.api.booking;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;


public class CreateBookingRequest {

    private long clientId;
    private long salonId;
    private long staffId;
    private long serviceId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    private String note;

    public CreateBookingRequest() {
    }

    public CreateBookingRequest(long clientId, long salonId, long staffId, long serviceId,
                                 LocalDateTime startTime, String note) {
        this.clientId = clientId;
        this.salonId = salonId;
        this.staffId = staffId;
        this.serviceId = serviceId;
        this.startTime = startTime;
        this.note = note;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public long getSalonId() {
        return salonId;
    }

    public void setSalonId(long salonId) {
        this.salonId = salonId;
    }

    public long getStaffId() {
        return staffId;
    }

    public void setStaffId(long staffId) {
        this.staffId = staffId;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
