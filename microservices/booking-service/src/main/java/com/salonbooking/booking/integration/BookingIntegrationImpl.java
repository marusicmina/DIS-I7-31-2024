package com.salonbooking.booking.integration;

import com.salonbooking.api.catalog.ServiceOffering;
import com.salonbooking.api.salon.Salon;
import com.salonbooking.api.staff.AvailabilityResponse;
import com.salonbooking.util.exceptions.NotFoundException;
import com.salonbooking.util.exceptions.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST pozivi ka ostalim mikroservisima.
 *
 * Adrese su imena servisa iz Eureke ("http://salon-service/..."), a ne portovi.
 * RestTemplate je oznacen sa @LoadBalanced (vidi RestTemplateConfig), pa Spring
 * Cloud pre slanja zahteva zameni ime stvarnom adresom instance iz registra.
 *
 * Svaki poziv je zasticen sa @Retry i @CircuitBreaker:
 *  - Retry ponavlja poziv ako je greska prolazna (mrezni prekid, servis se bas dize)
 *  - CircuitBreaker posle previse uzastopnih gresaka "otvara kolo" i neko vreme
 *    odmah odbija pozive, umesto da svaki zahtev ceka tajmaut. Time se pad jednog
 *    servisa ne pretvara u zagusenje celog sistema.
 * Redosled je bitan: retry je unutar circuit breaker-a, pa se ponovljeni pokusaji
 * broje kao jedan dogadjaj prema kolu.
 */
@Component
public class BookingIntegrationImpl implements BookingIntegration {

    private static final Logger LOG = LoggerFactory.getLogger(BookingIntegrationImpl.class);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final RestTemplate restTemplate;

    public BookingIntegrationImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @Retry(name = "salonService")
    @CircuitBreaker(name = "salonService", fallbackMethod = "salonFallback")
    public Salon getSalon(long salonId) {
        String url = "http://salon-service/salons/" + salonId;
        LOG.debug("Pozivam salon-service: {}", url);
        return restTemplate.getForObject(url, Salon.class);
    }

    @Override
    @Retry(name = "catalogService")
    @CircuitBreaker(name = "catalogService", fallbackMethod = "catalogFallback")
    public ServiceOffering getService(long serviceId) {
        String url = "http://catalog-service/catalog/services/" + serviceId;
        LOG.debug("Pozivam catalog-service: {}", url);
        return restTemplate.getForObject(url, ServiceOffering.class);
    }

    @Override
    @Retry(name = "staffService")
    @CircuitBreaker(name = "staffService", fallbackMethod = "staffFallback")
    public AvailabilityResponse checkStaffAvailability(long staffId, LocalDateTime start, LocalDateTime end) {
        String url = UriComponentsBuilder
                .fromUriString("http://staff-service/staff/" + staffId + "/availability")
                .queryParam("start", start.format(ISO))
                .queryParam("end", end.format(ISO))
                .toUriString();
        LOG.debug("Pozivam staff-service: {}", url);
        return restTemplate.getForObject(url, AvailabilityResponse.class);
    }

    // --- fallback metode ---
    //
    // Resilience4j ih poziva kada je kolo otvoreno ili kada poziv baci gresku.
    // Moraju da imaju isti potpis kao originalna metoda plus Throwable na kraju.
    //
    // 404 od drugog servisa NIJE kvar - to je legitiman odgovor "ne postoji",
    // pa ga prevodimo u NotFoundException. Sve ostalo tretiramo kao nedostupnost.

    private Salon salonFallback(long salonId, Throwable t) {
        if (isNotFound(t)) {
            throw new NotFoundException("Salon nije pronadjen za salonId: " + salonId);
        }
        LOG.warn("salon-service nedostupan (salonId={}): {}", salonId, t.toString());
        throw new ServiceUnavailableException(
                "Trenutno nije moguce proveriti podatke o salonu. Pokusajte ponovo za koji trenutak.");
    }

    private ServiceOffering catalogFallback(long serviceId, Throwable t) {
        if (isNotFound(t)) {
            throw new NotFoundException("Usluga nije pronadjena za serviceId: " + serviceId);
        }
        LOG.warn("catalog-service nedostupan (serviceId={}): {}", serviceId, t.toString());
        throw new ServiceUnavailableException(
                "Trenutno nije moguce proveriti podatke o usluzi. Pokusajte ponovo za koji trenutak.");
    }

    private AvailabilityResponse staffFallback(long staffId, LocalDateTime start, LocalDateTime end, Throwable t) {
        if (isNotFound(t)) {
            throw new NotFoundException("Zaposleni nije pronadjen za staffId: " + staffId);
        }
        LOG.warn("staff-service nedostupan (staffId={}): {}", staffId, t.toString());
        // Ovde NE vracamo "slobodan je" - kad ne mozemo da proverimo dostupnost,
        // sigurnije je odbiti zakazivanje nego rizikovati dupli termin.
        throw new ServiceUnavailableException(
                "Trenutno nije moguce proveriti raspolozivost zaposlenog. Pokusajte ponovo za koji trenutak.");
    }

    private boolean isNotFound(Throwable t) {
        if (t instanceof HttpClientErrorException ex) {
            HttpStatusCode status = ex.getStatusCode();
            return status.value() == 404;
        }
        if (t instanceof RestClientException && t.getCause() != null) {
            return isNotFound(t.getCause());
        }
        return false;
    }
}
