package com.senity.waved.domain.challenge.repository;

import com.senity.waved.domain.challenge.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<TypeIsFreeLGIProjection> findTypeIsFreeLGIById(Long id);
}