package com.senity.waved.domain.notification.service;

import com.senity.waved.domain.member.entity.Member;
import com.senity.waved.domain.member.repository.MemberRepository;
import com.senity.waved.domain.member.service.MemberUtil;
import com.senity.waved.domain.notification.entity.Notification;
import com.senity.waved.domain.notification.repository.NotificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    private final MemberUtil memberUtil;

    @Override
    public List<Notification> getNotifications(String email) {
        Member member = memberUtil.getByEmail(email);
        List<Notification> notifications = notificationRepository.findByMemberId(member.getId());

        member.updateNewEvent(false);
        memberRepository.flush();
        return notifications;
    }

    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}