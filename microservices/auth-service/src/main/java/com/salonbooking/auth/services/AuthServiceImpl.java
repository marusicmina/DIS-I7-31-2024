package com.salonbooking.auth.services;

import com.salonbooking.api.auth.*;
import com.salonbooking.auth.persistence.UserEntity;
import com.salonbooking.auth.persistence.UserRepository;
import com.salonbooking.util.exceptions.AuthenticationException;
import com.salonbooking.util.exceptions.ConflictException;
import com.salonbooking.util.exceptions.InvalidInputException;
import com.salonbooking.util.exceptions.NotFoundException;
import com.salonbooking.util.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AuthServiceImpl implements AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository repository, UserMapper mapper,
                            PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    public UserSummary register(@RequestBody RegisterRequest body) {
        validateRegisterRequest(body);

        if (repository.existsByEmailIgnoreCase(body.getEmail())) {
            throw new ConflictException("Nalog sa email adresom '" + body.getEmail() + "' vec postoji");
        }

        UserEntity entity = new UserEntity(
                body.getFirstName(),
                body.getLastName(),
                body.getEmail(),
                passwordEncoder.encode(body.getPassword()),
                body.getRole() != null ? body.getRole() : Role.CUSTOMER
        );

        UserEntity saved = repository.save(entity);
        LOG.debug("register: kreiran korisnik sa id={}, uloga={}", saved.getId(), saved.getRole());
        return mapper.entityToSummary(saved);
    }

    @Override
    public AuthResponse login(@RequestBody LoginRequest body) {
        if (isBlank(body.getEmail()) || isBlank(body.getPassword())) {
            throw new InvalidInputException("Email i lozinka su obavezni");
        }

        UserEntity entity = repository.findByEmailIgnoreCase(body.getEmail())
                .orElseThrow(() -> new AuthenticationException("Pogresan email ili lozinka"));

        if (!passwordEncoder.matches(body.getPassword(), entity.getPasswordHash())) {
            throw new AuthenticationException("Pogresan email ili lozinka");
        }

        String token = jwtUtil.generateToken(entity.getId(), entity.getEmail(), entity.getRole().name());
        LOG.debug("login: uspesna prijava za userId={}", entity.getId());
        return new AuthResponse(token, jwtUtil.getExpirationMs(), mapper.entityToSummary(entity));
    }

    @Override
    public List<UserSummary> getUsers() {
        return repository.findAll().stream()
                .map(mapper::entityToSummary)
                .toList();
    }

    @Override
    public UserSummary getUser(long userId) {
        UserEntity entity = findOrThrow(userId);
        return mapper.entityToSummary(entity);
    }

    @Override
    public UserSummary updateUser(long userId, @RequestBody UpdateUserRequest body) {
        UserEntity entity = findOrThrow(userId);

        if (isBlank(body.getFirstName()) || isBlank(body.getLastName()) || isBlank(body.getEmail())) {
            throw new InvalidInputException("Ime, prezime i email su obavezni");
        }

        if (!entity.getEmail().equalsIgnoreCase(body.getEmail())
                && repository.existsByEmailIgnoreCase(body.getEmail())) {
            throw new ConflictException("Nalog sa email adresom '" + body.getEmail() + "' vec postoji");
        }

        entity.setFirstName(body.getFirstName());
        entity.setLastName(body.getLastName());
        entity.setEmail(body.getEmail());

        return mapper.entityToSummary(repository.save(entity));
    }

    @Override
    public void deleteUser(long userId) {
        repository.findById(userId).ifPresent(repository::delete);
        LOG.debug("deleteUser: obrisan (ako je postojao) korisnik sa id={}", userId);
    }

    private UserEntity findOrThrow(long userId) {
        if (userId < 1) {
            throw new InvalidInputException("Nevalidan userId: " + userId);
        }
        return repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Korisnik nije pronadjen za userId: " + userId));
    }

    private void validateRegisterRequest(RegisterRequest body) {
        if (isBlank(body.getFirstName()) || isBlank(body.getLastName())) {
            throw new InvalidInputException("Ime i prezime su obavezni");
        }
        if (isBlank(body.getEmail()) || !body.getEmail().contains("@")) {
            throw new InvalidInputException("Email nije validan");
        }
        if (isBlank(body.getPassword()) || body.getPassword().length() < 6) {
            throw new InvalidInputException("Lozinka mora imati bar 6 karaktera");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
