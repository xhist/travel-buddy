package com.travelbuddy.service.interfaces;

import com.travelbuddy.model.Trip;
import com.travelbuddy.model.User;
import java.util.List;

public interface ITripService {
    Trip createTrip(Trip trip);
    Trip updateTrip(Trip trip, User currentUser);
    void deleteTrip(Long tripId, User currentUser);
    Trip getTripById(Long id);
    List<Trip> getTripsByOrganizer(Long organizerId);
    // Join and member management
    void joinTrip(Long tripId, User currentUser);
    void approveJoinRequest(Long tripId, Long userId, User currentUser);
    void kickMember(Long tripId, Long memberId, User currentUser);
}
