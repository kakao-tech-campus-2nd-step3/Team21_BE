package com.potatocake.everymoment.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FriendTest {

    @DisplayName("친구 관계가 성공적으로 생성된다.")
    @Test
    void should_CreateFriendship_When_ValidInput() {
        // given
        Member member = Member.builder()
                .id(1L)
                .nickname("user")
                .build();
        Member friend = Member.builder()
                .id(2L)
                .nickname("friend")
                .build();

        // when
        Friend friendship = Friend.builder()
                .member(member)
                .friend(friend)
                .build();

        // then
        assertThat(friendship.getMember()).isEqualTo(member);
        assertThat(friendship.getFriend()).isEqualTo(friend);
    }

}
