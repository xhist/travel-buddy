// src/main/java/com/travelbuddy/service/NotificationScheduler.java
package com.travelbuddy.service;

import com.travelbuddy.model.Trip;
import com.travelbuddy.model.TripStatus;
import com.travelbuddy.model.User;
import com.travelbuddy.repository.TripRepository;
import com.travelbuddy.service.interfaces.IEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class NotificationScheduler {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private IEmailService emailService;

    // Window (in minutes) during which the email will be sent
    @Value("${app.reminder.windowInMinutes:15}")
    private int reminderWindowInMinutes;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // Run every 30 minutes (adjust as needed)
    @Scheduled(cron = "0 0/30 * * * ?")
    public void sendTripReminders() {
        LocalDate today = LocalDate.now();
        // Round current time to minutes
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);

        List<Trip> trips = tripRepository.findAll();
        for (Trip trip : trips) {
            if (trip.getStatus() != TripStatus.UPCOMING) {
                continue;
            }
            // For each member in the trip
            for (User member : trip.getMembers()) {

                if (member.getNotificationPreferences() == null) {
                    break;
                }

                final var effectiveDays = member.getNotificationPreferences().getReminderDaysBefore();
                final var effectiveTime = member.getNotificationPreferences().getReminderTime();

                if (effectiveDays == null || effectiveTime == null) {
                    break;
                }

                LocalDate reminderDate = trip.getStartDate().minusDays(effectiveDays);
                if (today.equals(reminderDate)) {
                    // Check if current time is within the window of the effective reminder time.
                    LocalTime windowEnd = effectiveTime.plusMinutes(reminderWindowInMinutes);
                    if (!now.isBefore(effectiveTime) && now.isBefore(windowEnd)) {
                        String subject = "Upcoming Trip Reminder: " + trip.getTitle();
                        String text = "Dear " + member.getUsername() + ",\n\n" +
                                "This is a reminder for your upcoming trip to " + trip.getDestination() +
                                " starting on " + trip.getStartDate() + ".\n\nSafe travels!";
                        emailService.sendEmail(member.getEmail(), subject, text);
                        log.info("Sent reminder to {} for trip {}", member.getUsername(), trip.getId());
                    }
                }
            }
        }
    }
}
