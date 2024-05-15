package com.senity.waved.domain.myChallenge.service;

import com.senity.waved.domain.challengeGroup.entity.ChallengeGroup;
import com.senity.waved.domain.member.entity.Member;
import com.senity.waved.domain.myChallenge.entity.MyChallenge;
import com.senity.waved.domain.myChallenge.exception.AlreadyMyChallengeExistsException;
import com.senity.waved.domain.myChallenge.exception.MyChallengeNotFoundException;
import com.senity.waved.domain.myChallenge.repository.MyChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MyChallengeUtil {

    private final MyChallengeRepository myChallengeRepository;

    public MyChallenge getById(Long id) {
        return myChallengeRepository.findById(id)
                .orElseThrow(() -> new MyChallengeNotFoundException("해당 마이 챌린지를 찾을 수 없습니다."));
    }

    public MyChallenge getByMemberAndGroup(Member member, ChallengeGroup challengeGroup) {
        return myChallengeRepository.findByMemberIdAndChallengeGroupIdAndIsPaidTrue(member.getId(), challengeGroup.getId())
                .orElseThrow(() -> new MyChallengeNotFoundException("해당 마이 챌린지를 찾을 수 없습니다."));
    }

    public void checkMyChallengeExistence(Long memberId, Long groupId) {
        Optional<MyChallenge> myChallenge = myChallengeRepository.findByMemberIdAndChallengeGroupIdAndIsPaidTrue(memberId, groupId);
        if (myChallenge.isPresent()) {
            throw new AlreadyMyChallengeExistsException("이미 신청되어있는 챌린지 그룹 입니다.");
        }
    }
}