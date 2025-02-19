package com.travelbuddy.controller;

import com.travelbuddy.dto.PollRequest;
import com.travelbuddy.dto.PollResponse;
import com.travelbuddy.dto.PollVoteRequest;
import com.travelbuddy.security.CustomUserDetails;
import com.travelbuddy.service.PollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/polls")
public class PollController {

    @Autowired
    private PollService pollService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public ResponseEntity<List<PollResponse>> getTripPolls(@PathVariable Long tripId) {
        return ResponseEntity.ok(pollService.getTripPolls(tripId));
    }

    @PostMapping
    public ResponseEntity<PollResponse> createPoll(
            @PathVariable Long tripId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody PollRequest request) {
        PollResponse poll = pollService.createPoll(tripId, currentUser.getId(), request);

        // Notify all users in the trip about the new poll
        messagingTemplate.convertAndSend(
                "/topic/trip/" + tripId + "/polls",
                poll
        );

        return ResponseEntity.ok(poll);
    }

    @PostMapping("/{pollId}/vote")
    public ResponseEntity<PollResponse> vote(
            @PathVariable Long tripId,
            @PathVariable Long pollId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody PollVoteRequest request) {
        PollResponse updatedPoll = pollService.vote(pollId, currentUser.getId(), request.getOptionId());

        // Notify all users about the vote update
        messagingTemplate.convertAndSend(
                "/topic/trip/" + tripId + "/polls",
                updatedPoll
        );

        return ResponseEntity.ok(updatedPoll);
    }

    @PostMapping("/{pollId}/finalize")
    public ResponseEntity<PollResponse> finalizePoll(
            @PathVariable Long tripId,
            @PathVariable Long pollId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        PollResponse finalizedPoll = pollService.finalizePoll(pollId, currentUser.getId());

        // Notify all users about the poll finalization
        messagingTemplate.convertAndSend(
                "/topic/trip/" + tripId + "/polls",
                finalizedPoll
        );

        return ResponseEntity.ok(finalizedPoll);
    }

    @PutMapping("/{pollId}")
    public ResponseEntity<PollResponse> updatePoll(
            @PathVariable Long tripId,
            @PathVariable Long pollId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody PollRequest request) {
        PollResponse updatedPoll = pollService.updatePoll(pollId, currentUser.getId(), request);

        // Notify all users about the poll update
        messagingTemplate.convertAndSend(
                "/topic/trip/" + tripId + "/polls",
                updatedPoll
        );

        return ResponseEntity.ok(updatedPoll);
    }
}