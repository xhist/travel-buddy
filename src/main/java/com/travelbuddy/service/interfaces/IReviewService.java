package com.travelbuddy.service.interfaces;

import com.travelbuddy.model.Review;
import java.util.List;

public interface IReviewService {
    Review addReview(Review review);
    List<Review> getReviewsForEntity(String reviewee);
}
