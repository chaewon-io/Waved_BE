package com.senity.waved.domain.review.service;

import com.senity.waved.domain.challenge.entity.Challenge;
import com.senity.waved.domain.challenge.service.ChallengeUtil;
import com.senity.waved.domain.challengeGroup.entity.ChallengeGroup;
import com.senity.waved.domain.challengeGroup.exception.ChallengeGroupNotCompletedException;
import com.senity.waved.domain.challengeGroup.service.ChallengeGroupUtil;
import com.senity.waved.domain.member.entity.Member;
import com.senity.waved.domain.member.service.MemberUtil;
import com.senity.waved.domain.myChallenge.entity.MyChallenge;
import com.senity.waved.domain.myChallenge.service.MyChallengeUtil;
import com.senity.waved.domain.review.entity.Review;
import com.senity.waved.domain.review.exception.AlreadyReviewedException;
import com.senity.waved.domain.review.exception.ReviewNotFoundException;
import com.senity.waved.domain.review.exception.UnauthorizedReviewAccessException;
import com.senity.waved.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    private final MemberUtil memberUtil;
    private final ChallengeUtil challengeUtil;
    private final ChallengeGroupUtil challengeGroupUtil;
    private final MyChallengeUtil myChallengeUtil;

    @Override
    @Transactional
    public void createChallengeReview(String email, Long myChallengeId, String content) {
        Member member = memberUtil.getByEmail(email);
        MyChallenge myChallenge = myChallengeUtil.getById(myChallengeId);
        checkReviewExist(myChallengeId);

        ChallengeGroup challengeGroup = challengeGroupUtil.getById(myChallenge.getChallengeGroupId());
        Challenge challenge = challengeUtil.getById(challengeGroup.getChallengeId());
        validateDate(challengeGroup);

        Review newReview = Review.of(content, member.getId(), challenge.getId(), challengeGroup.getGroupTitle());
        myChallenge.updateIsReviewed();
        reviewRepository.save(newReview);
    }

    @Override
    @Transactional(readOnly = true)
    public String getReviewContentForEdit(String email, Long reviewId) {
        Review review = getReviewAndCheckPermission(email, reviewId, "리뷰 작성자만 수정 가능합니다.");
        return review.getContent();
    }

    @Override
    @Transactional
    public void editChallengeReview(String email, Long reviewId, String content) {
        Review review = getReviewAndCheckPermission(email, reviewId, "리뷰 작성자만 수정 가능합니다.");
        review.updateContent(content);
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteChallengeReview(String email, Long reviewId) {
        Review review = getReviewAndCheckPermission(email, reviewId, "리뷰 작성자만 삭제 가능합니다.");
        reviewRepository.delete(review);

    }

    private Review getReviewAndCheckPermission(String email, Long reviewId, String errMsg) {
        Member member = memberUtil.getByEmail(email);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("해당 리뷰를 찾을 수 없습니다."));

        if (!review.getMemberId().equals(member.getId())) {
            throw new UnauthorizedReviewAccessException(errMsg);
        }
        return review;
    }

    private void checkReviewExist(Long myChallengeId) {
        MyChallenge myChallenge = myChallengeUtil.getById(myChallengeId);
        if (myChallenge.getIsReviewed()) {
            throw new AlreadyReviewedException("해당 챌린지에 이미 리뷰를 남기셨습니다.");
        }
    }

    private void validateDate(ChallengeGroup group) {
        if (group.getEndDate().isAfter(ZonedDateTime.now())) {
            throw new ChallengeGroupNotCompletedException("종료된 챌린지 그룹에 대해서만 리뷰 작성 가능합니다.");
        }
    }
}