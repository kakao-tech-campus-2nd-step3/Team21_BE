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

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_member_id"), nullable = false)
    @Column(name = "member_id", columnDefinition = "bigint", nullable = false)
    private Long memberId;

    @Column(name = "content", columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @Column(name = "type", columnDefinition = "varchar(50)", nullable = false)
    private String type;

    @Column(name = "target_id", columnDefinition = "bigint", nullable = false)
    private Long targetId;

    @CreationTimestamp
    @Column(name = "create_at", columnDefinition = "datetime(6)", updatable = false)
    private LocalDateTime createAt;
}
