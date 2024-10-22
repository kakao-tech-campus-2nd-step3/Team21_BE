package com.potatocake.everymoment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Column(nullable = false)
    private String fcmToken;

    @Column(nullable = false)
    @Lob
    private String deviceId;

    @Builder
    public DeviceToken(Member member, String fcmToken, String deviceId) {
        this.member = member;
        this.fcmToken = fcmToken;
        this.deviceId = deviceId;
    }

    public void updateToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

}
