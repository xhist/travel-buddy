package com.travelbuddy.service;

import com.travelbuddy.dto.PollRequest;
import com.travelbuddy.dto.PollResponse;
import com.travelbuddy.dto.PollOptionResponse;
import com.travelbuddy.dto.UserDto;
import com.travelbuddy.model.*;
import com.travelbuddy.repository.*;
import com.travelbuddy.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PollService {
    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private PollOptionRepository optionRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public PollResponse createPoll(Long tripId, Long userId, PollRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Poll poll = Poll.builder()
                .question(request.getQuestion())
                .trip(trip)
                .creator(creator)
                .finalized(false)
                .build();

        poll = pollRepository.save(poll);

        Poll finalPoll = poll;
        List<PollOption> options = request.getOptions().stream()
                .map(optionText -> PollOption.builder()
                        .text(optionText)
                        .poll(finalPoll)
                        .votes(new HashSet<>())
                        .build())
                .collect(Collectors.toList());

        poll.setOptions(optionRepository.saveAll(options));

        return mapPollToResponse(poll);
    }

    @Transactional
    public PollResponse vote(Long pollId, Long userId, Long optionId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));

        if (poll.getFinalized()) {
            throw new IllegalStateException("Poll is already finalized");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Remove any existing votes by this user
        poll.getOptions().forEach(option -> option.getVotes().remove(user));

        PollOption selectedOption = poll.getOptions().stream()
                .filter(option -> option.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Option not found"));

        selectedOption.getVotes().add(user);
        optionRepository.save(selectedOption);

        return mapPollToResponse(poll);
    }

    @Transactional(readOnly = true)
    public List<PollResponse> getTripPolls(Long tripId) {
        return pollRepository.findByTripIdOrderByCreatedAtDesc(tripId).stream()
                .map(this::mapPollToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PollResponse finalizePoll(Long pollId, Long userId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));

        if (!poll.getCreator().getId().equals(userId) &&
                !poll.getTrip().getOrganizer().getId().equals(userId)) {
            throw new IllegalStateException("Only the creator or trip organizer can finalize the poll");
        }

        poll.setFinalized(true);
        poll = pollRepository.save(poll);

        return mapPollToResponse(poll);
    }

    @Transactional
    public PollResponse updatePoll(Long pollId, Long userId, PollRequest request) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));

        if (!poll.getCreator().getId().equals(userId) &&
                !poll.getTrip().getOrganizer().getId().equals(userId)) {
            throw new IllegalStateException("Only the creator or trip organizer can update the poll");
        }

        if (poll.getFinalized()) {
            throw new IllegalStateException("Cannot update a finalized poll");
        }

        poll.setQuestion(request.getQuestion());

        // Update existing options and add new ones
        Map<String, PollOption> existingOptions = poll.getOptions().stream()
                .collect(Collectors.toMap(PollOption::getText, option -> option));

        Poll finalPoll = poll;
        List<PollOption> updatedOptions = request.getOptions().stream()
                .map(optionText -> {
                    if (existingOptions.containsKey(optionText)) {
                        return existingOptions.get(optionText);
                    }
                    return PollOption.builder()
                            .text(optionText)
                            .poll(finalPoll)
                            .votes(new HashSet<>())
                            .build();
                })
                .collect(Collectors.toList());

        // Remove options not in the update request
        poll.getOptions().clear();
        poll.getOptions().addAll(updatedOptions);

        poll = pollRepository.save(poll);

        return mapPollToResponse(poll);
    }

    private PollResponse mapPollToResponse(Poll poll) {
        return PollResponse.builder()
                .id(poll.getId())
                .question(poll.getQuestion())
                .creator(UserDto.builder()
                        .id(poll.getCreator().getId())
                        .username(poll.getCreator().getUsername())
                        .profilePicture(poll.getCreator().getProfilePicture())
                        .build())
                .options(poll.getOptions().stream()
                        .map(option -> PollOptionResponse.builder()
                                .id(option.getId())
                                .text(option.getText())
                                .votes(option.getVotes().stream()
                                        .map(user -> UserDto.builder()
                                                .id(user.getId())
                                                .email(user.getEmail())
                                                .username(user.getUsername())
                                                .profilePicture(user.getProfilePicture())
                                                .build())
                                        .collect(Collectors.toSet()))
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(poll.getCreatedAt())
                .finalized(poll.getFinalized())
                .build();
    }
}