package com.senity.waved.domain.liked.service;

import com.senity.waved.domain.liked.entity.Liked;
import com.senity.waved.domain.liked.exception.DuplicationLikeException;
import com.senity.waved.domain.liked.repository.LikedRepository;
import com.senity.waved.domain.member.entity.Member;
import com.senity.waved.domain.member.exception.MemberNotFoundException;
import com.senity.waved.domain.member.repository.MemberRepository;
import com.senity.waved.domain.verification.entity.Verification;
import com.senity.waved.domain.verification.exception.VerificationNotFoundException;
import com.senity.waved.domain.verification.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikedServiceImpl implements LikedService {
    private final VerificationRepository verificationRepository;
    private final MemberRepository memberRepository;
    private final LikedRepository likedRepository;

    @Override
    public void addLikedToVerification(String email, Long verificationId) {

        Member member = getMemberByEmail(email);
        Verification verification = getVerificationById(verificationId);

        boolean hasAlreadyLiked = likedRepository.existsByMemberAndVerification(member, verification);

        if (hasAlreadyLiked) {
            throw new DuplicationLikeException("이미 좋아요를 누른 인증 내역 입니다.");
        }

        Liked like = Liked.builder()
                .verification(verification)
                .member(member)
                .build();

        likedRepository.save(like);
    }

    private Verification getVerificationById(Long id) {
        return verificationRepository.findById(id)
                .orElseThrow(() -> new VerificationNotFoundException("해당 인증내역을 찾을 수 없습니다."));
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.getMemberByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다."));
    }
}
