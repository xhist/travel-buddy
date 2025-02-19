package com.travelbuddy.repository;

import com.travelbuddy.model.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
    List<PollOption> findByPollId(Long pollId);
}