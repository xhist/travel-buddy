package com.travelbuddy.service.interfaces;

import com.travelbuddy.model.ItineraryItem;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IItineraryService {
    List<ItineraryItem> getItineraryByTrip(Long tripId);
    CompletableFuture<byte[]> exportItineraryPdf(Long tripId);
}
