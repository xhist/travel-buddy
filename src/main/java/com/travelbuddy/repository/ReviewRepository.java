package com.travelbuddy.repository;

import com.travelbuddy.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByReviewee(String reviewee);
}
