package com.senity.waved.domain.quiz.service;

import com.senity.waved.domain.quiz.entity.Quiz;
import com.senity.waved.domain.quiz.exception.QuizNotFoundException;
import com.senity.waved.domain.quiz.repository.QuizRepository;
import com.senity.waved.domain.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final VerificationService verificationService;

    @Override
    public Quiz getTodaysQuiz(Long challengeGroupId) {
        verificationService.IsChallengeGroupTextType(challengeGroupId);
        ZonedDateTime today = ZonedDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS);
        return findQuizByDate(challengeGroupId, today);
    }

    @Override
    public Quiz getQuizByDate(Long challengeGroupId, Timestamp requestedQuizDate) {
        ZonedDateTime quizDate = requestedQuizDate.toInstant().atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS);

        verificationService.IsChallengeGroupTextType(challengeGroupId);
        return findQuizByDate(challengeGroupId, quizDate);
    }

    private Quiz findQuizByDate(Long challengeGroupId, ZonedDateTime date) {
        return quizRepository.findByChallengeGroupIdAndDate(challengeGroupId, date)
                .orElseThrow(() -> new QuizNotFoundException("퀴즈를 찾을 수 없습니다."));
    }
}