package com.senity.waved.domain.quiz.controller;

import com.senity.waved.domain.quiz.dto.response.QuizResponseDto;
import com.senity.waved.domain.quiz.entity.Quiz;
import com.senity.waved.domain.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/quiz")
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/{challengeGroupId}")
    public QuizResponseDto getTodaysQuiz(@PathVariable("challengeGroupId") Long challengeGroupId) {
        Quiz quiz = quizService.getTodaysQuiz(challengeGroupId);
        ZonedDateTime plusDate = quiz.getDate();
        return QuizResponseDto.of(plusDate, quiz.getQuestion());
    }

    @GetMapping("/{challengeGroupId}/dates")
    public QuizResponseDto getQuizByDate(
            @PathVariable("challengeGroupId") Long challengeGroupId,
            @RequestParam("quizDate") Timestamp quizDate
    ) {
        Quiz quiz = quizService.getQuizByDate(challengeGroupId, quizDate);
        ZonedDateTime plusDate = quiz.getDate();
        return QuizResponseDto.of(plusDate, quiz.getQuestion());
    }
}