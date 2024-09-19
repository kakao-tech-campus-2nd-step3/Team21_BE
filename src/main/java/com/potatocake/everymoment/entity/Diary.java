package com.potatocake.everymoment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_member_id"), nullable = false)
    @Column(name = "member_id", columnDefinition = "bigint", nullable = false)
    private Long memberId;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    //point는 mysql연결 뒤에
    @Column(name = "location_point", columnDefinition = "varchar(250)", nullable = false)
    private String locationPoint;  // Java에서는 문자열로 처리

    @Column(name = "location_name", columnDefinition = "varchar(50)", nullable = false)
    private String locationName;

    @Column(name = "address", columnDefinition = "varchar(50)", nullable = false)
    private String address;

    @Column(name = "emoji", columnDefinition = "text")
    private String emoji;

    @Column(name = "is_bookmark", nullable = false)
    @Builder.Default
    private boolean isBookmark = false;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private boolean isPublic = false;

    @CreationTimestamp
    @Column(name = "create_at", columnDefinition = "datetime(6)", updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "modify_at", columnDefinition = "datetime(6)")
    private LocalDateTime modifyAt;
}
