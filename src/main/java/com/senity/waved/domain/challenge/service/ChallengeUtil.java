package com.senity.waved.domain.challenge.service;

import com.senity.waved.domain.challenge.entity.Challenge;
import com.senity.waved.domain.challenge.exception.ChallengeNotFoundException;
import com.senity.waved.domain.challenge.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeUtil {

    private final ChallengeRepository challengeRepository;

    public Challenge getById(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new ChallengeNotFoundException("해당 챌린지를 찾을 수 없습니다."));
    }
}