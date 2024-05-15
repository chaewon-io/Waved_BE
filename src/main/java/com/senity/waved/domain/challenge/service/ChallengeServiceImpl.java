package com.senity.waved.domain.challenge.service;

import com.senity.waved.common.TimeUtil;
import com.senity.waved.domain.challenge.entity.Challenge;
import com.senity.waved.domain.challenge.repository.ChallengeRepository;
import com.senity.waved.domain.challengeGroup.entity.ChallengeGroup;
import com.senity.waved.domain.challengeGroup.repository.ChallengeGroupRepository;
import com.senity.waved.domain.challengeGroup.service.ChallengeGroupUtil;
import com.senity.waved.domain.member.entity.Member;
import com.senity.waved.domain.member.repository.MemberRepository;
import com.senity.waved.domain.member.service.MemberUtil;
import com.senity.waved.domain.myChallenge.entity.MyChallenge;
import com.senity.waved.domain.myChallenge.repository.MyChallengeRepository;
import com.senity.waved.domain.notification.entity.Notification;
import com.senity.waved.domain.notification.repository.NotificationRepository;
import com.senity.waved.domain.review.dto.response.ChallengeReviewResponseDto;
import com.senity.waved.domain.review.entity.Review;
import com.senity.waved.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeGroupRepository challengeGroupRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final MyChallengeRepository myChallengeRepository;
    private final NotificationRepository notificationRepository;

    private final MemberUtil memberUtil;
    private final ChallengeUtil challengeUtil;
    private final ChallengeGroupUtil challengeGroupUtil;
    private final TimeUtil timeUtil;

    @Override
    @Transactional(readOnly = true)
    public List<Challenge> getHomeChallengeGroupsListed() {
        int cnt = Math.toIntExact(challengeRepository.count());
        List<Challenge> challenges = new ArrayList<>();

        for (int i = 1; i <= cnt; i++) {
            Challenge challenge = challengeUtil.getById(i * 1L);
            challenges.add(challenge);
        }
        return challenges;
    }

    @Override
    @Transactional
    public Page<ChallengeReviewResponseDto> getReviewsPaged(Long challengeId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createDate").descending());
        Page<Review> reviewPaged = reviewRepository.findByChallengeId(challengeId, pageable);

        List<ChallengeReviewResponseDto> responseDtoList = getReviewListed(reviewPaged);
        return new PageImpl<>(responseDtoList, pageable, reviewPaged.getTotalElements());
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * MON")
    public void deleteOldNotifications() {
        ZonedDateTime deleteBefore = timeUtil.getTodayZoned().minusDays(14);
        notificationRepository.deleteNotificationsByCreateDate(deleteBefore);
    }

    @Transactional
    @Scheduled(cron = "0 0 1 * * MON")
    public void makeChallengeGroupAndDoNotificationScheduled() {
        List<Challenge> challengeList = challengeRepository.findAll();

        for (Challenge challenge : challengeList) {
            Long latestGroupIndex = challenge.getLatestGroupIndex();
            ChallengeGroup latestGroup = challengeGroupUtil.getByChallengeIdAndGroupIndex(challenge.getId(), latestGroupIndex);
            ZonedDateTime startDate = timeUtil.localToZoned(LocalDateTime.from(latestGroup.getStartDate()));

            if (startDate.equals(timeUtil.getTodayZoned())) {
                Long lastGroupIndex = challenge.getLatestGroupIndex() - 1;
                ChallengeGroup lastGroup = challengeGroupUtil.getByChallengeIdAndGroupIndex(challenge.getId(), lastGroupIndex);

                sendEndMessage(challenge, lastGroup);
                sendStartMessage(challenge, latestGroup);
                makeChallengeGroup(challenge, latestGroup);
            }
        }
    }

    private void makeChallengeGroup(Challenge challenge, ChallengeGroup latestGroup) {
        ChallengeGroup newGroup = ChallengeGroup.from(latestGroup, challenge);
        challengeGroupRepository.save(newGroup);
        challenge.updateLatestGroupIndex();
    }

    private void sendEndMessage(Challenge challenge, ChallengeGroup lastGroup) {
        String endMessage = String.format("%s %d기가 \r\n종료되었습니다. 진행 완료 챌린지 내역에서 \r\n성공 여부를 확인하고 환급 신청해주세요.", challenge.getTitle(), lastGroup.getGroupIndex());
        notifyMembersAppliedGroup(lastGroup.getId(), "챌린지 종료", endMessage);
    }

    private void sendStartMessage(Challenge challenge, ChallengeGroup latestGroup) {
        String startMessage = String.format("%s %d기가 \r\n오늘부터 시작됩니다.", challenge.getTitle(), latestGroup.getGroupIndex());
        notifyMembersAppliedGroup(latestGroup.getId(), "챌린지 시작", startMessage);
    }

    private void notifyMembersAppliedGroup(Long groupId, String title, String message) {
        List<MyChallenge> myChallengeList = myChallengeRepository.findByChallengeGroupIdAndIsPaidTrue(groupId);

        for(MyChallenge myChallenge: myChallengeList) {
            Member member = myChallenge.getMember();
            Notification newNotification = Notification.of(member, title, message);
            notificationRepository.save(newNotification);
            member.updateNewEvent(true);
            memberRepository.flush();
        }
    }

    private List<ChallengeReviewResponseDto> getReviewListed(Page<Review> reviewPaged) {
        return reviewPaged.getContent()
                .stream()
                .map(review -> {
                    Member member = memberUtil.getById(review.getMemberId());
                    return ChallengeReviewResponseDto.of(review, member);
                })
                .collect(Collectors.toList());
    }
}