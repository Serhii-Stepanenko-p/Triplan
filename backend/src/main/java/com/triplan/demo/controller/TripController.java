package com.triplan.demo.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.triplan.demo.dto.TripDTO;
import com.triplan.demo.service.TripService;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public ResponseEntity<TripDTO> createTrip(
            @Valid @RequestBody TripDTO tripDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tripService.createTrip(tripDTO, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<TripDTO>> getUserTrips(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tripService.getUserTrips(userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripDTO> updateTrip(
            @PathVariable Long id,
            @Valid @RequestBody TripDTO tripDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tripService.updateTrip(id, tripDTO, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        tripService.deleteTrip(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}