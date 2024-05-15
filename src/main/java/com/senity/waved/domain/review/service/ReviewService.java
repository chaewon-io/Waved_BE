package com.senity.waved.domain.review.service;

import com.senity.waved.domain.review.entity.Review;

public interface ReviewService {
    void createChallengeReview(String email, Long myChallengeId, String content);
    void editChallengeReview(String email, Long reviewId, String content);
    void deleteChallengeReview(String email, Long reviewId);
    Review getReviewContentForEdit(String email, Long reviewId);
}