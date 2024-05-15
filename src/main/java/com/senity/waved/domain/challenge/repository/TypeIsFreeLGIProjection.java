package com.senity.waved.domain.challenge.repository;

import com.senity.waved.domain.challenge.entity.VerificationType;

public interface TypeIsFreeLGIProjection {
    VerificationType getVerificationType();
    Boolean getIsFree();
    Long getLatestGroupIndex();
}