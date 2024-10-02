package com.potatocake.everymoment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Table(name = "likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
