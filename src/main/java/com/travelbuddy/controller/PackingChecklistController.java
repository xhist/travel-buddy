package com.travelbuddy.controller;

import com.travelbuddy.dto.ChecklistRequest;
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

@RestController
@RequestMapping("/api/checklists")
@Slf4j
public class PackingChecklistController {

    @Autowired
    private IPackingChecklistService checklistService;

    @Autowired
    private ModelMapper modelMapper;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    public ResponseEntity<?> addItem(@Valid @RequestBody ChecklistRequest request) {
        Trip trip = new Trip();
        trip.setId(request.getTripId());
        PackingChecklistItem item = PackingChecklistItem.builder()
                .itemName(request.getItemName())
                .category(request.getCategory())
                .completed(false)
                .trip(trip)
                .build();
        PackingChecklistItem createdItem = checklistService.addChecklistItem(item);
        return ResponseEntity.ok(createdItem);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{tripId}")
    public ResponseEntity<List<PackingChecklistItem>> getChecklist(@PathVariable Long tripId) {
        List<PackingChecklistItem> list = checklistService.getChecklistByTrip(tripId);
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        checklistService.deleteChecklistItem(id);
        return ResponseEntity.ok("Deleted successfully");
    }
}
