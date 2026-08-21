package com.salonbooking.staff.services;

import com.salonbooking.api.staff.AvailabilityResponse;
import com.salonbooking.api.staff.Staff;
import com.salonbooking.api.staff.StaffService;
import com.salonbooking.api.staff.WorkingHours;
import com.salonbooking.staff.persistence.StaffEntity;
import com.salonbooking.staff.persistence.StaffRepository;
import com.salonbooking.staff.persistence.WorkingHoursEntity;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
public class StaffServiceImpl implements StaffService {

    private static final Logger LOG = LoggerFactory.getLogger(StaffServiceImpl.class);

    private final StaffRepository repository;
    private final StaffMapper mapper;

    @Value("${server.port}")
    private String port;

    public StaffServiceImpl(StaffRepository repository, StaffMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Staff> getStaffMembers(Long salonId) {
        List<StaffEntity> entities = (salonId == null)
                ? repository.findAll()
                : repository.findBySalonId(salonId);

        List<Staff> list = entities.stream()
                .map(e -> mapper.entityToApi(e, serviceAddress()))
                .toList();

        LOG.debug("getStaffMembers: vracam {} zaposlenih (filter salonId={})", list.size(), salonId);
        return list;
    }

    @Override
    public Staff getStaffMember(long staffId) {
        return mapper.entityToApi(findOrThrow(staffId), serviceAddress());
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Staff createStaffMember(@RequestBody Staff body) {
        validate(body);

        StaffEntity saved = repository.save(mapper.apiToEntity(body));
        LOG.debug("createStaffMember: kreiran zaposleni {} {} sa id={}",
                saved.getFirstName(), saved.getLastName(), saved.getId());
        return mapper.entityToApi(saved, serviceAddress());
    }

    @Override
    @Transactional
    public Staff updateStaffMember(long staffId, @RequestBody Staff body) {
        StaffEntity entity = findOrThrow(staffId);
        validate(body);

        entity.setFirstName(body.getFirstName());
        entity.setLastName(body.getLastName());
        entity.setSpecialization(body.getSpecialization());
        entity.setUserId(body.getUserId());
        entity.setActive(body.isActive());
        mapper.applyWorkingHours(entity, body.getWorkingHours());

        return mapper.entityToApi(repository.save(entity), serviceAddress());
    }

    @Override
    @Transactional
    public void deleteStaffMember(long staffId) {
        repository.findById(staffId).ifPresent(repository::delete);
        LOG.debug("deleteStaffMember: obrisan (ako je postojao) zaposleni sa id={}", staffId);
    }

    /**
     * Provera da li zaposleni RADI u zadatom intervalu.
     *
     * Namerno ne zna nista o vec zakazanim terminima - ti podaci pripadaju
     * booking-service-u, koji preklapanje proverava u svojoj bazi. Ovaj servis
     * odgovara iskljucivo na osnovu podataka koje poseduje: status zaposlenog
     * i njegovo radno vreme.
     */
    @Override
    public AvailabilityResponse checkAvailability(long staffId, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new InvalidInputException("Parametri 'start' i 'end' su obavezni");
        }
        if (!end.isAfter(start)) {
            throw new InvalidInputException("Kraj termina mora biti posle pocetka");
        }

        StaffEntity staff = findOrThrow(staffId);

        if (!staff.isActive()) {
            return unavailable(staffId, "Zaposleni nije aktivan");
        }

        // Termin koji prelazi ponoc ne moze da se uporedi sa radnim vremenom
        // jednog dana, a u salonu lepote ionako nema smisla.
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            return unavailable(staffId, "Termin ne moze da prelazi u naredni dan");
        }

        Optional<WorkingHoursEntity> maybeHours = staff.workingHoursFor(start.getDayOfWeek());
        if (maybeHours.isEmpty()) {
            return unavailable(staffId, "Zaposleni ne radi u danu: " + start.getDayOfWeek());
        }

        WorkingHoursEntity hours = maybeHours.get();
        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();

        // Granice su ukljucive: ako radi od 09:00 do 17:00, termin 09:00-09:45
        // je ispravan, kao i 16:15-17:00. Zato !isBefore / !isAfter, a ne
        // isAfter / isBefore - inace bi termin tacno na granici bio odbijen.
        boolean insideWorkingHours = !startTime.isBefore(hours.getStartTime())
                && !endTime.isAfter(hours.getEndTime());

        if (!insideWorkingHours) {
            return unavailable(staffId, String.format(
                    "Trazeni termin je izvan radnog vremena (%s radi %s-%s)",
                    start.getDayOfWeek(), hours.getStartTime(), hours.getEndTime()));
        }

        LOG.debug("checkAvailability: zaposleni id={} je raspolozivi {} - {}", staffId, start, end);
        return new AvailabilityResponse(staffId, true, null);
    }

    private AvailabilityResponse unavailable(long staffId, String reason) {
        LOG.debug("checkAvailability: zaposleni id={} nije raspoloziv - {}", staffId, reason);
        return new AvailabilityResponse(staffId, false, reason);
    }

    private StaffEntity findOrThrow(long staffId) {
        if (staffId < 1) {
            throw new InvalidInputException("Nevalidan staffId: " + staffId);
        }
        return repository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Zaposleni nije pronadjen za staffId: " + staffId));
    }

    private void validate(Staff body) {
        if (body.getFirstName() == null || body.getFirstName().isBlank()) {
            throw new InvalidInputException("Ime zaposlenog je obavezno");
        }
        if (body.getLastName() == null || body.getLastName().isBlank()) {
            throw new InvalidInputException("Prezime zaposlenog je obavezno");
        }
        if (body.getSalonId() < 1) {
            throw new InvalidInputException("Nevalidan salonId: " + body.getSalonId());
        }
        if (body.getWorkingHours() != null) {
            for (WorkingHours wh : body.getWorkingHours()) {
                if (wh.getDayOfWeek() == null || wh.getStartTime() == null || wh.getEndTime() == null) {
                    throw new InvalidInputException("Radno vreme mora imati dan, pocetak i kraj");
                }
                if (!wh.getEndTime().isAfter(wh.getStartTime())) {
                    throw new InvalidInputException(
                            "Kraj radnog vremena mora biti posle pocetka (dan: " + wh.getDayOfWeek() + ")");
                }
            }
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
