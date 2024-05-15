package com.senity.waved.domain.liked.service;

import com.senity.waved.domain.liked.entity.Liked;
import com.senity.waved.domain.liked.exception.DuplicationLikeException;
import com.senity.waved.domain.liked.exception.LikeNotAuthorizedException;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikedServiceImpl implements LikedService {

    private final VerificationRepository verificationRepository;
    private final MemberRepository memberRepository;
    private final LikedRepository likedRepository;

    @Override
    @Transactional
    public void addLikedToVerification(String email, Long verificationId) {
        Member member = getMemberByEmail(email);
        Verification verification = getVerificationById(verificationId);
        checkLikedExistence(member, verification);

        Liked like = Liked.of(verification, member.getId());
        verification.addLikeToVerification(like);
        likedRepository.save(like);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countLikesToVerification(Long verificationId) {
        Verification verification = getVerificationById(verificationId);
        return likedRepository.countLikesByVerification(verification);
    }

    @Override
    @Transactional
    public void removeLikeFromVerification(String email, Long verificationId) {
        Member member = getMemberByEmail(email);
        Verification verification = getVerificationById(verificationId);
        Liked liked = getLikedByMemberIdAndVerification(member.getId(), verification);

        verification.removeLikeFromVerification(liked);
        likedRepository.delete(liked);
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("해당 멤버를 찾을 수 없습니다."));
    }

    private Verification getVerificationById(Long id) {
        return verificationRepository.findById(id)
                .orElseThrow(() -> new VerificationNotFoundException("해당 인증 내역을 찾을 수 없습니다."));
    }

    private Liked getLikedByMemberIdAndVerification(Long memberId, Verification verification) {
        return likedRepository.findByMemberIdAndVerification(memberId, verification)
                .orElseThrow(() -> new LikeNotAuthorizedException("해당 인증 내역에 좋아요를 누르지 않았습니다."));
    }

    private void checkLikedExistence(Member member, Verification verification) {
        boolean hasAlreadyLiked = likedRepository.existsByMemberIdAndVerification(member.getId(), verification);
        if (hasAlreadyLiked) {
            throw new DuplicationLikeException("이미 좋아요를 누른 인증 내역 입니다.");
        }
    }
}