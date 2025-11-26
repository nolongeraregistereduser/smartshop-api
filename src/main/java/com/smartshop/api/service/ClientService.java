package com.smartshop.api.service;

import com.smartshop.api.dto.request.ClientRequestDTO;
import com.smartshop.api.dto.response.ClientResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {

    ClientResponseDTO createClient(ClientRequestDTO clientRequestDTO);

    ClientResponseDTO getClientById(Long id);

    Page<ClientResponseDTO> getAllClients(Pageable pageable);

    ClientResponseDTO updateClient(Long id, ClientRequestDTO clientRequestDTO);

    void deleteClient(Long id);
}