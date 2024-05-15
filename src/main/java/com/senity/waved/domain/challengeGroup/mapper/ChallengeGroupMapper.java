package com.senity.waved.domain.challengeGroup.mapper;

import com.senity.waved.domain.challenge.entity.Challenge;
import com.senity.waved.domain.challengeGroup.dto.response.ChallengeGroupHomeResponseDto;
import com.senity.waved.domain.challengeGroup.entity.ChallengeGroup;
import com.senity.waved.domain.challengeGroup.repository.ChallengeGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChallengeGroupMapper {

    private final ChallengeGroupRepository challengeGroupRepository;

    public ChallengeGroupHomeResponseDto toHomeResponseDto(Challenge challenge) {
        List<ChallengeGroup> group = challengeGroupRepository.findByChallengeIdAndGroupIndex(challenge.getId(), challenge.getLatestGroupIndex());
        return ChallengeGroupHomeResponseDto.of(group.get(0), challenge.getVerificationType(), challenge.getIsFree());
    }
}