package com.travelbuddy.service.interfaces;

import com.travelbuddy.dto.TripResponse;
import com.travelbuddy.model.Trip;
import com.travelbuddy.model.User;
import java.util.List;
import java.util.Set;

public interface ITripService {
    Set<TripResponse> getTrips();
    TripResponse createTrip(Trip trip);
    TripResponse updateTrip(Trip trip, User currentUser);
    void deleteTrip(Long tripId, User currentUser);
    TripResponse getTripById(Long id);
    // Join and member management
    void joinTrip(Long tripId, User currentUser);
    void approveJoinRequest(Long tripId, Long userId, User currentUser);
    void kickMember(Long tripId, Long memberId, User currentUser);
}
