package com.travelbuddy.controller;

import com.travelbuddy.dto.ReviewRequest;
import com.travelbuddy.model.Review;
import com.travelbuddy.service.interfaces.IReviewService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping("/api/reviews")
@Slf4j
public class ReviewController {

    @Autowired
    private IReviewService reviewService;

    @Autowired
    private ModelMapper modelMapper;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    public ResponseEntity<?> addReview(@Valid @RequestBody ReviewRequest request) {
        Review review = Review.builder()
                .reviewer(request.getReviewer())
                .reviewee(request.getReviewee())
                .comment(request.getComment())
                .rating(request.getRating())
                .build();
        final var updatedReviews = reviewService.addReview(review);
        return ResponseEntity.ok(updatedReviews);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{reviewee}")
    public ResponseEntity<Set<Review>> getReviews(@PathVariable String reviewee) {
        Set<Review> reviews = reviewService.getReviewsForUser(reviewee);
        return ResponseEntity.ok(reviews);
    }
}
