package com.smartshop.api.config;

import com.smartshop.api.entity.Client;
import com.smartshop.api.entity.User;
import com.smartshop.api.enums.CustomerTier;
import com.smartshop.api.enums.UserRole;
import com.smartshop.api.repository.ClientRepository;
import com.smartshop.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Initializing test data...");

            // Create Admin User
            User adminUser = User.builder()
                    .email("mohamed@admin.com")
                    .password("123456789")
                    .role(UserRole.ADMIN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(adminUser);
            log.info("Created admin user: mohamed@admin.com");

            // Create Client User 1
            User clientUser1 = User.builder()
                    .email("mohamed@client.com")
                    .password("123456789")
                    .role(UserRole.CLIENT)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(clientUser1);

            Client client1 = Client.builder()
                    .user(clientUser1)
                    .nom("Mohamed Client 1")
                    .email("mohamed@client.com")
                    .telephone("+212600000001")
                    .adresse("123 Client Street, City")
                    .tier(CustomerTier.BASIC)
                    .totalOrders(0)
                    .totalSpent(0.0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            clientRepository.save(client1);
            log.info("Created client user: mohamed@client.com");

            // Create Client User 2
            User clientUser2 = User.builder()
                    .email("mohamed2@client.com")
                    .password("123456789")
                    .role(UserRole.CLIENT)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(clientUser2);

            Client client2 = Client.builder()
                    .user(clientUser2)
                    .nom("Mohamed Client 2")
                    .email("mohamed2@client.com")
                    .telephone("+212600000002")
                    .adresse("456 Client Avenue, City")
                    .tier(CustomerTier.BASIC)
                    .totalOrders(0)
                    .totalSpent(0.0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            clientRepository.save(client2);
            log.info("Created client user: mohamed2@client.com");

            // Create Client User 3
            User clientUser3 = User.builder()
                    .email("mohamed3@client.com")
                    .password("123456789")
                    .role(UserRole.CLIENT)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(clientUser3);

            Client client3 = Client.builder()
                    .user(clientUser3)
                    .nom("Mohamed Client 3")
                    .email("mohamed3@client.com")
                    .telephone("+212600000003")
                    .adresse("789 Client Boulevard, City")
                    .tier(CustomerTier.BASIC)
                    .totalOrders(0)
                    .totalSpent(0.0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            clientRepository.save(client3);
            log.info("Created client user: mohamed3@client.com");

            log.info("Test data initialization completed successfully!");
            log.info("=".repeat(60));
            log.info("Test Users Created:");
            log.info("Admin: mohamed@admin.com / 123456789");
            log.info("Client 1: mohamed@client.com / 123456789");
            log.info("Client 2: mohamed2@client.com / 123456789");
            log.info("Client 3: mohamed3@client.com / 123456789");
            log.info("=".repeat(60));
        } else {
            log.info("Database already contains data, skipping initialization");
        }
    }
}

