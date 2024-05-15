package com.senity.waved.domain.member.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubInfoDto {
    private String githubId;
    private String githubToken;

    public static GithubInfoDto from(String githubId, String githubToken) {
        return GithubInfoDto.builder()
                .githubId(githubId)
                .githubToken(githubToken)
                .build();
    }

    public static GithubInfoDto deleteGithubInfo() {
        return GithubInfoDto.builder()
                .githubId(null)
                .githubToken(null)
                .build();
    }
}