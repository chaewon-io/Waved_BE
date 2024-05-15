package com.senity.waved.domain.myChallenge.service;

import com.senity.waved.domain.challengeGroup.entity.ChallengeGroup;
import com.senity.waved.domain.myChallenge.dto.response.MyChallengeResponseDto;
import com.senity.waved.domain.myChallenge.entity.ChallengeStatus;
import com.senity.waved.domain.myChallenge.entity.MyChallenge;
import org.springframework.data.util.Pair;

import java.util.List;

public interface MyChallengeService {
    Pair<MyChallenge, ChallengeGroup> getMyVerifications(Long myChallengeId);
    void cancelAppliedMyChallenge(String email, Long myChallengeId);
    List<MyChallengeResponseDto> getMyChallengesListed(String email, ChallengeStatus status);
}