package com.travelbuddy.controller;

import com.travelbuddy.service.interfaces.ICalendarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
@Slf4j
public class CalendarController {

    @Autowired
    private ICalendarService calendarService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/sync/{tripId}")
    public ResponseEntity<?> syncTrip(@PathVariable Long tripId) {
        String result = calendarService.syncTripWithCalendar(tripId);
        return ResponseEntity.ok(result);
    }
}
