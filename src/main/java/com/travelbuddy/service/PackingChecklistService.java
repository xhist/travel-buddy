package com.travelbuddy.service;

import com.travelbuddy.model.PackingChecklistItem;
import com.travelbuddy.repository.PackingChecklistRepository;
import com.travelbuddy.service.interfaces.IPackingChecklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class PackingChecklistService implements IPackingChecklistService {

    @Autowired
    private PackingChecklistRepository checklistRepository;

    @Override
    public PackingChecklistItem addChecklistItem(PackingChecklistItem item) {
        PackingChecklistItem savedItem = checklistRepository.save(item);
        log.info("Checklist item {} added for trip {}", item.getItemName(), item.getTrip().getId());
        return savedItem;
    }

    @Override
    public List<PackingChecklistItem> getChecklistByTrip(Long tripId) {
        return checklistRepository.findByTripId(tripId);
    }

    @Override
    public void deleteChecklistItem(Long id) {
        checklistRepository.deleteById(id);
        log.info("Checklist item {} deleted", id);
    }
}
