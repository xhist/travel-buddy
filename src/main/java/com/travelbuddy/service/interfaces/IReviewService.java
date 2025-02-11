package com.travelbuddy.service.interfaces;

import com.travelbuddy.model.Review;
import java.util.Set;

public interface IReviewService {
    Set<Review> addReview(Review review);
    Set<Review> getReviewsForUser(String reviewee);
}
