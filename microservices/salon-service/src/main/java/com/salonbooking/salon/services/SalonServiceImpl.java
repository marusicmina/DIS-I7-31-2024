package com.salonbooking.salon.services;

import com.salonbooking.api.salon.Salon;
import com.salonbooking.api.salon.SalonService;
import com.salonbooking.salon.persistence.SalonEntity;
import com.salonbooking.salon.persistence.SalonRepository;
import com.salonbooking.util.exceptions.InvalidInputException;
import com.salonbooking.util.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@RestController
public class SalonServiceImpl implements SalonService {

    private static final Logger LOG = LoggerFactory.getLogger(SalonServiceImpl.class);

    private final SalonRepository repository;
    private final SalonMapper mapper;

    @Value("${server.port}")
    private String port;

    public SalonServiceImpl(SalonRepository repository, SalonMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Salon> getSalons(String city) {
        List<SalonEntity> entities = (city == null || city.isBlank())
                ? repository.findAll()
                : repository.findByCityIgnoreCase(city);

        List<Salon> list = entities.stream()
                .map(e -> mapper.entityToApi(e, serviceAddress()))
                .toList();

        LOG.debug("getSalons: vracam {} salona (filter grad={})", list.size(), city);
        return list;
    }

    @Override
    public Salon getSalon(long salonId) {
        if (salonId < 1) {
            throw new InvalidInputException("Nevalidan salonId: " + salonId);
        }

        SalonEntity entity = repository.findById(salonId)
                .orElseThrow(() -> new NotFoundException("Salon nije pronadjen za salonId: " + salonId));

        Salon response = mapper.entityToApi(entity, serviceAddress());
        LOG.debug("getSalon: pronadjen salon sa id={}", response.getSalonId());
        return response;
    }

    @Override
    public Salon createSalon(Salon body) {
        try {
            SalonEntity entity = mapper.apiToEntity(body);
            SalonEntity saved = repository.save(entity);
            LOG.debug("createSalon: kreiran salon sa id={}", saved.getId());
            return mapper.entityToApi(saved, serviceAddress());
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Nevalidni podaci za salon: " + body.getName());
        }
    }

    @Override
    public Salon updateSalon(long salonId, Salon body) {
        SalonEntity entity = repository.findById(salonId)
                .orElseThrow(() -> new NotFoundException("Salon nije pronadjen za salonId: " + salonId));

        entity.setName(body.getName());
        entity.setAddress(body.getAddress());
        entity.setCity(body.getCity());
        entity.setPhoneNumber(body.getPhoneNumber());
        entity.setDescription(body.getDescription());
        entity.setOpeningTime(body.getOpeningTime());
        entity.setClosingTime(body.getClosingTime());

        SalonEntity saved = repository.save(entity);
        return mapper.entityToApi(saved, serviceAddress());
    }

    @Override
    public void deleteSalon(long salonId) {
        repository.findById(salonId).ifPresent(repository::delete);
        LOG.debug("deleteSalon: obrisan (ako je postojao) salon sa id={}", salonId);
    }

    private String serviceAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress() + ":" + port;
        } catch (UnknownHostException e) {
            return "unknown:" + port;
        }
    }
}
