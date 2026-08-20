# Salon Booking System

Mikroservisni sistem za zakazivanje termina u salonima lepote. Projekat za
predmet **Distribuirani informacioni sistemi**, rađen po uzoru na knjigu
*"Hands-On Microservices with Spring Boot and Spring Cloud"*.

Puna dokumentacija (poslovna logika, arhitektura, dijagram, uputstvo za
pipeline) nalazi se u [`DOCUMENTATION.md`](./DOCUMENTATION.md).

## Brzi start

```bash
# Build i testovi
./gradlew build

# Pokretanje dev okruženja (trenutno: salon-service + njegova baza)
docker compose up --build
```

Kada se `salon-service` pokrene (samostalno ili kroz Docker Compose), API je
dostupan na `http://localhost:8081`, npr.:

```bash
curl http://localhost:8081/salons
```

## Status projekta

Projekat se gradi inkrementalno. Trenutni napredak i plan preostalih delova
opisani su u sekciji **"Plan preostalih delova"** u `DOCUMENTATION.md`.

## Struktura repozitorijuma

```
api/                    - zajednicki API ugovori (interfejsi + DTO) za sve mikroservise
util/                   - zajednicke klase (izuzeci, error handling)
microservices/
  salon-service/        - upravljanje salonima (implementirano)
  ...                   - ostali servisi dolaze u narednim delovima
spring-cloud/           - eureka-server, config-server, gateway (dolazi kasnije)
docs/                   - dodatna dokumentacija/dijagrami
config-repo/            - centralizovana konfiguracija za Spring Cloud Config
.github/workflows/      - CI/CD pipeline (GitHub Actions)
docker-compose.yml      - dev okruzenje
```
