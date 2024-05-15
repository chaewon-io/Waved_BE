package com.senity.waved.domain.challenge.mapper;

import com.senity.waved.domain.challenge.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeMapper {

    private final ChallengeRepository challengeRepository;


}