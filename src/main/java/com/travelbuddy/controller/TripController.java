package com.travelbuddy.controller;

import com.travelbuddy.dto.TripRequest;
import com.travelbuddy.dto.TripResponse;
import com.travelbuddy.model.Trip;
import com.travelbuddy.security.CustomUserDetails;
import com.travelbuddy.service.interfaces.ITripService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.LinkedList;
import java.util.Set;

@RestController
@RequestMapping("/api/trips")
@Slf4j
public class TripController {

    @Autowired
    private ITripService tripService;

    @Autowired
    private ModelMapper modelMapper;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping
    public ResponseEntity<Set<TripResponse>> getTrips() {
        final var trips = tripService.getTrips();
        return ResponseEntity.ok(trips);
    }

    // Create a trip – current user becomes organizer
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    public ResponseEntity<TripResponse> createTrip(@Valid @RequestBody TripRequest tripRequest) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Trip trip = Trip.builder()
                .title(tripRequest.getTitle())
                .destination(tripRequest.getDestination())
                .startDate(tripRequest.getStartDate())
                .endDate(tripRequest.getEndDate())
                .description(tripRequest.getDescription())
                .organizer(currentUser.getUser())
                .members(new LinkedList<>())
                .build();
        trip.getMembers().add(currentUser.getUser());
        final var createdTrip = tripService.createTrip(trip);
        log.info("Trip {} created", createdTrip.getId());
        return ResponseEntity.ok(createdTrip);
    }

    // Get trip details (public basic info)
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTrip(@PathVariable Long tripId) {
        final var trip = tripService.getTripById(tripId);
        log.info("Trip {} retrieved", trip.getId());
        return ResponseEntity.ok(trip);
    }

    // Update a trip (only organizer or admin)
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_ADMIN')")
    @PutMapping("/{tripId}")
    public ResponseEntity<TripResponse> updateTrip(@PathVariable Long tripId, @Valid @RequestBody TripRequest tripRequest) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final var trip = new Trip();
        trip.setTitle(tripRequest.getTitle());
        trip.setDestination(tripRequest.getDestination());
        trip.setStartDate(tripRequest.getStartDate());
        trip.setEndDate(tripRequest.getEndDate());
        trip.setDescription(tripRequest.getDescription());
        final var updatedTrip = tripService.updateTrip(trip, currentUser.getUser());
        return ResponseEntity.ok(updatedTrip);
    }

    // Delete a trip (only organizer or admin)
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{tripId}")
    public ResponseEntity<?> deleteTrip(@PathVariable Long tripId) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        tripService.deleteTrip(tripId, currentUser.getUser());
        return ResponseEntity.ok("Trip deleted successfully");
    }

    // Send join request to a trip
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/join/{tripId}")
    public ResponseEntity<?> joinTrip(@PathVariable Long tripId) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        tripService.joinTrip(tripId, currentUser.getUser());
        return ResponseEntity.ok("Join request sent");
    }

    // Approve a join request (only organizer)
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_ADMIN')")
    @PostMapping("/approve/{tripId}/{userId}")
    public ResponseEntity<?> approveJoinRequest(@PathVariable Long tripId, @PathVariable Long userId) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        tripService.approveJoinRequest(tripId, userId, currentUser.getUser());
        return ResponseEntity.ok("Join request approved");
    }

    // Kick a member (only organizer)
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/kick/{tripId}/{userId}")
    public ResponseEntity<?> kickMember(@PathVariable Long tripId, @PathVariable Long userId) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        tripService.kickMember(tripId, userId, currentUser.getUser());
        return ResponseEntity.ok("Member kicked");
    }
}
