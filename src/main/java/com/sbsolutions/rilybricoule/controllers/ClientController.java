package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.ClientDTO;
import com.sbsolutions.rilybricoule.entity.Client;
import com.sbsolutions.rilybricoule.repository.ClientRepository;
import com.sbsolutions.rilybricoule.services.GeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    
    private final ClientRepository clientRepository;
    private final GeocodingService geocodingService;

    @PostMapping
    public ResponseEntity<ClientDTO> createClient(@RequestBody ClientDTO request) {
        Client client = Client.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .address(request.getAddress())
            .build();
        setLatLngIfPossible(client);

        Client saved = clientRepository.save(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getClient(@PathVariable Long id) {
        Optional<Client> client = clientRepository.findById(id);
        if (client.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client not found");
        }
        return ResponseEntity.ok(toDTO(client.get()));
    }
    
    @GetMapping
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        List<ClientDTO> clients = clientRepository.findAll()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(clients);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @RequestBody ClientDTO request) {
        Optional<Client> clientOpt = clientRepository.findById(id);
        if (clientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client not found");
        }
        
        Client client = clientOpt.get();
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setAddress(request.getAddress());
        setLatLngIfPossible(client);
        Client updated = clientRepository.save(client);
        return ResponseEntity.ok(toDTO(updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        if (!clientRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client not found");
        }
        clientRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    private void setLatLngIfPossible(Client client) {
        if (client.getAddress() != null && !client.getAddress().isBlank()) {
            Double[] coords = geocodingService.getCoordinates(client.getAddress());
            if (coords != null) {
                client.setLatitude(coords[0]);
                client.setLongitude(coords[1]);
            }
        }
    }
    private ClientDTO toDTO(Client client) {
        return ClientDTO.builder()
            .id(client.getId())
            .firstName(client.getFirstName())
            .lastName(client.getLastName())
            .email(client.getEmail())
            .phone(client.getPhone())
            .address(client.getAddress())
            .latitude(client.getLatitude())
            .longitude(client.getLongitude())
            .build();
    }
}
