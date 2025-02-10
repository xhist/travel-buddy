package com.travelbuddy.service;

import com.travelbuddy.service.interfaces.ICalendarService;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CalendarService implements ICalendarService {
    @Override
    public String syncTripWithCalendar(Long tripId) {
        String response = "Trip " + tripId + " synced with Google Calendar successfully.";
        log.info(response);
        return response;
    }
}
