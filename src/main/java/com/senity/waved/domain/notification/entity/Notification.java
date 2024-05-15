package com.senity.waved.domain.notification.entity;

import com.senity.waved.common.BaseEntity;
import com.senity.waved.domain.member.entity.Member;
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
public class Notification extends BaseEntity {

    @Column(name = "title")
    private String title;

    @Column(name = "message")
    private String message;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public static Notification of(Member member, String title, String message) {
        return Notification.builder()
                .member(member)
                .title(title)
                .message(message)
                .build();
    }
}
