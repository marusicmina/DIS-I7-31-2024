package com.salonbooking.catalog.services;

import com.salonbooking.api.catalog.CatalogService;
import com.salonbooking.api.catalog.ServiceOffering;
import com.salonbooking.catalog.persistence.ServiceOfferingEntity;
import com.salonbooking.catalog.persistence.ServiceOfferingRepository;
import com.salonbooking.util.exceptions.InvalidInputException;
import com.salonbooking.util.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@RestController
public class CatalogServiceImpl implements CatalogService {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogServiceImpl.class);

    private final ServiceOfferingRepository repository;
    private final ServiceOfferingMapper mapper;

    @Value("${server.port}")
    private String port;

    public CatalogServiceImpl(ServiceOfferingRepository repository, ServiceOfferingMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<ServiceOffering> getServices(Long salonId) {
        List<ServiceOfferingEntity> entities = (salonId == null)
                ? repository.findAll()
                : repository.findBySalonId(salonId);

        List<ServiceOffering> list = entities.stream()
                .map(e -> mapper.entityToApi(e, serviceAddress()))
                .toList();

        LOG.debug("getServices: vracam {} usluga (filter salonId={})", list.size(), salonId);
        return list;
    }

    @Override
    public ServiceOffering getService(long serviceId) {
        ServiceOfferingEntity entity = findOrThrow(serviceId);
        return mapper.entityToApi(entity, serviceAddress());
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceOffering createService(@RequestBody ServiceOffering body) {
        validate(body);

        ServiceOfferingEntity saved = repository.save(mapper.apiToEntity(body));
        LOG.debug("createService: kreirana usluga '{}' sa id={}", saved.getName(), saved.getId());
        return mapper.entityToApi(saved, serviceAddress());
    }

    @Override
    public ServiceOffering updateService(long serviceId, @RequestBody ServiceOffering body) {
        ServiceOfferingEntity entity = findOrThrow(serviceId);
        validate(body);

        entity.setName(body.getName());
        entity.setDescription(body.getDescription());
        entity.setDurationMinutes(body.getDurationMinutes());
        entity.setPrice(body.getPrice());
        entity.setActive(body.isActive());

        return mapper.entityToApi(repository.save(entity), serviceAddress());
    }

    @Override
    public void deleteService(long serviceId) {
        repository.findById(serviceId).ifPresent(repository::delete);
        LOG.debug("deleteService: obrisana (ako je postojala) usluga sa id={}", serviceId);
    }

    private ServiceOfferingEntity findOrThrow(long serviceId) {
        if (serviceId < 1) {
            throw new InvalidInputException("Nevalidan serviceId: " + serviceId);
        }
        return repository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Usluga nije pronadjena za serviceId: " + serviceId));
    }

    private void validate(ServiceOffering body) {
        if (body.getName() == null || body.getName().isBlank()) {
            throw new InvalidInputException("Naziv usluge je obavezan");
        }
        if (body.getSalonId() < 1) {
            throw new InvalidInputException("Nevalidan salonId: " + body.getSalonId());
        }
        if (body.getDurationMinutes() <= 0) {
            throw new InvalidInputException("Trajanje usluge mora biti vece od 0 minuta");
        }
        if (body.getPrice() == null || body.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInputException("Cena ne sme biti negativna");
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
