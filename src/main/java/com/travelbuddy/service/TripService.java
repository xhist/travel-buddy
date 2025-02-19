package com.travelbuddy.service;

import com.travelbuddy.dto.TripRequest;
import com.travelbuddy.dto.TripResponse;
import com.travelbuddy.dto.UserDto;
import com.travelbuddy.model.*;
import com.travelbuddy.repository.*;
import com.travelbuddy.exception.ResourceNotFoundException;
import com.travelbuddy.service.interfaces.ITripService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TripService implements ITripService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripJoinRequestRepository joinRequestRepository;

    @Autowired
    private PackingChecklistRepository packingChecklistRepository;

    @Autowired
    private ItineraryRepository itineraryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Set<TripResponse> getTrips() {
        return tripRepository.findAll().stream().map(trip -> TripResponse.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .organizer(trip.getOrganizer().getId())
                .members(trip.getMembers().stream()
                        .map(member -> modelMapper.map(member, UserDto.class)).toList())
                .status(trip.getStatus())
                .startDate(trip.getStartDate())
                .destination(trip.getDestination())
                .description(trip.getDescription())
                .endDate(trip.getEndDate())
                .build()
        ).collect(Collectors.toSet());
    }

    @Override
    public TripResponse createTrip(final TripRequest tripRequest, final User organizer) {
        Trip trip = Trip.builder()
                .title(tripRequest.getTitle())
                .destination(tripRequest.getDestination())
                .status(TripStatus.UPCOMING)
                .startDate(tripRequest.getStartDate())
                .endDate(tripRequest.getEndDate())
                .description(tripRequest.getDescription())
                .organizer(organizer)
                .members(new LinkedList<>(Collections.singleton(organizer)))
                .build();
        tripRepository.save(trip);
        log.info("Trip {} created successfully.", trip.getTitle());
        return TripResponse.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .organizer(trip.getOrganizer().getId())
                .members(trip.getMembers().stream()
                        .map(member -> modelMapper.map(member, UserDto.class)).toList())
                .status(trip.getStatus())
                .startDate(trip.getStartDate())
                .destination(trip.getDestination())
                .description(trip.getDescription())
                .endDate(trip.getEndDate())
                .build();
    }

    @Override
    @Transactional
    public TripResponse updateTrip(TripRequest trip) {
        final var existing = tripRepository.findById(trip.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id " + trip.getId()));
        // Only organizer may update the trip
        existing.setTitle(trip.getTitle());
        existing.setDestination(trip.getDestination());
        existing.setStartDate(trip.getStartDate());
        existing.setEndDate(trip.getEndDate());
        existing.setDescription(trip.getDescription());
        tripRepository.save(existing);
        log.info("Trip {} updated successfully.", trip.getId());
        return TripResponse.builder()
                .id(existing.getId())
                .title(existing.getTitle())
                .organizer(existing.getOrganizer().getId())
                .members(existing.getMembers().stream()
                        .map(member -> modelMapper.map(member, UserDto.class)).toList())
                .status(existing.getStatus())
                .startDate(existing.getStartDate())
                .destination(existing.getDestination())
                .description(existing.getDescription())
                .endDate(existing.getEndDate())
                .build();
    }

    @Override
    @Transactional
    public void deleteTrip(Long tripId) {
        final var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id " + tripId));
        tripRepository.delete(trip);
        log.info("Trip {} deleted", tripId);
    }

    @Override
    public TripResponse getTripById(Long id) {
        final var trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id " + id));
        return TripResponse.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .organizer(trip.getOrganizer().getId())
                .members(trip.getMembers().stream()
                        .map(member -> modelMapper.map(member, UserDto.class)).toList())
                .status(trip.getStatus())
                .startDate(trip.getStartDate())
                .destination(trip.getDestination())
                .description(trip.getDescription())
                .endDate(trip.getEndDate())
                .build();
    }

    @Override
    @Transactional
    public void joinTrip(Long tripId, Long userId) {
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User %d not found"
                        .formatted(userId)));
        // Check if user already has a pending request
        if (joinRequestRepository.findByUserIdAndTripId(userId, tripId).isPresent()) {
            throw new IllegalArgumentException("User %s already requested to join this trip"
                    .formatted(user.getUsername()));
        }

        final var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id " + tripId));
        TripJoinRequest request = TripJoinRequest.builder()
                .user(user)
                .trip(trip)
                .requestDate(LocalDateTime.now())
                .build();

        joinRequestRepository.save(request);
        log.info("User {} requested to join trip {}", user.getUsername(), tripId);
    }

    @Override
    @Transactional
    public void approveJoinRequest(Long tripId, Long userId) {
        final var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id " + tripId));
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        final var joinRequest = joinRequestRepository.findByUserIdAndTripId(userId, tripId);
        if (joinRequest.isEmpty()) {
            throw new RuntimeException("There is no pending request from %s to join trip to %s."
                    .formatted(user.getUsername(), trip.getDestination()));
        }
        trip.getMembers().add(user);
        user.getTrips().add(trip);
        joinRequestRepository.delete(joinRequest.get());
        log.info("User {} approved to join trip {}", user.getUsername(), trip.getId());
    }

    @Override
    public void declineJoinRequest(Long tripId, Long userId) {
        final var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id " + tripId));
        final var user = userRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        final var joinRequest = joinRequestRepository.findByUserIdAndTripId(userId, tripId);
        if (joinRequest.isEmpty()) {
            throw new RuntimeException("There is no pending request from %s to join trip to %s."
                    .formatted(user.getUsername(), trip.getDestination()));
        }
        joinRequestRepository.delete(joinRequest.get());
        log.info("Request of user {} to join trip {} was declined", user.getUsername(), trip.getId());
    }

    @Override
    @Transactional
    public void kickMember(Long tripId, Long memberId) {
        final var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id " + tripId));
        final var member = userRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + memberId));
        boolean removed = trip.getMembers().removeIf(u -> u.getId().equals(memberId));
        if (!removed) {
            throw new RuntimeException("User is not a member of the trip.");
        }
        tripRepository.save(trip);
        log.info("Member with id {} kicked from trip {}", memberId, trip.getId());
    }

    @Override
    public Set<UserDto> getJoinRequests(Long tripId) {
        final var tripRequests = joinRequestRepository.findByTripId(tripId);
        return tripRequests.stream()
                .map(request ->
                        modelMapper.map(request.getUser(), UserDto.class))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isOrganizer(Long tripId, Long userId) {
        return tripRepository.findById(tripId)
                .map(trip -> trip.getOrganizer().getId().equals(userId))
                .orElse(false);
    }

    @Override
    public boolean isOrganizerForExpense(Long expenseId, Long userId) {
        return expenseRepository.findById(expenseId)
                .map(expense -> expense.getTrip().getOrganizer().getId().equals(userId))
                .orElse(false);
    }

    @Override
    public boolean canDeleteChecklistItem(final Long itemId, final Long userId) {
        return packingChecklistRepository.findById(itemId)
                .map(item -> {
                    // Check if user is the item owner
                    if (item.getUser() != null && item.getUser().getId().equals(userId)) {
                        return true;
                    }
                    // Check if user is the trip organizer
                    return item.getTrip().getOrganizer().getId().equals(userId);
                })
                .orElse(false);
    }

    @Override
    public boolean canDeleteItineraryItem(final Long itemId, final Long userId) {
        return itineraryRepository.findById(itemId)
                .map(item -> {
                    // Check if user is the item owner
                    if (item.getUser() != null && item.getUser().getId().equals(userId)) {
                        return true;
                    }
                    // Check if user is the trip organizer
                    return item.getTrip().getOrganizer().getId().equals(userId);
                })
                .orElse(false);
    }
}
