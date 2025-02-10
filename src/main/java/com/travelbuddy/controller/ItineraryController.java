package com.travelbuddy.controller;

import com.travelbuddy.service.interfaces.IItineraryService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
