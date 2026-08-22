package com.salonbooking.api.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class BookingEvent {

    private BookingEventType type;

    private long bookingId;
    private long clientId;
    private long salonId;
    private long staffId;
    private long serviceId;
    private String serviceName;
    private BigDecimal price;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventTime;

    public BookingEvent() {
    }

    public BookingEvent(BookingEventType type, long bookingId, long clientId, long salonId,
                         long staffId, long serviceId, String serviceName, BigDecimal price,
                         LocalDateTime startTime, LocalDateTime endTime, LocalDateTime eventTime) {
        this.type = type;
        this.bookingId = bookingId;
        this.clientId = clientId;
        this.salonId = salonId;
        this.staffId = staffId;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.price = price;
        this.startTime = startTime;
        this.endTime = endTime;
        this.eventTime = eventTime;
    }

    public BookingEventType getType() {
        return type;
    }

    public void setType(BookingEventType type) {
        this.type = type;
    }

    public long getBookingId() {
        return bookingId;
    }

    public void setBookingId(long bookingId) {
        this.bookingId = bookingId;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }
}
