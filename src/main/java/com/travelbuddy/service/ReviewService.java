package com.travelbuddy.service;

import com.travelbuddy.model.Review;
import com.travelbuddy.repository.ReviewRepository;
import com.travelbuddy.service.interfaces.IReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ReviewService implements IReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public Review addReview(Review review) {
        review.setDateReviewed(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);
        log.info("Review added for {} by {}", review.getReviewee(), review.getReviewer());
        return savedReview;
    }

    @Override
    public List<Review> getReviewsForEntity(String reviewee) {
        return reviewRepository.findByReviewee(reviewee);
    }
}
