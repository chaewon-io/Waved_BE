package com.senity.waved.domain.challengeGroup.service;

import com.senity.waved.domain.challenge.entity.Challenge;
import com.senity.waved.domain.challenge.service.ChallengeUtil;
import com.senity.waved.domain.challengeGroup.entity.ChallengeGroup;
import com.senity.waved.domain.liked.repository.LikedRepository;
import com.senity.waved.domain.member.entity.Member;
import com.senity.waved.domain.member.service.MemberUtil;
import com.senity.waved.domain.myChallenge.entity.MyChallenge;
import com.senity.waved.domain.myChallenge.repository.MyChallengeRepository;
import com.senity.waved.domain.myChallenge.service.MyChallengeUtil;
import com.senity.waved.domain.paymentRecord.entity.PaymentRecord;
import com.senity.waved.domain.paymentRecord.entity.PaymentStatus;
import com.senity.waved.domain.paymentRecord.repository.PaymentRecordRepository;
import com.senity.waved.domain.verification.dto.response.LinkVerificationResponseDto;
import com.senity.waved.domain.verification.dto.response.PictureVerificationResponseDto;
import com.senity.waved.domain.verification.dto.response.TextVerificationResponseDto;
import com.senity.waved.domain.verification.dto.response.VerificationResponseDto;
import com.senity.waved.domain.verification.entity.Verification;
import com.senity.waved.domain.verification.exception.VerifyNotFoundOnDateException;
import com.senity.waved.domain.verification.exception.WrongVerificationTypeException;
import com.senity.waved.domain.verification.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
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
@RequiredArgsConstructor
public class ChallengeGroupServiceImpl implements ChallengeGroupService {

    private final MyChallengeRepository myChallengeRepository;
    private final VerificationRepository verificationRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final LikedRepository likedRepository;

    private final MemberUtil memberUtil;
    private final ChallengeUtil challengeUtil;
    private final ChallengeGroupUtil challengeGroupUtil;
    private final MyChallengeUtil myChallengeUtil;

    @Override
    @Transactional
    public Long applyForChallengeGroup(String email, Long groupId, Long deposit) {
        Member member = memberUtil.getByEmail(email);
        ChallengeGroup group = challengeGroupUtil.getById(groupId);
        myChallengeUtil.checkMyChallengeExistence(member.getId(), groupId);

        MyChallenge newMyChallenge = MyChallenge.of(member, group, deposit);
        myChallengeRepository.save(newMyChallenge);

        savePaymentRecordWhenDepositZero(newMyChallenge, member, group);
        return newMyChallenge.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<ChallengeGroup, Challenge> getGroupDetail(Long groupId) {
        ChallengeGroup group = challengeGroupUtil.getById(groupId);
        Challenge challenge = challengeUtil.getById(group.getChallengeId());
        return Pair.of(group, challenge);
    }

    @Override
    @Transactional(readOnly = true)
    public MyChallenge getMyChallenge(String email, Long groupId) {
        if (Objects.isNull(email)) {
            return null;
        }

        Member member = memberUtil.getByEmail(email);
        Optional<MyChallenge> myChallenge = myChallengeRepository.findByMemberIdAndChallengeGroupIdAndIsPaidTrue(member.getId(), groupId);
        return myChallenge.isPresent() ? myChallenge.get() : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationResponseDto> getVerifications(String email, Long challengeGroupId, Timestamp verificationDate) {
        return getVerificationsByUserAndGroup(email, challengeGroupId, verificationDate, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationResponseDto> getUserVerifications(String email, Long challengeGroupId, Timestamp verificationDate) {
        return getVerificationsByUserAndGroup(email, challengeGroupId, verificationDate, true);
    }

    private List<VerificationResponseDto> getVerificationsByUserAndGroup(String email, Long challengeGroupId, Timestamp verificationDate, boolean isUserVerifications) {
        Member member = memberUtil.getByEmail(email);
        ChallengeGroup challengeGroup = challengeGroupUtil.getById(challengeGroupId);
        ZonedDateTime[] dateRange = calculateStartAndEndDate(verificationDate);
        List<Verification> verifications;

        if (isUserVerifications) {
            verifications = findVerificationsByMemberAndGroupAndDateRange(member, challengeGroup, dateRange);
        } else {
            verifications = findVerifications(challengeGroup, dateRange);
        } return convertToDtoList(verifications, member);
    }

    private ZonedDateTime[] calculateStartAndEndDate(Timestamp verificationDate) {
        ZonedDateTime startOfDay = verificationDate.toLocalDateTime().atZone(ZoneId.of("Asia/Seoul")).toLocalDate().atStartOfDay(ZoneId.of("Asia/Seoul"));
        ZonedDateTime endOfDay = startOfDay.withHour(23).withMinute(59).withSecond(59).withNano(999000000); // 23:59:59.999
        return new ZonedDateTime[]{startOfDay, endOfDay};
    }

    private List<Verification> findVerificationsByMemberAndGroupAndDateRange(Member member, ChallengeGroup challengeGroup, ZonedDateTime[] dateRange) {
        return verificationRepository.findByMemberIdAndChallengeGroupIdAndCreateDateBetweenAndIsDeletedFalse(
                member.getId(),
                challengeGroup.getId(),
                dateRange[0],
                dateRange[1]
        );
    }

    private List<Verification> findVerifications(ChallengeGroup challengeGroup, ZonedDateTime[] dateRange) {
        return verificationRepository.findByCreateDateBetweenAndChallengeGroupAndIsDeletedFalse (
                ZonedDateTime.of(dateRange[0].toLocalDate(), dateRange[0].toLocalTime(), dateRange[0].getZone()),
                ZonedDateTime.of(dateRange[1].toLocalDate(), dateRange[1].toLocalTime(), dateRange[1].getZone()),
                challengeGroup.getId()
        );
    }

    private List<VerificationResponseDto> convertToDtoList(List<Verification> verifications, Member member) {
        if (verifications.isEmpty()) {
            throw new VerifyNotFoundOnDateException("해당 날짜에 존재하는 인증내역이 없습니다.");
        }
        return verifications.stream()
                .map(verification -> {
                    Member verificationMember = memberUtil.getById(verification.getMemberId());
                    switch (verification.getVerificationType()) {
                        case TEXT:
                            return TextVerificationResponseDto.of(verification, verificationMember.getNickname(), isLikedByMember(verification, member));
                        case PICTURE:
                            return PictureVerificationResponseDto.of(verification, verificationMember.getNickname(), isLikedByMember(verification, member));
                        case LINK:
                            return LinkVerificationResponseDto.of(verification, verificationMember.getNickname(), isLikedByMember(verification, member));
                        case GITHUB:
                            break;
                        default:
                            throw new WrongVerificationTypeException("유효하지 않은 인증타입입니다.");
                    } return null;
                })
                .collect(Collectors.toList());
    }

    private boolean isLikedByMember(Verification verification, Member member) {
        return likedRepository.existsByMemberIdAndVerification(member.getId(), verification);
    }

    private void savePaymentRecordWhenDepositZero(MyChallenge myChallenge, Member member, ChallengeGroup group) {
        if (myChallenge.getDeposit() == 0) {
            String groupTitle = group.getGroupTitle();
            group.addParticipantCount();

            PaymentRecord paymentRecord = PaymentRecord.of(PaymentStatus.APPLIED, member, myChallenge, groupTitle);
            paymentRecordRepository.save(paymentRecord);
        }
    }
}