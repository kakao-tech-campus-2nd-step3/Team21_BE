package com.potatocake.everymoment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(
        name = "likes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"member_id", "diary_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Like {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Diary diary;

    @Builder
    public Like(Long id, Member member, Diary diary) {
        this.id = id;
        this.member = member;
        this.diary = diary;
    }

}
