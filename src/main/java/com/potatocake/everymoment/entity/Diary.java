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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class Diary extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Lob
    private String content;

    //point는 mysql연결 뒤에
    @Column(length = 250, nullable = false)
    private String locationPoint;  // Java에서는 문자열로 처리

    @Column(length = 50, nullable = false)
    private String locationName;

    @Column(length = 50, nullable = false)
    private String address;

    @Lob
    private String emoji;

    @Column(nullable = false)
    @Builder.Default
    private boolean isBookmark = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean isPublic = false;

    public void updateContent(String content) {
        if (content != null) {
            this.content = content;
        }
    }

    public void updateLocationPoint(String locationPoint) {
        if (locationPoint != null) {
            this.locationPoint = locationPoint;
        }
    }

    public void updateLocationName(String locationName) {
        if (locationName != null) {
            this.locationName = locationName;
        }
    }

    public void updateAddress(String address) {
        if (address != null) {
            this.address = address;
        }
    }

    public void updateEmoji(String emoji) {
        if (emoji != null) {
            this.emoji = emoji;
        }
    }

    public void updateBookmark(boolean isBookmark) {
        this.isBookmark = isBookmark;
    }

    public void updatePublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void toggleBookmark() {
        this.isBookmark = !this.isBookmark;
    }

    public void togglePublic() {
        this.isPublic = !this.isPublic;
    }

    public boolean checkOwner(Long memberId) {
        return this.member.getId().equals(memberId);
    }

}
