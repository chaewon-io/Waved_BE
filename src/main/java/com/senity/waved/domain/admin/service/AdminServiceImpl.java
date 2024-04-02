package com.senity.waved.domain.admin.service;

import com.senity.waved.domain.challengeGroup.dto.response.AdminChallengeGroupResponseDto;
import com.senity.waved.domain.challengeGroup.entity.ChallengeGroup;
import com.senity.waved.domain.challengeGroup.exception.ChallengeGroupNotFoundException;
import com.senity.waved.domain.challengeGroup.repository.ChallengeGroupRepository;
import com.senity.waved.domain.member.entity.Member;
import com.senity.waved.domain.member.exception.MemberNotFoundException;
import com.senity.waved.domain.member.repository.MemberRepository;
import com.senity.waved.domain.myChallenge.entity.MyChallenge;
import com.senity.waved.domain.myChallenge.exception.MyChallengeNotFoundException;
import com.senity.waved.domain.myChallenge.repository.MyChallengeRepository;
import com.senity.waved.domain.verification.dto.response.AdminVerificationDto;
import com.senity.waved.domain.verification.entity.Verification;
import com.senity.waved.domain.verification.exception.VerificationNotFoundException;
import com.senity.waved.domain.verification.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ChallengeGroupRepository groupRepository;
    private final VerificationRepository verificationRepository;
    private final MyChallengeRepository myChallengeRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AdminChallengeGroupResponseDto> getGroups() {
        ZonedDateTime todayStart = ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault());
        List<ChallengeGroup> groups = groupRepository.findChallengeGroupsInProgress(todayStart);

        return groups.stream()
                .map(AdminChallengeGroupResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminVerificationDto> getGroupVerificationsPaged(Long challengeGroupId, int pageNumber, int pageSize) {
        ChallengeGroup challengeGroup = getGroupById(challengeGroupId);
        List<Verification> verifications = verificationRepository.findByChallengeGroupId(challengeGroup.getId());

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<AdminVerificationDto> verificationDtoList = getPaginatedVerificationDtoList(verifications, pageable);

        return new PageImpl<>(verificationDtoList, pageable, verifications.size());
    }

    @Override
    @Transactional
    public void deleteVerification(Long groupId, Long verificationId) {
        Verification verification = getVerificationById(verificationId);
        verification.markAsDeleted(true);

        Member member = getMemberById(verification.getMemberId());
        ChallengeGroup group = getGroupById(verification.getChallengeGroupId());

        MyChallenge myChallenge = getMyChallengeByGroupAndMemberId(group, member.getId());
        myChallenge.deleteVerification(verification.getCreateDate());
        verificationRepository.save(verification);
    }

    private List<AdminVerificationDto> getPaginatedVerificationDtoList(List<Verification> verifs, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), verifs.size());

        return verifs.subList(start, end)
                .stream()
                .map(verification -> {
                    Member member = getMemberById(verification.getMemberId());
                    return AdminVerificationDto.from(verification, member);
                })
                .collect(Collectors.toList());
    }

    private Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("해당 멤버를 찾을 수 없습니다."));
    }

    private Verification getVerificationById(Long id) {
        return verificationRepository.findById(id)
                .orElseThrow(() -> new VerificationNotFoundException("해당 인증 내역을 찾을 수 없습니다."));
    }

    private ChallengeGroup getGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ChallengeGroupNotFoundException("해당 챌린지 그룹을 찾을 수 없습니다."));
    }

    private MyChallenge getMyChallengeByGroupAndMemberId(ChallengeGroup group, Long memberId) {
        return myChallengeRepository.findByMemberIdAndChallengeGroupIdAndIsPaid(memberId, group.getId(), true)
                .orElseThrow(() -> new MyChallengeNotFoundException("해당 마이챌린지를 찾을 수 없습니다."));
    }
}

