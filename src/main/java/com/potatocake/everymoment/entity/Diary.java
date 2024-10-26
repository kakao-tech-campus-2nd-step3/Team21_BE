package com.potatocake.everymoment.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

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

    @Column(nullable = false)
    private Point locationPoint;

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

    @OneToMany(mappedBy = "diary", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<DiaryCategory> diaryCategories = new HashSet<>();

    public void updateContent(String content) {
        if (content != null) {
            this.content = content;
        }
    }

    public void updateLocationPoint(Point locationPoint) {
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
