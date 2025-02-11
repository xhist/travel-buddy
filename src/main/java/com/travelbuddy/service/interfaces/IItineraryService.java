package com.travelbuddy.service.interfaces;

import com.travelbuddy.dto.ItineraryItemRequest;
import com.travelbuddy.dto.ItineraryResponse;
import com.travelbuddy.model.ItineraryItem;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface IItineraryService {
    Set<ItineraryResponse> getItineraryByTrip(Long tripId);
    Set<ItineraryResponse> getItineraryByTripAndUser(Long tripId, Long userId);
    Set<ItineraryResponse> addToTripItinerary(final ItineraryItemRequest request);
    Set<ItineraryResponse> addToUserItinerary(final ItineraryItemRequest request);
    CompletableFuture<byte[]> exportItineraryPdf(Long tripId);
}
