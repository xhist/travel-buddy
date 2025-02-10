package com.travelbuddy.service;

import com.travelbuddy.model.ItineraryItem;
import com.travelbuddy.model.Trip;
import com.travelbuddy.repository.ItineraryRepository;
import com.travelbuddy.repository.TripRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.travelbuddy.service.interfaces.IItineraryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class ItineraryService implements IItineraryService {

    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private ItineraryRepository itineraryRepository;

    @Override
    public List<ItineraryItem> getItineraryByTrip(Long tripId) {
        return itineraryRepository.findByTripId(tripId);
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
                document.add(new Paragraph("Date & Time: " + item.getActivityDateTime()));
                document.add(new Paragraph("Location: " + item.getLocation()));
                document.add(new Paragraph("Notes: " + item.getNotes()));
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
