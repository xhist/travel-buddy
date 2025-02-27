package com.travelbuddy.service;

import com.travelbuddy.dto.ChecklistCategoryResponse;
import com.travelbuddy.dto.ChecklistRequest;
import com.travelbuddy.dto.ChecklistResponse;
import com.travelbuddy.exception.ResourceNotFoundException;
import com.travelbuddy.model.PackingChecklistItem;
import com.travelbuddy.repository.ChecklistCategoryRepository;
import com.travelbuddy.repository.PackingChecklistRepository;
import com.travelbuddy.repository.TripRepository;
import com.travelbuddy.repository.UserRepository;
import com.travelbuddy.service.interfaces.IPackingChecklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PackingChecklistService implements IPackingChecklistService {

    @Autowired
    private PackingChecklistRepository checklistRepository;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChecklistCategoryRepository checklistCategoryRepository;

    @Override
    public Set<ChecklistResponse> addToTripChecklist(final ChecklistRequest request) {
        final var trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip %d not found!"
                        .formatted(request.getTripId())));
        final var category = checklistCategoryRepository.findByName(request.getCategory())
                .orElseThrow(() -> new ResourceNotFoundException("Category %s not found!"
                        .formatted(request.getCategory())));
        final var packingChecklistItem = PackingChecklistItem.builder()
                .itemName(request.getItemName())
                .trip(trip)
                .category(category)
                .build();
        checklistRepository.save(packingChecklistItem);
        log.info("Checklist item {} added for trip {}", request.getItemName(), request.getTripId());
        final var tripChecklist = checklistRepository.findByTripId(trip.getId());
        return tripChecklist.stream().map(checklist ->
                    ChecklistResponse.builder()
                            .id(checklist.getId())
                            .name(checklist.getItemName())
                            .category(checklist.getCategory().getName())
                            .tripId(checklist.getTrip().getId())
                            .build())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ChecklistResponse> getChecklistByTrip(Long tripId) {
        return checklistRepository.findByTripId(tripId).stream().map(checklist ->
                    ChecklistResponse.builder()
                            .id(checklist.getId())
                            .name(checklist.getItemName())
                            .category(checklist.getCategory().getName())
                            .tripId(checklist.getTrip().getId())
                            .build())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ChecklistResponse> addToUserChecklist(final ChecklistRequest request) {
        final var user = userRepository.findById(request.getUserId());
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User %d not found!".formatted(request.getUserId()));
        }
        final var trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip %d not found!".formatted(request.getTripId())));
        final var category = checklistCategoryRepository.findByName(request.getCategory())
                .orElseThrow(() -> new ResourceNotFoundException("Category %s not found!"
                        .formatted(request.getCategory())));
        final var packingChecklistItem = PackingChecklistItem.builder()
                .itemName(request.getItemName())
                .trip(trip)
                .category(category)
                .build();
        checklistRepository.save(packingChecklistItem);
        log.info("Checklist item {} added for trip {}", request.getItemName(), request.getTripId());
        final var tripChecklist = checklistRepository.findByTripId(trip.getId());
        return tripChecklist.stream().map(checklist ->
                    ChecklistResponse.builder()
                            .id(checklist.getId())
                            .name(checklist.getItemName())
                            .category(checklist.getCategory().getName())
                            .tripId(checklist.getTrip().getId())
                            .userId(checklist.getUser().getId())
                            .build())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ChecklistResponse> getChecklistByTripAndUser(Long tripId, Long userId) {
        return checklistRepository.findByTripId(tripId).stream().map(checklist -> new ChecklistResponse(checklist.getId(), checklist.getItemName(),
                        checklist.getCategory().getName(), checklist.getTrip().getId(), checklist.getUser() == null ? null : checklist.getUser().getId()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ChecklistCategoryResponse> getChecklistCategories() {
        return checklistCategoryRepository.findAll().stream()
                .map(category -> ChecklistCategoryResponse.builder()
                        .name(category.getName())
                        .build())
                .collect(Collectors.toSet());
    }

    @Override
    public void deleteChecklistItem(Long id) {
        checklistRepository.deleteById(id);
        log.info("Checklist item {} deleted", id);
    }
}
