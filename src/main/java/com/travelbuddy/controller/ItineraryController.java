package com.travelbuddy.controller;

import com.travelbuddy.dto.ItineraryItemRequest;
import com.travelbuddy.dto.ItineraryResponse;
import com.travelbuddy.model.ItineraryItem;
import com.travelbuddy.service.interfaces.IItineraryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private ModelMapper modelMapper;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{tripId}")
    public ResponseEntity<Set<ItineraryResponse>> getTripItinerary(@PathVariable Long tripId) {
        return ResponseEntity.ok(itineraryService.getItineraryByTrip(tripId));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{tripId}/{userId}")
    public ResponseEntity<Set<ItineraryResponse>> getTripItinerary(@PathVariable Long tripId, @PathVariable Long userId) {
        return ResponseEntity.ok(itineraryService.getItineraryByTripAndUser(tripId, userId));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{tripId}")
    public ResponseEntity<Set<ItineraryResponse>> addToTripItinerary(@Valid @RequestBody final ItineraryItemRequest request) {
        return ResponseEntity.ok(itineraryService.addToTripItinerary(request));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{tripId}/{userId}")
    public ResponseEntity<Set<ItineraryResponse>> addToUserItinerary(@Valid @RequestBody final ItineraryItemRequest request) {
        return ResponseEntity.ok(itineraryService.addToUserItinerary(request));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/export/{tripId}")
    public CompletableFuture<ResponseEntity<byte[]>> exportItineraryPdf(@PathVariable Long tripId) {
        return itineraryService.exportItineraryPdf(tripId)
                .thenApply(pdfBytes -> {
                    if (pdfBytes == null) {
                        return ResponseEntity.notFound().build();
                    }
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.setContentDispositionFormData("attachment", "itinerary.pdf");
                    return ResponseEntity.ok().headers(headers).body(pdfBytes);
                });
    }
}
