package com.senity.waved.domain.challengeGroup.service;

import com.senity.waved.domain.challengeGroup.dto.response.ChallengeGroupResponseDto;
import com.senity.waved.domain.challengeGroup.entity.ChallengeGroup;
import com.senity.waved.domain.challengeGroup.exception.ChallengeGroupNotFoundException;
import com.senity.waved.domain.challengeGroup.repository.ChallengeGroupRepository;
import com.senity.waved.domain.member.entity.Member;
import com.senity.waved.domain.member.repository.MemberRepository;
import com.senity.waved.domain.myChallenge.entity.MyChallenge;
import com.senity.waved.domain.myChallenge.exception.AlreadyMyChallengeExistsException;
import com.senity.waved.domain.myChallenge.exception.MemberNotFoundException;
import com.senity.waved.domain.myChallenge.repository.MyChallengeRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChallengeGroupServiceImpl implements ChallengeGroupService {

    private final ChallengeGroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final MyChallengeRepository myChallengeRepository;

    public void applyForChallengeGroup(String email, Long groupId) {
        Member member = getMemberByEmail(email);
        ChallengeGroup group = getGroupById(groupId);

        Optional<MyChallenge> myChallenge = myChallengeRepository.findByMemberAndChallengeGroup(member, group);
        if (myChallenge.isPresent()) {
            throw new AlreadyMyChallengeExistsException("이미 신청되어있는 챌린지 그룹 입니다.");
        }

        MyChallenge newMyChallenge = MyChallenge.builder()
                .challengeGroup(group)
                .successCount(0L)
                .isReviewed(false)
                .member(member)
                .myVerifs(new int[14])
                .build();

        myChallengeRepository.save(newMyChallenge);
        group.addMyChallenge(newMyChallenge);
        groupRepository.save(group);
    }

    public ChallengeGroupResponseDto getGroupDetail(String email, Long groupId) {
        ChallengeGroup group = getGroupById(groupId);
        Member member = getMemberByEmail(email);
        Optional<MyChallenge> myChallenge = myChallengeRepository.findByMemberAndChallengeGroup(member, group);

        Boolean isApplied = myChallenge.isPresent()? true : false;
        return ChallengeGroup.getGroupResponse(group, isApplied);
    }

    private ChallengeGroup getGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ChallengeGroupNotFoundException("해당 챌린지 그룹을 찾을 수 없습니다."));
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.getMemberByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다."));
    }
}
