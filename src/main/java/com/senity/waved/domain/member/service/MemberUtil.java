package com.senity.waved.domain.member.service;

import com.senity.waved.domain.member.entity.Member;
import com.senity.waved.domain.member.exception.MemberNotFoundException;
import com.senity.waved.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberUtil {

    private MemberRepository memberRepository;

    public Member getById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("해당 멤버를 찾을 수 없습니다."));
    }

    public Member getByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("해당 멤버를 찾을 수 없습니다."));
    }
}