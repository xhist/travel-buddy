package com.travelbuddy.service;

import com.travelbuddy.model.Trip;
import com.travelbuddy.model.TripStatus;
import com.travelbuddy.model.User;
import com.travelbuddy.repository.TripRepository;
import com.travelbuddy.exception.ResourceNotFoundException;
import com.travelbuddy.service.interfaces.ITripService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.List;

@Service
@Slf4j
public class TripService implements ITripService {

    @Autowired
    private TripRepository tripRepository;

    @Override
    public Trip createTrip(Trip trip) {
        trip.setStatus(TripStatus.UPCOMING);
        Trip savedTrip = tripRepository.save(trip);
        log.info("Trip {} created successfully.", savedTrip.getTitle());
        return savedTrip;
    }

    @Override
    @Transactional
    public Trip updateTrip(Trip trip, User currentUser) {
        Trip existing = getTripById(trip.getId());
        // Only organizer may update the trip
        if (!existing.getOrganizer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the organizer can update the trip.");
        }
        existing.setTitle(trip.getTitle());
        existing.setDestination(trip.getDestination());
        existing.setStartDate(trip.getStartDate());
        existing.setEndDate(trip.getEndDate());
        existing.setDescription(trip.getDescription());
        Trip updated = tripRepository.save(existing);
        log.info("Trip {} updated successfully.", updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public void deleteTrip(Long tripId, User currentUser) {
        Trip trip = getTripById(tripId);
        if (!trip.getOrganizer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the organizer can delete the trip.");
        }
        tripRepository.delete(trip);
        log.info("Trip {} deleted by organizer {}", tripId, currentUser.getUsername());
    }

    @Override
    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id " + id));
    }

    @Override
    public List<Trip> getTripsByOrganizer(Long organizerId) {
        return tripRepository.findByOrganizerId(organizerId);
    }

    @Override
    @Transactional
    public void joinTrip(Long tripId, User currentUser) {
        Trip trip = getTripById(tripId);
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
        Trip trip = getTripById(tripId);
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
        Trip trip = getTripById(tripId);
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
