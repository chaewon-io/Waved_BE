package com.senity.waved.domain.challenge.service;

import com.senity.waved.domain.challenge.entity.Challenge;
import com.senity.waved.domain.review.dto.response.ChallengeReviewResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ChallengeService {
    List<Challenge> getHomeChallengeGroupsListed();
    Page<ChallengeReviewResponseDto> getReviewsPaged(Long challengeId, int pageNumber, int pageSize);
}