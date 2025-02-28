package com.travelbuddy.controller;

import com.travelbuddy.dto.TripRequest;
import com.travelbuddy.dto.TripResponse;
import com.travelbuddy.dto.UserDto;
import com.travelbuddy.repository.TripJoinRequestRepository;
import com.travelbuddy.security.CustomUserDetails;
import com.travelbuddy.security.SecurityEvaluator;
import com.travelbuddy.service.interfaces.ITripService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trips")
@Slf4j
public class TripController {

    @Autowired
    private ITripService tripService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private SecurityEvaluator securityEvaluator;

    @Autowired
    private TripJoinRequestRepository joinRequestRepository;

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
        final var createdTrip = tripService.createTrip(tripRequest, currentUser.getUser());
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

    @PreAuthorize("@securityEvaluator.isOrganizer(#tripId, authentication.principal.id)")
    @GetMapping("/{tripId}/pendingRequests")
    public ResponseEntity<Set<UserDto>> getTripRequests(@PathVariable Long tripId) {
        final var requests = tripService.getJoinRequests(tripId);
        log.info("Trip {} retrieved", tripId);
        return ResponseEntity.ok(requests);
    }

    // Update a trip (only organizer or admin)
    @PreAuthorize("hasRole('ROLE_ADMIN') || @securityEvaluator.isOrganizer(#tripId, authentication.principal.id)")
    @PutMapping
    public ResponseEntity<TripResponse> updateTrip(@Valid @RequestBody TripRequest tripRequest) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final var updatedTrip = tripService.updateTrip(tripRequest);
        return ResponseEntity.ok(updatedTrip);
    }

    // Delete a trip (only organizer or admin)
    @PreAuthorize("hasRole('ROLE_ADMIN') || @securityEvaluator.isOrganizer(#tripId, authentication.principal.id)")
    @DeleteMapping("/{tripId}")
    public ResponseEntity<?> deleteTrip(@PathVariable Long tripId) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        tripService.deleteTrip(tripId);
        return ResponseEntity.ok("Trip deleted successfully");
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{tripId}/joinStatus/{userId}")
    public ResponseEntity<?> getJoinStatus(@PathVariable Long tripId, @PathVariable Long userId) {
        final var pendingRequests = tripService.getJoinRequests(tripId).stream().map(UserDto::getId).collect(Collectors.toSet());
        return ResponseEntity.ok(Collections.singletonMap("hasPendingRequest", pendingRequests.contains(userId)));
    }

    // Send join request to a trip
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{tripId}/join")
    public ResponseEntity<?> joinTrip(@PathVariable Long tripId) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        tripService.joinTrip(tripId, currentUser.getId());
        return ResponseEntity.ok("Join request sent");
    }

    // Approve a join request (only organizer)
    @PreAuthorize("@securityEvaluator.isOrganizer(#tripId, authentication.principal.id)")
    @PostMapping("/{tripId}/approve/{userId}")
    public ResponseEntity<?> approveJoinRequest(@PathVariable Long tripId, @PathVariable Long userId) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        tripService.approveJoinRequest(tripId, userId);
        return ResponseEntity.ok("Join request approved");
    }

    // Approve a join request (only organizer)
    @PreAuthorize("@securityEvaluator.isOrganizer(#tripId, authentication.principal.id)")
    @PostMapping("/{tripId}/decline/{userId}")
    public ResponseEntity<?> declineJoinRequest(@PathVariable Long tripId, @PathVariable Long userId) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        tripService.declineJoinRequest(tripId, userId);
        return ResponseEntity.ok("Join request approved");
    }

    // Kick a member (only organizer)
    @PreAuthorize("@securityEvaluator.isOrganizer(#tripId, authentication.principal.id)")
    @PostMapping("/{tripId}/kick/{userId}")
    public ResponseEntity<?> kickMember(@PathVariable Long tripId, @PathVariable Long userId) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        tripService.kickMember(tripId, userId);
        return ResponseEntity.ok("Member kicked");
    }
}
