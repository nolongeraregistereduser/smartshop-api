package com.smartshop.api.service.Impl;

import com.smartshop.api.dto.request.ClientRequestDTO;
import com.smartshop.api.dto.response.ClientResponseDTO;
import com.smartshop.api.entity.Client;
import com.smartshop.api.entity.User;
import com.smartshop.api.enums.CustomerTier;
import com.smartshop.api.enums.UserRole;
import com.smartshop.api.exception.DuplicateResourceException;
import com.smartshop.api.exception.ResourceNotFoundException;
import com.smartshop.api.repository.ClientRepository;
import com.smartshop.api.repository.UserRepository;
import com.smartshop.api.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ClientResponseDTO createClient(ClientRequestDTO clientRequestDTO) {
        // Check if email already exists
        if (clientRepository.existsByEmail(clientRequestDTO.getEmail())) {
            throw new DuplicateResourceException("Un client avec cet email existe déjà");
        }

        // Check if username already exists
        if (userRepository.existsByEmail(clientRequestDTO.getEmail())) {
            throw new DuplicateResourceException("Ce nom d'utilisateur existe déjà");
        }

        // Create User first
        User user = User.builder()
                .email(clientRequestDTO.getEmail())
                .password(passwordEncoder.encode(clientRequestDTO.getPassword()))
                .role(UserRole.CLIENT)
                .build();

        User savedUser = userRepository.save(user);

        // Create Client
        Client client = Client.builder()
                .nom(clientRequestDTO.getNom())
                .email(clientRequestDTO.getEmail())
                .telephone(clientRequestDTO.getTelephone())
                .adresse(clientRequestDTO.getAdresse())
                .tier(CustomerTier.BASIC)
                .totalOrders(0)
                .totalSpent(0.0)
                .user(savedUser)
                .build();

        Client savedClient = clientRepository.save(client);

        return convertToResponseDTO(savedClient);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponseDTO getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        return convertToResponseDTO(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponseDTO> getAllClients(Pageable pageable) {
        Page<Client> clients = clientRepository.findAll(pageable);
        return clients.map(this::convertToResponseDTO);
    }

    @Override
    public ClientResponseDTO updateClient(Long id, ClientRequestDTO clientRequestDTO) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        // Check if new email already exists (if different from current)
        if (!clientRequestDTO.getEmail().equals(client.getEmail()) &&
            clientRepository.existsByEmail(clientRequestDTO.getEmail())) {
            throw new DuplicateResourceException("Un client avec cet email existe déjà");
        }

        // Update client fields
        client.setNom(clientRequestDTO.getNom());
        client.setEmail(clientRequestDTO.getEmail());
        client.setTelephone(clientRequestDTO.getTelephone());
        client.setAdresse(clientRequestDTO.getAdresse());

        Client updatedClient = clientRepository.save(client);
        return convertToResponseDTO(updatedClient);
    }

    @Override
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        clientRepository.delete(client);
    }

    // Helper method to convert Client entity to ClientResponseDTO
    private ClientResponseDTO convertToResponseDTO(Client client) {
        return ClientResponseDTO.builder()
                .id(client.getId())
                .nom(client.getNom())
                .email(client.getEmail())
                .telephone(client.getTelephone())
                .adresse(client.getAdresse())
                .tier(client.getTier())
                .totalOrders(client.getTotalOrders())
                .totalSpent(client.getTotalSpent())
                .firstOrderDate(client.getFirstOrderDate())
                .lastOrderDate(client.getLastOrderDate())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }
}