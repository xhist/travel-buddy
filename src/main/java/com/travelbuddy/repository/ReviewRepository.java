package com.travelbuddy.repository;

import com.travelbuddy.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Set;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Set<Review> findByReviewee(String reviewee);
}
