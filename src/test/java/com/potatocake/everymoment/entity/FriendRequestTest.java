package com.potatocake.everymoment.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FriendRequestTest {

    @Test
    @DisplayName("친구 요청이 성공적으로 생성된다.")
    void should_CreateFriendRequest_When_ValidInput() {
        // given
        Member sender = Member.builder()
                .id(1L)
                .nickname("sender")
                .build();
        Member receiver = Member.builder()
                .id(2L)
                .nickname("receiver")
                .build();

        // when
        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .build();

        // then
        assertThat(request.getSender()).isEqualTo(sender);
        assertThat(request.getReceiver()).isEqualTo(receiver);
    }

}
