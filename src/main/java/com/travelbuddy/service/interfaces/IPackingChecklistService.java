package com.travelbuddy.service.interfaces;

import com.travelbuddy.dto.ChecklistRequest;
import com.travelbuddy.dto.ChecklistResponse;
import com.travelbuddy.dto.UserChecklistResponse;
import com.travelbuddy.model.PackingChecklistItem;
import java.util.List;
import java.util.Set;

public interface IPackingChecklistService {
    Set<ChecklistResponse> getChecklistByTrip(Long tripId);
    Set<ChecklistResponse> getChecklistByTripAndUser(Long tripId, Long userId);
    Set<ChecklistResponse> addToTripChecklist(final ChecklistRequest tripChecklist);
    Set<ChecklistResponse> addToUserChecklist(final ChecklistRequest userChecklist);
    void deleteChecklistItem(Long id);
}
