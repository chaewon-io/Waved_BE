package com.senity.waved.domain.myChallenge.controller;

import com.senity.waved.domain.myChallenge.dto.response.MyChallengeResponseDto;
import com.senity.waved.domain.myChallenge.service.MyChallengeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/myChallenges")
public class MyChallengeController {

    public final MyChallengeService myChallengeService;

    @DeleteMapping("/{myChallengeId}/delete")
    public ResponseEntity<String> cancelMyChallenge(
            @PathVariable("myChallengeId") Long myChallengeId,
            @AuthenticationPrincipal User user
    ) {
        myChallengeService.cancelAppliedMyChallenge(myChallengeId);
        return new ResponseEntity<>("챌린지 그룹 신청 취소를 완료했습니다.", HttpStatus.OK);
    }

    @GetMapping("/inProgress")
    public List<MyChallengeResponseDto> getMyChallengesInProgress(
            @AuthenticationPrincipal User user
    ) {
        return myChallengeService.getMyChallengesInProgressListed(user.getUsername());
    }

    @GetMapping("/waiting")
    public List<MyChallengeResponseDto> getMyChallengesWaiting(
            @AuthenticationPrincipal User user
    ) {
        return myChallengeService.getMyChallengesWaitingListed(user.getUsername());
    }

    @GetMapping("/completed")
    public List<MyChallengeResponseDto> getMyChallengesCompleted(
            @AuthenticationPrincipal User user
    ) {
        return myChallengeService.getMyChallengesCompletedListed(user.getUsername());
    }
}
