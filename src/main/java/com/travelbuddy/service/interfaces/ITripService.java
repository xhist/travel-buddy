package com.travelbuddy.service.interfaces;

import com.travelbuddy.dto.TripRequest;
import com.travelbuddy.dto.TripResponse;
import com.travelbuddy.dto.UserDto;
import com.travelbuddy.model.Trip;
import com.travelbuddy.model.User;
import java.util.List;
import java.util.Set;

public interface ITripService {
    Set<TripResponse> getTrips();
    TripResponse createTrip(final TripRequest trip, final User organizer);
    TripResponse updateTrip(final TripRequest trip);
    void deleteTrip(final Long tripId);
    TripResponse getTripById(final Long id);
    // Join and member management
    void joinTrip(final Long tripId, final Long userId);
    void approveJoinRequest(final Long tripId, final Long userId);
    void declineJoinRequest(final Long tripId, final Long userId);
    void kickMember(final Long tripId, final Long memberId);
    Set<UserDto> getJoinRequests(final Long tripId);
}
