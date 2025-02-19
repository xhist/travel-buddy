package com.travelbuddy.controller;

import com.travelbuddy.dto.ChecklistCategoryResponse;
import com.travelbuddy.dto.ChecklistRequest;
import com.travelbuddy.dto.ChecklistResponse;
import com.travelbuddy.service.interfaces.IPackingChecklistService;
import com.travelbuddy.service.interfaces.ITripService;
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
    private ITripService tripService;

    @PreAuthorize("@tripService.isOrganizer(#tripId, authentication.principal.id)")
    @PostMapping("/{tripId}")
    public ResponseEntity<Set<ChecklistResponse>> addToTripChecklist(@PathVariable Long tripId, @Valid @RequestBody ChecklistRequest request) {
        if (tripId == null || request.getTripId() == null || !Objects.equals(tripId, request.getTripId())) {
            throw new IllegalArgumentException("Mismatch between request body and variable.");
        }
        final var updatedTripChecklist = checklistService.addToTripChecklist(request);
        return ResponseEntity.ok(updatedTripChecklist);
    }

    @PreAuthorize("hasRole('ROLE_USER') && #authentication.principal.id == #userId")
    @PostMapping("/{tripId}/{userId}")
    public ResponseEntity<Set<ChecklistResponse>> addToUserChecklist(@Valid @RequestBody ChecklistRequest request) {
        final var updatedTripChecklist = checklistService.addToUserChecklist(request);
        return ResponseEntity.ok(updatedTripChecklist);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{tripId}")
    public ResponseEntity<Set<ChecklistResponse>> getChecklistByTrip(@PathVariable Long tripId) {
        final var tripChecklist = checklistService.getChecklistByTrip(tripId);
        return ResponseEntity.ok(tripChecklist);
    }

    @PreAuthorize("hasRole('ROLE_USER') && #authentication.principal.id == #userId")
    @GetMapping("/{tripId}/{userId}")
    public ResponseEntity<Set<ChecklistResponse>> getUserChecklistByTrip(@PathVariable Long tripId, @PathVariable Long userId) {
        final var userChecklist = checklistService.getChecklistByTripAndUser(tripId, userId);
        return ResponseEntity.ok(userChecklist);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/categories")
    public ResponseEntity<Set<ChecklistCategoryResponse>> getChecklistCategories() {
        final var userChecklist = checklistService.getChecklistCategories();
        return ResponseEntity.ok(userChecklist);
    }

    @PreAuthorize("@tripService.canDeleteChecklistItem(#id, authentication.principal.id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChecklistItem(@PathVariable Long id) {
        checklistService.deleteChecklistItem(id);
        return ResponseEntity.ok("Packing checklist item successfully deleted");
    }
}
