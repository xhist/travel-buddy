package com.travelbuddy.security;

import com.travelbuddy.model.Trip;
import com.travelbuddy.repository.TripRepository;
import com.travelbuddy.repository.ExpenseRepository;
import com.travelbuddy.repository.PackingChecklistRepository;
import com.travelbuddy.repository.ItineraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("securityEvaluator")
public class SecurityEvaluator {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PackingChecklistRepository packingChecklistRepository;

    @Autowired
    private ItineraryRepository itineraryRepository;

    public boolean isSameUser(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        return Objects.equals(((CustomUserDetails) auth.getPrincipal()).getId(), userId);
    }

    public boolean isOrganizerOrMember(Long tripId, Long userId) {
        return isOrganizer(tripId, userId) || isTripMember(tripId, userId);
    }

    public boolean isOrganizer(final Long tripId, final Long userId) {
        return tripRepository.findById(tripId)
                .map(trip -> trip.getOrganizer().getId().equals(userId))
                .orElse(false);
    }

    public boolean isTripMember(final Long tripId, final Long userId) {
        return tripRepository.findById(tripId)
                .map(trip -> trip.getMembers().stream()
                        .anyMatch(member -> member.getId().equals(userId)))
                .orElse(false);
    }

    public boolean isOrganizerForExpense(Long expenseId, Long userId) {
        if (expenseId == null || userId == null) {
            return false;
        }
        return expenseRepository.findById(expenseId)
                .map(expense -> expense.getTrip().getOrganizer().getId().equals(userId))
                .orElse(false);
    }

    public boolean canDeleteChecklistItem(Long itemId, Long userId) {
        if (itemId == null || userId == null) {
            return false;
        }
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

    public boolean canDeleteItineraryItem(Long itemId, Long userId) {
        if (itemId == null || userId == null) {
            return false;
        }
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