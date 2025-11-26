package com.smartshop.api.controller;

import com.smartshop.api.dto.request.ClientRequestDTO;
import com.smartshop.api.dto.response.ClientResponseDTO;
import com.smartshop.api.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(
            @Valid @RequestBody ClientRequestDTO clientRequestDTO) {

        ClientResponseDTO createdClient = clientService.createClient(clientRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getClientById(@PathVariable Long id) {
        ClientResponseDTO client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @GetMapping
    public ResponseEntity<Page<ClientResponseDTO>> getAllClients(Pageable pageable) {
        Page<ClientResponseDTO> clients = clientService.getAllClients(pageable);
        return ResponseEntity.ok(clients);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequestDTO clientRequestDTO) {

        ClientResponseDTO updatedClient = clientService.updateClient(id, clientRequestDTO);
        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}