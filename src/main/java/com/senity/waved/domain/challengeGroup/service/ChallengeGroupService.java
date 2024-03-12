package com.senity.waved.domain.challengeGroup.service;

import com.senity.waved.domain.challengeGroup.dto.response.ChallengeGroupResponseDto;
import com.senity.waved.domain.challengeGroup.dto.response.VerificationListResponseDto;

import java.sql.Timestamp;
import java.util.List;

public interface ChallengeGroupService {

    void applyForChallengeGroup(String email, Long groupId);
    ChallengeGroupResponseDto getGroupDetail(String email, Long groupId);
    List<VerificationListResponseDto> getVerifications(Long challengeGroupId, Timestamp verificationDate);
}