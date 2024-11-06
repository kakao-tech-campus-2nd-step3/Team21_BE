package com.potatocake.everymoment.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    @DisplayName("회원 정보가 성공적으로 생성된다.")
    void should_CreateMember_When_ValidInput() {
        // given
        Long id = 1L;
        Long number = 1234L;
        String nickname = "testUser";
        String profileImageUrl = "https://example.com/profile.jpg";

        // when
        Member member = Member.builder()
                .id(id)
                .number(number)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();

        // then
        assertThat(member.getId()).isEqualTo(id);
        assertThat(member.getNumber()).isEqualTo(number);
        assertThat(member.getNickname()).isEqualTo(nickname);
        assertThat(member.getProfileImageUrl()).isEqualTo(profileImageUrl);
        assertThat(member.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("회원 정보가 성공적으로 업데이트된다.")
    void should_UpdateInfo_When_NewValuesProvided() {
        // given
        Member member = Member.builder()
                .nickname("oldNickname")
                .profileImageUrl("https://example.com/old.jpg")
                .build();

        // when
        member.update("newNickname", "https://example.com/new.jpg");

        // then
        assertThat(member.getNickname()).isEqualTo("newNickname");
        assertThat(member.getProfileImageUrl()).isEqualTo("https://example.com/new.jpg");
    }

    @Test
    @DisplayName("닉네임만 업데이트된다.")
    void should_UpdateNickname_When_OnlyNicknameProvided() {
        // given
        Member member = Member.builder()
                .nickname("oldNickname")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();

        // when
        member.update("newNickname", null);

        // then
        assertThat(member.getNickname()).isEqualTo("newNickname");
        assertThat(member.getProfileImageUrl()).isEqualTo("https://example.com/profile.jpg");
    }

    @Test
    @DisplayName("프로필 이미지만 업데이트된다.")
    void should_UpdateProfileImage_When_OnlyProfileImageProvided() {
        // given
        Member member = Member.builder()
                .nickname("testUser")
                .profileImageUrl("https://example.com/old.jpg")
                .build();

        // when
        member.update(null, "https://example.com/new.jpg");

        // then
        assertThat(member.getNickname()).isEqualTo("testUser");
        assertThat(member.getProfileImageUrl()).isEqualTo("https://example.com/new.jpg");
    }

}
