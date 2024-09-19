package com.potatocake.everymoment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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

    //    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_member_id"), nullable = false)
    @Column(nullable = false)
    private Long memberId;

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
}
