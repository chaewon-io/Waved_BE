package com.senity.waved.domain.quiz.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizResponseDto {
    private ZonedDateTime date;
    private String question;

    public static QuizResponseDto of(ZonedDateTime date, String question) {
        return QuizResponseDto.builder()
                .date(date)
                .question(question)
                .build();
    }
}