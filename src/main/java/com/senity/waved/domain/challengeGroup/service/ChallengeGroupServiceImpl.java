package com.senity.waved.domain.challengeGroup.service;

import com.senity.waved.domain.challenge.entity.Challenge;
import com.senity.waved.domain.challenge.exception.ChallengeNotFoundException;
import com.senity.waved.domain.challenge.repository.ChallengeRepository;
import com.senity.waved.domain.challengeGroup.dto.response.ChallengeGroupResponseDto;
import com.senity.waved.domain.challengeGroup.dto.response.VerificationListResponseDto;
import com.senity.waved.domain.challengeGroup.entity.ChallengeGroup;
import com.senity.waved.domain.challengeGroup.exception.ChallengeGroupNotFoundException;
import com.senity.waved.domain.challengeGroup.repository.ChallengeGroupRepository;
import com.senity.waved.domain.liked.repository.LikedRepository;
import com.senity.waved.domain.member.entity.Member;
import com.senity.waved.domain.member.exception.MemberNotFoundException;
import com.senity.waved.domain.member.repository.MemberRepository;
import com.senity.waved.domain.myChallenge.entity.MyChallenge;
import com.senity.waved.domain.myChallenge.exception.AlreadyMyChallengeExistsException;
import com.senity.waved.domain.myChallenge.repository.MyChallengeRepository;
import com.senity.waved.domain.verification.entity.Verification;
import com.senity.waved.domain.verification.exception.VerifyExistenceOnDateException;
import com.senity.waved.domain.verification.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChallengeGroupServiceImpl implements ChallengeGroupService {

    private final MemberRepository memberRepository;
    private final MyChallengeRepository myChallengeRepository;
    private final VerificationRepository verificationRepository;
    private final ChallengeGroupRepository challengeGroupRepository;
    private final ChallengeRepository challengeRepository;
    private final LikedRepository likedRepository;

    // TODO 대기중인 챌린지만 신청 가능
    public Long applyForChallengeGroup(String email, Long groupId, Long deposit) {
        Member member = getMemberByEmail(email);
        ChallengeGroup group = getGroupById(groupId);

        Optional<MyChallenge> myChallenge = myChallengeRepository.findByMemberIdAndChallengeGroupId(member.getId(), group.getId());
        if (myChallenge.isPresent()) {
            throw new AlreadyMyChallengeExistsException("이미 신청되어있는 챌린지 그룹 입니다.");
        }

        MyChallenge newMyChallenge = MyChallenge.builder()
                .challengeGroup(group)
                .successCount(0L)
                .isReviewed(false)
                .memberId(member.getId())
                .myVerifs(300000000000000L)
                .deposit(deposit)
                .isRefundRequested(false)
                .startDate(group.getStartDate())
                .endDate(group.getEndDate())
                .build();

        myChallengeRepository.save(newMyChallenge);
        return newMyChallenge.getId();
    }

    public ChallengeGroupResponseDto getGroupDetail(String email, Long groupId) {
        ChallengeGroup group = getGroupById(groupId);
        Challenge challenge = getChallengeById(group.getChallengeId());
        if (Objects.isNull(email))
            return ChallengeGroup.getGroupResponse(group, challenge, -1L);

        Member member = getMemberByEmail(email);
        Optional<MyChallenge> myChallenge = myChallengeRepository.findByMemberIdAndChallengeGroupId(member.getId(), group.getId());

        Long myChallengeId = myChallenge.isPresent() ? myChallenge.get().getId() : -1L;
        return ChallengeGroup.getGroupResponse(group, challenge, myChallengeId);
    }

    @Override
    public List<VerificationListResponseDto> getVerifications(String email, Long challengeGroupId, Timestamp verificationDate) {
        Member member = getMemberByEmail(email);
        ChallengeGroup challengeGroup = getGroupById(challengeGroupId);
        ZonedDateTime[] dateRange = calculateStartAndEndDate(verificationDate);
        List<Verification> verifications = findVerifications(challengeGroup, dateRange);
        return convertToDtoList(verifications, member);
    }

    @Override
    public List<VerificationListResponseDto> getUserVerifications(String email, Long challengeGroupId, Timestamp verificationDate) {
        Member member = getMemberByEmail(email);
        ChallengeGroup challengeGroup = getGroupById(challengeGroupId);
        ZonedDateTime[] dateRange = calculateStartAndEndDate(verificationDate);
        List<Verification> verifications = findVerificationsByMemberAndGroupAndDateRange(member, challengeGroup, dateRange);
        return convertToDtoList(verifications, member);
    }

    private ChallengeGroup getGroupById(Long id) {
        return challengeGroupRepository.findById(id)
                .orElseThrow(() -> new ChallengeGroupNotFoundException("해당 챌린지 그룹을 찾을 수 없습니다."));
    }

    private Challenge getChallengeById(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new ChallengeNotFoundException("해당 챌린지를 찾을 수 없습니다."));
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.getMemberByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다."));
    }

    private ZonedDateTime[] calculateStartAndEndDate(Timestamp verificationDate) {
        ZonedDateTime startOfDay = verificationDate.toLocalDateTime().atZone(ZoneId.of("Asia/Seoul")).toLocalDate().atStartOfDay(ZoneId.of("Asia/Seoul"));
        ZonedDateTime endOfDay = startOfDay.withHour(23).withMinute(59).withSecond(59).withNano(999000000); // 23:59:59.999

        return new ZonedDateTime[]{startOfDay, endOfDay};
    }

    private List<Verification> findVerifications(ChallengeGroup challengeGroup, ZonedDateTime[] dateRange) {
        return verificationRepository.findByCreateDateBetweenAndChallengeGroupAndIsDeletedFalse (
                ZonedDateTime.of(dateRange[0].toLocalDate(), dateRange[0].toLocalTime(), dateRange[0].getZone()),
                ZonedDateTime.of(dateRange[1].toLocalDate(), dateRange[1].toLocalTime(), dateRange[1].getZone()),
                challengeGroup.getId()
        );
    }

    private List<Verification> findVerificationsByMemberAndGroupAndDateRange(Member member, ChallengeGroup challengeGroup, ZonedDateTime[] dateRange) {
        return verificationRepository.findByMemberIdAndChallengeGroupIdAndCreateDateBetweenAndIsDeletedFalse(
                member.getId(),
                challengeGroup.getId(),
                dateRange[0],
                dateRange[1]
        );
    }

    private List<VerificationListResponseDto> convertToDtoList(List<Verification> verifications, Member member) {
        if (verifications.isEmpty()) {
            throw new VerifyExistenceOnDateException("해당 날짜에 존재하는 인증내역이 없습니다.");
        }
        return verifications.stream()
                .map(verification -> new VerificationListResponseDto(verification, member, isLikedByMember(verification, member)))
                .collect(Collectors.toList());
    }

    private boolean isLikedByMember(Verification verification, Member member) {
        return likedRepository.existsByMemberAndVerification(member, verification);
    }
}