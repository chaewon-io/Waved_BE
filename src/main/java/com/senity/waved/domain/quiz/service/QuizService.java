package com.senity.waved.domain.quiz.service;

import com.senity.waved.domain.quiz.dto.response.QuizResponseDto;
import com.senity.waved.domain.quiz.entity.Quiz;

import java.sql.Timestamp;

public interface QuizService {
    Quiz getTodaysQuiz(Long challengeGroupId);
    Quiz getQuizByDate(Long challengeGroupId, Timestamp quizDate);
}