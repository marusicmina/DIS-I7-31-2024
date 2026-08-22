package com.salonbooking.booking.services;

import com.salonbooking.api.booking.Booking;
import com.salonbooking.api.booking.BookingService;
import com.salonbooking.api.booking.BookingStatus;
import com.salonbooking.api.booking.CreateBookingRequest;
import com.salonbooking.api.catalog.ServiceOffering;
import com.salonbooking.api.event.BookingEvent;
import com.salonbooking.api.event.BookingEventType;
import com.salonbooking.api.salon.Salon;
import com.salonbooking.api.staff.AvailabilityResponse;
import com.salonbooking.booking.integration.BookingIntegration;
import com.salonbooking.booking.messaging.BookingEventPublisher;
import com.salonbooking.booking.persistence.BookingEntity;
import com.salonbooking.booking.persistence.BookingRepository;
import com.salonbooking.util.exceptions.ConflictException;
import com.salonbooking.util.exceptions.InvalidInputException;
import com.salonbooking.util.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.List;


@RestController
public class BookingServiceImpl implements BookingService {

    private static final Logger LOG = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository repository;
    private final BookingMapper mapper;
    private final BookingIntegration integration;
    private final BookingEventPublisher eventPublisher;

    @Value("${server.port}")
    private String port;

    public BookingServiceImpl(BookingRepository repository, BookingMapper mapper,
                               BookingIntegration integration, BookingEventPublisher eventPublisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.integration = integration;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Booking createBooking(@RequestBody CreateBookingRequest body) {
        validateRequest(body);

        Salon salon = integration.getSalon(body.getSalonId());

        ServiceOffering service = integration.getService(body.getServiceId());
        if (service.getSalonId() != body.getSalonId()) {
            throw new InvalidInputException("Usluga " + body.getServiceId()
                    + " ne pripada salonu " + body.getSalonId());
        }
        if (!service.isActive()) {
            throw new InvalidInputException("Usluga '" + service.getName() + "' trenutno nije u ponudi");
        }

        LocalDateTime start = body.getStartTime();
        LocalDateTime end = start.plusMinutes(service.getDurationMinutes());

        AvailabilityResponse availability =
                integration.checkStaffAvailability(body.getStaffId(), start, end);
        if (!availability.isAvailable()) {
            throw new InvalidInputException("Zaposleni nije raspoloziv: " + availability.getReason());
        }

        boolean overlaps = repository.existsOverlappingBooking(
                body.getStaffId(), start, end, BookingStatus.CANCELLED);
        if (overlaps) {
            throw new ConflictException(
                    "Zaposleni vec ima zakazan termin koji se preklapa sa trazenim vremenom");
        }

        BookingEntity entity = new BookingEntity(
                body.getClientId(),
                body.getSalonId(),
                body.getStaffId(),
                body.getServiceId(),
                service.getName(),
                service.getPrice(),
                start,
                end,
                BookingStatus.CONFIRMED,
                body.getNote(),
                LocalDateTime.now()
        );

        BookingEntity saved = repository.save(entity);
        LOG.info("Zakazan termin id={} u salonu '{}' kod zaposlenog {} za {} ({})",
                saved.getId(), salon.getName(), saved.getStaffId(), start, service.getName());

        publishEvent(BookingEventType.BOOKING_CREATED, saved);
        return mapper.entityToApi(saved, serviceAddress());
    }

    @Override
    public List<Booking> getBookings(Long clientId, Long staffId, Long salonId) {
        List<BookingEntity> entities;
        if (clientId != null) {
            entities = repository.findByClientId(clientId);
        } else if (staffId != null) {
            entities = repository.findByStaffId(staffId);
        } else if (salonId != null) {
            entities = repository.findBySalonId(salonId);
        } else {
            entities = repository.findAll();
        }

        LOG.debug("getBookings: vracam {} termina (clientId={}, staffId={}, salonId={})",
                entities.size(), clientId, staffId, salonId);

        return entities.stream()
                .map(e -> mapper.entityToApi(e, serviceAddress()))
                .toList();
    }

    @Override
    public Booking getBooking(long bookingId) {
        return mapper.entityToApi(findOrThrow(bookingId), serviceAddress());
    }

    @Override
    @Transactional
    public Booking cancelBooking(long bookingId) {
        BookingEntity entity = findOrThrow(bookingId);

        if (entity.getStatus() == BookingStatus.COMPLETED) {
            throw new ConflictException("Odrzan termin ne moze da se otkaze");
        }
        if (entity.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Termin je vec otkazan");
        }

        entity.setStatus(BookingStatus.CANCELLED);
        BookingEntity saved = repository.save(entity);
        LOG.info("Otkazan termin id={}", bookingId);

        publishEvent(BookingEventType.BOOKING_CANCELLED, saved);
        return mapper.entityToApi(saved, serviceAddress());
    }

    @Override
    @Transactional
    public Booking completeBooking(long bookingId) {
        BookingEntity entity = findOrThrow(bookingId);

        if (entity.getStatus() != BookingStatus.CONFIRMED) {
            throw new ConflictException(
                    "Samo potvrdjen termin moze da se oznaci kao odrzan (trenutni status: "
                            + entity.getStatus() + ")");
        }

        entity.setStatus(BookingStatus.COMPLETED);
        BookingEntity saved = repository.save(entity);
        LOG.info("Termin id={} oznacen kao odrzan", bookingId);

        publishEvent(BookingEventType.BOOKING_COMPLETED, saved);
        return mapper.entityToApi(saved, serviceAddress());
    }

   
    private void publishEvent(BookingEventType type, BookingEntity entity) {
        BookingEvent event = new BookingEvent(
                type,
                entity.getId(),
                entity.getClientId(),
                entity.getSalonId(),
                entity.getStaffId(),
                entity.getServiceId(),
                entity.getServiceName(),
                entity.getPrice(),
                entity.getStartTime(),
                entity.getEndTime(),
                LocalDateTime.now()
        );
        eventPublisher.publish(event);
    }

    private BookingEntity findOrThrow(long bookingId) {
        if (bookingId < 1) {
            throw new InvalidInputException("Nevalidan bookingId: " + bookingId);
        }
        return repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Termin nije pronadjen za bookingId: " + bookingId));
    }

    private void validateRequest(CreateBookingRequest body) {
        if (body.getStartTime() == null) {
            throw new InvalidInputException("Vreme pocetka termina je obavezno");
        }
        if (body.getClientId() < 1) {
            throw new InvalidInputException("Nevalidan clientId: " + body.getClientId());
        }
        if (body.getSalonId() < 1) {
            throw new InvalidInputException("Nevalidan salonId: " + body.getSalonId());
        }
        if (body.getStaffId() < 1) {
            throw new InvalidInputException("Nevalidan staffId: " + body.getStaffId());
        }
        if (body.getServiceId() < 1) {
            throw new InvalidInputException("Nevalidan serviceId: " + body.getServiceId());
        }
        if (body.getStartTime().isBefore(LocalDateTime.now())) {
            throw new InvalidInputException("Nije moguce zakazati termin u proslosti");
        }
    }

    private String serviceAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress() + ":" + port;
        } catch (UnknownHostException e) {
            return "unknown:" + port;
        }
    }
}
