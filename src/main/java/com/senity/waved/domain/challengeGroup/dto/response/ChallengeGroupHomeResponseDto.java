package com.senity.waved.domain.challengeGroup.dto.response;

import com.senity.waved.domain.challenge.entity.VerificationType;
import com.senity.waved.domain.challengeGroup.entity.ChallengeGroup;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeGroupHomeResponseDto {

    private String groupTitle;
    private VerificationType verificationType;
    private Boolean isFree;
    private Long participantCount;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private Long challengeGroupId;  // group 상세페이지 호출을 위한 id값
    private String imageUrl;

    public static ChallengeGroupHomeResponseDto of(ChallengeGroup group, VerificationType verificationType, Boolean isFree) {
        String imageUrl = String.format("https://wavedstorage.blob.core.windows.net/blob/challenge_%s_home.png", group.getChallengeId());
        return ChallengeGroupHomeResponseDto.builder()
                .groupTitle(group.getGroupTitle())
                .verificationType(verificationType)
                .isFree(isFree)
                .participantCount(group.getParticipantCount())
                .startDate(group.getStartDate())
                .endDate(group.getEndDate())
                .challengeGroupId(group.getId())
                .imageUrl(imageUrl)
                .build();
    }
}