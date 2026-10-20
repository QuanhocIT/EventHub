package com.eventhub.services;

import com.eventhub.dto.request.LoginRequest;
import com.eventhub.dto.request.RegisterRequest;
import com.eventhub.dto.response.AuthResponse;
import com.eventhub.entities.User;
import com.eventhub.enums.UserRole;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.repositories.UserRepository;
import com.eventhub.security.CustomUserDetailsService;
import com.eventhub.security.JwtService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(UserRole.ROLE_CUSTOMER)
                .enabled(true)
                .build();

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Authenticating user: {}", request.getUsernameOrEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new BadRequestException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    /**
     * Seeds initial demo users for all 4 roles if no users exist.
     */
    @PostConstruct
    @Transactional
    public void seedInitialUsers() {
        if (userRepository.count() == 0) {
            log.info("Seeding default demo users for 4 roles: admin, organizer, staff, customer...");

            User admin = User.builder()
                    .username("admin")
                    .email("admin@eventhub.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("EventHub Administrator")
                    .role(UserRole.ROLE_ADMIN)
                    .enabled(true)
                    .build();

            User organizer = User.builder()
                    .username("organizer")
                    .email("organizer@eventhub.com")
                    .password(passwordEncoder.encode("organizer123"))
                    .fullName("Event Organizer")
                    .role(UserRole.ROLE_ORGANIZER)
                    .enabled(true)
                    .build();

            User staff = User.builder()
                    .username("staff")
                    .email("staff@eventhub.com")
                    .password(passwordEncoder.encode("staff123"))
                    .fullName("Event Check-in Staff")
                    .role(UserRole.ROLE_STAFF)
                    .enabled(true)
                    .build();

            User customer = User.builder()
                    .username("customer")
                    .email("customer@eventhub.com")
                    .password(passwordEncoder.encode("customer123"))
                    .fullName("Regular Customer")
                    .role(UserRole.ROLE_CUSTOMER)
                    .enabled(true)
                    .build();

            userRepository.save(admin);
            userRepository.save(organizer);
            userRepository.save(staff);
            userRepository.save(customer);

            log.info("Default users seeded: admin, organizer, staff, customer (passwords: <username>123)");
        }
    }
}
