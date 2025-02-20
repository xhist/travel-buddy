package com.travelbuddy.controller;

import com.travelbuddy.dto.ChecklistRequest;
import com.travelbuddy.dto.ChecklistResponse;
import com.travelbuddy.dto.UserChecklistResponse;
import com.travelbuddy.model.PackingChecklistItem;
import com.travelbuddy.model.Trip;
import com.travelbuddy.service.interfaces.IPackingChecklistService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/api/checklists")
@Slf4j
public class PackingChecklistController {

    @Autowired
    private IPackingChecklistService checklistService;

    @Autowired
    private ModelMapper modelMapper;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{tripId}")
    public ResponseEntity<Set<ChecklistResponse>> addToTripChecklist(@PathVariable Long tripId, @Valid @RequestBody ChecklistRequest request) {
        if (tripId == null || request.getTripId() == null || !Objects.equals(tripId, request.getTripId())) {
            throw new IllegalArgumentException("Mismatch between request body and variable.");
        }
        final var updatedTripChecklist = checklistService.addToTripChecklist(request);
        return ResponseEntity.ok(updatedTripChecklist);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{tripId}/{userId}")
    public ResponseEntity<Set<ChecklistResponse>> addToUserChecklist(@PathVariable Long tripId, @PathVariable Long userId, @Valid @RequestBody ChecklistRequest request) {
        if ((tripId == null || request.getTripId() == null
                || !Objects.equals(tripId, request.getTripId()))
            || (userId == null || request.getUserId() == null
                || !Objects.equals(userId, request.getUserId()))) {
            throw new IllegalArgumentException("Mismatch between request body and variable.");
        }
        final var updatedTripChecklist = checklistService.addToUserChecklist(request);
        return ResponseEntity.ok(updatedTripChecklist);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{tripId}")
    public ResponseEntity<Set<ChecklistResponse>> getChecklistByTrip(@PathVariable Long tripId) {
        final var tripChecklist = checklistService.getChecklistByTrip(tripId);
        return ResponseEntity.ok(tripChecklist);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{tripId}/{userId}")
    public ResponseEntity<Set<ChecklistResponse>> getUserChecklistByTrip(@PathVariable Long tripId, @PathVariable Long userId) {
        final var userChecklist = checklistService.getChecklistByTripAndUser(tripId, userId);
        return ResponseEntity.ok(userChecklist);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChecklistItem(@PathVariable Long id) {
        checklistService.deleteChecklistItem(id);
        return ResponseEntity.ok("Deleted successfully");
    }
}
