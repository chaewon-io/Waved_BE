package com.senity.waved.domain.challengeGroup.service;

import com.senity.waved.domain.challengeGroup.entity.ChallengeGroup;
import com.senity.waved.domain.challengeGroup.repository.ChallengeGroupRepository;
import com.senity.waved.domain.myChallenge.exception.MyChallengeNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeGroupUtil {

    private final ChallengeGroupRepository challengeGroupRepository;

    public ChallengeGroup getById(Long id) {
        return challengeGroupRepository.findById(id)
                .orElseThrow(() -> new MyChallengeNotFoundException("해당 마이 챌린지를 찾을 수 없습니다."));
    }

    public ChallengeGroup getByChallengeIdAndGroupIndex(Long challengeId, Long groupIndex) {
        List<ChallengeGroup> group = challengeGroupRepository.findByChallengeIdAndGroupIndex(challengeId, groupIndex);
        if (group.isEmpty()) {
            log.error("challenge id {}의 {}기 그룹을 찾을 수 없습니다.", challengeId, groupIndex);
            return null;
        } return group.get(0);
    }
}