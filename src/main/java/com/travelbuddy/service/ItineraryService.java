package com.travelbuddy.service;

import com.travelbuddy.dto.ItineraryItemRequest;
import com.travelbuddy.dto.ItineraryResponse;
import com.travelbuddy.exception.ResourceNotFoundException;
import com.travelbuddy.model.ItineraryItem;
import com.travelbuddy.model.Trip;
import com.travelbuddy.repository.ItineraryRepository;
import com.travelbuddy.repository.TripRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.travelbuddy.repository.UserRepository;
import com.travelbuddy.service.interfaces.IItineraryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItineraryService implements IItineraryService {

    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItineraryRepository itineraryRepository;

    @Override
    public Set<ItineraryResponse> getItineraryByTrip(final Long tripId) {
        return itineraryRepository.findByTripId(tripId).stream()
                .map(itinerary -> ItineraryResponse.builder()
                        .id(itinerary.getId())
                        .activityName(itinerary.getActivityName())
                        .tripId(itinerary.getTrip().getId())
                        .build())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ItineraryResponse> getItineraryByTripAndUser(final Long tripId, final Long userId) {
        return itineraryRepository.findByTripIdAndUserId(tripId, userId).stream()
                .map(itinerary ->
                    ItineraryResponse.builder()
                            .id(itinerary.getId())
                            .activityName(itinerary.getActivityName())
                            .tripId(itinerary.getTrip().getId())
                            .userId(itinerary.getUser().getId())
                            .build())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ItineraryResponse> addToTripItinerary(final ItineraryItemRequest request) {
        final var trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip %d not found".formatted(request.getTripId())));
        final var itineraryItem = ItineraryItem.builder()
                .activityName(request.getActivityName())
                .trip(trip)
                .build();
        itineraryRepository.save(itineraryItem);
        return itineraryRepository.findByTripId(trip.getId()).stream()
                .map(itinerary -> ItineraryResponse.builder()
                        .id(itinerary.getId())
                        .activityName(itinerary.getActivityName())
                        .tripId(itinerary.getTrip().getId())
                        .build())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ItineraryResponse> addToUserItinerary(ItineraryItemRequest request) {
        final var trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip %d not found".formatted(request.getTripId())));
        final var user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User %d not found".formatted(request.getTripId())));
        final var itineraryItem = ItineraryItem.builder()
                .activityName(request.getActivityName())
                .trip(trip)
                .user(user)
                .build();
        itineraryRepository.save(itineraryItem);
        return itineraryRepository.findByTripIdAndUserId(trip.getId(), user.getId()).stream()
                .map(itinerary -> ItineraryResponse.builder()
                        .id(itinerary.getId())
                        .activityName(itinerary.getActivityName())
                        .tripId(itinerary.getTrip().getId())
                        .userId(itinerary.getUser().getId())
                        .build())
                .collect(Collectors.toSet());
    }

    @Override
    public void deleteItineraryItem(final Long itemId) {
        final var itineraryItem = itineraryRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary item %d not found".formatted(itemId)));
        itineraryRepository.delete(itineraryItem);
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<byte[]> exportItineraryPdf(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        List<ItineraryItem> items = itineraryRepository.findByTripId(tripId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new Paragraph("Trip Itinerary for: " + trip.getTitle()));
            document.add(new Paragraph("Destination: " + trip.getDestination()));
            document.add(new Paragraph(" "));
            for (ItineraryItem item : items) {
                document.add(new Paragraph("Activity: " + item.getActivityName()));
                document.add(new Paragraph("-----------------------------------"));
            }
            document.close();
            log.info("PDF itinerary exported for trip {}", trip.getId());
        } catch (DocumentException e) {
            log.error("Error generating PDF for trip {}: {}", trip.getId(), e.getMessage());
        }
        return CompletableFuture.completedFuture(baos.toByteArray());
    }
}
