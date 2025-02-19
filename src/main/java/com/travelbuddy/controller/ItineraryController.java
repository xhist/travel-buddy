package com.travelbuddy.controller;

import com.travelbuddy.dto.ItineraryItemRequest;
import com.travelbuddy.dto.ItineraryResponse;
import com.travelbuddy.model.ItineraryItem;
import com.travelbuddy.service.interfaces.IItineraryService;
import com.travelbuddy.service.interfaces.ITripService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/itineraries")
@Slf4j
public class ItineraryController {

    @Autowired
    private IItineraryService itineraryService;

    @Autowired
    private ITripService tripService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{tripId}")
    public ResponseEntity<Set<ItineraryResponse>> getTripItinerary(@PathVariable Long tripId) {
        return ResponseEntity.ok(itineraryService.getItineraryByTrip(tripId));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{tripId}/{userId}")
    public ResponseEntity<Set<ItineraryResponse>> getUserTripItinerary(@PathVariable Long tripId, @PathVariable Long userId) {
        return ResponseEntity.ok(itineraryService.getItineraryByTripAndUser(tripId, userId));
    }

    @PreAuthorize("@tripService.isOrganizer(#tripId, authentication.principal.id)")
    @PostMapping("/{tripId}")
    public ResponseEntity<Set<ItineraryResponse>> addToTripItinerary(@Valid @RequestBody final ItineraryItemRequest request) {
        return ResponseEntity.ok(itineraryService.addToTripItinerary(request));
    }

    @PreAuthorize("hasRole('ROLE_USER') && authentication.principal.id == #request.userId")
    @PostMapping("/{tripId}/{userId}")
    public ResponseEntity<Set<ItineraryResponse>> addToUserItinerary(@Valid @RequestBody final ItineraryItemRequest request) {
        return ResponseEntity.ok(itineraryService.addToUserItinerary(request));
    }

    @PreAuthorize("@tripService.canDeleteItineraryItem(#itemId, authentication.principal.id)")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<String> deleteItineraryItem(@PathVariable final Long itemId) {
        itineraryService.deleteItineraryItem(itemId);
        return ResponseEntity.ok("Itinerary item deleted");
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/export/{tripId}")
    public ResponseEntity<byte[]> exportItineraryPdf(@PathVariable Long tripId) {
        try {
            CompletableFuture<byte[]> pdfBytes = itineraryService.exportItineraryPdf(tripId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("itinerary-" + tripId + ".pdf")
                    .build());

            return new ResponseEntity<>(pdfBytes.get(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error exporting PDF for trip {}: {}", tripId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
