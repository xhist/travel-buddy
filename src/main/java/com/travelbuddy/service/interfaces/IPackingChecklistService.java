package com.travelbuddy.service.interfaces;

import com.travelbuddy.model.PackingChecklistItem;
import java.util.List;

public interface IPackingChecklistService {
    PackingChecklistItem addChecklistItem(PackingChecklistItem item);
    List<PackingChecklistItem> getChecklistByTrip(Long tripId);
    void deleteChecklistItem(Long id);
}
