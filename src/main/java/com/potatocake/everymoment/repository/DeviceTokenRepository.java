package com.potatocake.everymoment.repository;

import com.potatocake.everymoment.entity.DeviceToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findAllByMemberId(Long memberId);

    Optional<DeviceToken> findByMemberIdAndDeviceId(Long memberId, String deviceId);

    void deleteByMemberIdAndDeviceId(Long memberId, String deviceId);

}
