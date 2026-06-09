package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.entity.Client;
import com.sbsolutions.rilybricoule.entity.User;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import com.sbsolutions.rilybricoule.services.ReservationDispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dispatches")
@RequiredArgsConstructor
public class ReservationDispatchController {

    private final ReservationDispatchService reservationDispatchService;
    private final UserRepository userRepository;

    @PostMapping("/{reservationId}/accept")
    public ResponseEntity<String> acceptDispatch(@PathVariable Long reservationId) {
        Prestataire prestataire = getAuthenticatedPrestataire();

        boolean accepted = reservationDispatchService.acceptDispatch(
                reservationId,
                prestataire.getId()
        );

        if (!accepted) {
            return ResponseEntity.badRequest().body("Le dispatch ne peut pas être accepté");
        }

        return ResponseEntity.ok("Dispatch accepté avec succès");
    }

    @PostMapping("/{reservationId}/reject")
    public ResponseEntity<String> rejectDispatch(@PathVariable Long reservationId) {
        Prestataire prestataire = getAuthenticatedPrestataire();

        reservationDispatchService.rejectDispatch(
                reservationId,
                prestataire.getId()
        );

        return ResponseEntity.ok("Dispatch refusé avec succès");
    }

    @GetMapping("/myDispatch")
    public ResponseEntity<?> getMyDispatches() {
        Prestataire prestataire = getAuthenticatedPrestataire();
        return ResponseEntity.ok(
                reservationDispatchService.getDispatchesForPrestataire(prestataire.getId())
        );
    }

    private Prestataire getAuthenticatedPrestataire() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + email));

        if (!(user instanceof Prestataire prestataire)) {
            throw new IllegalArgumentException("Seul un prestataire peut effectuer cette action");
        }

        return prestataire;
    }

    @PostMapping("/{reservationId}/redispatch")
    public ResponseEntity<?> redispatchByClient(@PathVariable Long reservationId,
                                                Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!(user instanceof Client client)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Seul un client peut relancer un dispatch");
        }

        reservationDispatchService.redispatchByClient(reservationId, client.getId());

        return ResponseEntity.ok("Redispatch relancé avec succès");
    }


}