package com.senity.waved.domain.paymentRecord.entity;

import com.senity.waved.common.BaseEntity;
import com.senity.waved.domain.member.entity.Member;
import com.senity.waved.domain.myChallenge.entity.MyChallenge;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PaymentRecord extends BaseEntity {

    @Column(name="payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "deposit")
    private Long deposit;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "group_title")
    private String groupTitle;

    @Column(name = "my_challenge_id")
    private Long myChallengeId;

    public static PaymentRecord of(PaymentStatus status, Member member, MyChallenge myChallenge, String groupTitle) {
        Long deposit = status.equals(PaymentStatus.APPLIED) ?
                myChallenge.getDeposit() * (-1) : status.equals(PaymentStatus.FAIL) ? 0 : myChallenge.getDeposit();

        return PaymentRecord.builder()
                .deposit(deposit)
                .paymentStatus(status)
                .member(member)
                .myChallengeId(myChallenge.getId())
                .groupTitle(groupTitle)
                .build();
    }
}

