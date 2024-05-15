package com.senity.waved.domain.challengeGroup.controller;

import com.senity.waved.domain.challenge.entity.Challenge;
import com.senity.waved.domain.challengeGroup.dto.response.ChallengeGroupResponseDto;
import com.senity.waved.domain.challengeGroup.entity.ChallengeGroup;
import com.senity.waved.domain.challengeGroup.service.ChallengeGroupService;
import com.senity.waved.domain.myChallenge.entity.MyChallenge;
import com.senity.waved.domain.verification.dto.response.VerificationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challengeGroups")
public class ChallengeGroupController {

    public final ChallengeGroupService challengeGroupService;

    @GetMapping("/info/{challengeGroupId}")
    public ChallengeGroupResponseDto getChallengeGroup(
            @AuthenticationPrincipal User user,
            @PathVariable("challengeGroupId") Long groupId
    ) {
        String email = Objects.isNull(user) ? null : user.getUsername();
        Pair<ChallengeGroup, Challenge> groupChallengePair = challengeGroupService.getGroupDetail(groupId);

        MyChallenge myChallenge = challengeGroupService.getMyChallenge(email, groupId);
        Long myChallengeId = Objects.isNull(myChallenge) ? null : myChallenge.getId();
        return ChallengeGroupResponseDto.of(groupChallengePair.getFirst(), groupChallengePair.getSecond(), myChallengeId);
    }

    @PostMapping("/{challengeGroupId}")
    public Long applyChallengeGroup(
            @PathVariable("challengeGroupId") Long groupId,
            @AuthenticationPrincipal User user,
            @RequestParam("deposit") Long deposit
    ) {
        return challengeGroupService.applyForChallengeGroup(user.getUsername(), groupId, deposit);
    }

    @GetMapping("/{challengeGroupId}")
    public List<VerificationResponseDto> getVerificationsByDate (
            @PathVariable("challengeGroupId") Long challengeGroupId,
            @RequestParam("verificationDate") Timestamp verificationDate,
            @AuthenticationPrincipal User user
    ) {
        return challengeGroupService.getVerifications(user.getUsername(), challengeGroupId, verificationDate);
    }

    @GetMapping("/{challengeGroupId}/myVerifies")
    public List<VerificationResponseDto> getVerificationsByDateAndMember(
            @PathVariable("challengeGroupId") Long challengeGroupId,
            @RequestParam("verificationDate") Timestamp verificationDate,
            @AuthenticationPrincipal User user
    ) {
        return challengeGroupService.getUserVerifications(user.getUsername(), challengeGroupId, verificationDate);
    }
}