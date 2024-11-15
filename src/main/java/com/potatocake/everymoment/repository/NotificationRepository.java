package com.potatocake.everymoment.repository;

import com.potatocake.everymoment.entity.Notification;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByMemberId(Long memberId, Sort sort);

}
