package com.senity.waved.domain.verification.entity;

import com.senity.waved.common.BaseEntity;
import com.senity.waved.domain.challenge.entity.VerificationType;

import com.senity.waved.domain.challengeGroup.entity.ChallengeGroup;
import com.senity.waved.domain.member.entity.Member;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class Verification extends BaseEntity {

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "link", columnDefinition = "TEXT")
    private String link;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_type")
    private VerificationType verificationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_group_id")
    private ChallengeGroup challengeGroup;

    public static Verification createGithubVerification(Member member, ChallengeGroup challengeGroup, boolean hasCommitsToday) {
        return Verification.builder()
                .content(String.valueOf(hasCommitsToday))
                .member(member)
                .challengeGroup(challengeGroup)
                .verificationType(VerificationType.GITHUB)
                .build();
    }
}