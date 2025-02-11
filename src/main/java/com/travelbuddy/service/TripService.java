package com.travelbuddy.service;

import com.travelbuddy.dto.TripResponse;
import com.travelbuddy.dto.UserDto;
import com.travelbuddy.model.Trip;
import com.travelbuddy.model.TripStatus;
import com.travelbuddy.model.User;
import com.travelbuddy.repository.TripRepository;
import com.travelbuddy.exception.ResourceNotFoundException;
import com.travelbuddy.service.interfaces.ITripService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TripService implements ITripService {

    @Autowired
    private TripRepository tripRepository;

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
    public TripResponse createTrip(Trip trip) {
        trip.setStatus(TripStatus.UPCOMING);
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
    public TripResponse updateTrip(Trip trip, User currentUser) {
        final var existing = tripRepository.findById(trip.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id " + trip.getId()));
        // Only organizer may update the trip
        if (!existing.getOrganizer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the organizer can update the trip.");
        }
        existing.setTitle(trip.getTitle());
        existing.setDestination(trip.getDestination());
        existing.setStartDate(trip.getStartDate());
        existing.setEndDate(trip.getEndDate());
        existing.setDescription(trip.getDescription());
        tripRepository.save(existing);
        log.info("Trip {} updated successfully.", trip.getId());
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
    public void deleteTrip(Long tripId, User currentUser) {
        final var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id " + tripId));
        if (!trip.getOrganizer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the organizer can delete the trip.");
        }
        tripRepository.delete(trip);
        log.info("Trip {} deleted by organizer {}", tripId, currentUser.getUsername());
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
    public void joinTrip(Long tripId, User currentUser) {
        final var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id " + tripId));
        // Check if already a member or already requested
        if (trip.getMembers().stream().anyMatch(u -> u.getId().equals(currentUser.getId())) ||
                (trip.getJoinRequests() != null && trip.getJoinRequests().stream().anyMatch(u -> u.getId().equals(currentUser.getId())))) {
            throw new RuntimeException("Already a member or join request pending.");
        }
        trip.getJoinRequests().add(currentUser);
        tripRepository.save(trip);
        log.info("User {} requested to join trip {}", currentUser.getUsername(), trip.getId());
    }

    @Override
    @Transactional
    public void approveJoinRequest(Long tripId, Long userId, User currentUser) {
        final var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id " + tripId));
        if (!trip.getOrganizer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the organizer can approve join requests.");
        }
        User userToApprove = trip.getJoinRequests().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such join request."));
        trip.getJoinRequests().remove(userToApprove);
        trip.getMembers().add(userToApprove);
        tripRepository.save(trip);
        log.info("User {} approved to join trip {}", userToApprove.getUsername(), trip.getId());
    }

    @Override
    @Transactional
    public void kickMember(Long tripId, Long memberId, User currentUser) {
        final var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id " + tripId));
        if (!trip.getOrganizer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the organizer can kick members.");
        }
        boolean removed = trip.getMembers().removeIf(u -> u.getId().equals(memberId));
        if (!removed) {
            throw new RuntimeException("User is not a member of the trip.");
        }
        tripRepository.save(trip);
        log.info("Member with id {} kicked from trip {}", memberId, trip.getId());
    }
}
