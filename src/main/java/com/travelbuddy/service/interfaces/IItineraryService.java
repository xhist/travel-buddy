package com.travelbuddy.service.interfaces;

import com.travelbuddy.dto.ItineraryItemRequest;
import com.travelbuddy.dto.ItineraryResponse;
import com.travelbuddy.model.ItineraryItem;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface IItineraryService {
    Set<ItineraryResponse> getItineraryByTrip(final Long tripId);
    Set<ItineraryResponse> getItineraryByTripAndUser(final Long tripId, final Long userId);
    Set<ItineraryResponse> addToTripItinerary(final ItineraryItemRequest request);
    Set<ItineraryResponse> addToUserItinerary(final ItineraryItemRequest request);
    void deleteItineraryItem(final Long itemId);
    CompletableFuture<byte[]> exportItineraryPdf(final Long tripId);
}
