package com.potatocake.everymoment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class File extends BaseTimeEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Diary diary;

    @Column(length = 2083, nullable = false)
    private String imageUrl;

    @Column(name = "\"order\"")
    private Integer order;

    @Builder
    public File(Long id, Diary diary, String imageUrl, Integer order) {
        this.id = id;
        this.diary = diary;
        this.imageUrl = imageUrl;
        this.order = order;
    }

}
