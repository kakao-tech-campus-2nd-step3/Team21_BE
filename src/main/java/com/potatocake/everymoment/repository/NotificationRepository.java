package com.potatocake.everymoment.repository;

import com.potatocake.everymoment.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
