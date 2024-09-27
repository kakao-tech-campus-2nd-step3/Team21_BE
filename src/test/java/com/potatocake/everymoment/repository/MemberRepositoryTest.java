package com.potatocake.everymoment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.potatocake.everymoment.entity.Member;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("이메일로 회원을 성공적으로 조회한다.")
    void should_FindMemberByEmail_When_EmailExists() {
        // given
        String email = "test@test.com";
        memberRepository.save(Member.builder()
                .email(email)
                .nickname("testUser")
                .build());

        // when
        Optional<Member> foundMember = memberRepository.findByEmail(email);

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("이메일이 존재하는지 확인한다.")
    void should_ReturnTrue_When_EmailExists() {
        // given
        String email = "test@test.com";
        memberRepository.save(Member.builder()
                .email(email)
                .nickname("testUser")
                .build());

        // when
        boolean exists = memberRepository.existsByEmail(email);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("닉네임과 이메일을 포함하여 스크롤 방식으로 회원 목록을 조회한다.")
    void should_FindByNicknameContainingAndEmailContaining_When_ValidScrollPosition() {
        // given
        String nickname = "test";
        String email = "test";
        for (int i = 1; i <= 15; i++) {
            memberRepository.save(Member.builder()
                    .nickname(nickname + i)
                    .email(email + i + "@test.com")
                    .build());
        }

        // when
        ScrollPosition scrollPosition = ScrollPosition.offset();
        PageRequest pageRequest = PageRequest.of(0, 10);
        Window<Member> window = memberRepository.findByNicknameContainingAndEmailContaining(nickname, email,
                scrollPosition, pageRequest);

        // then
        assertThat(window).isNotNull();
        assertThat(window.getContent().size()).isEqualTo(10);
        assertThat(window.hasNext()).isTrue(); // 총 15개 중 10개를 조회했으므로 다음 페이지 존재
    }

    @Test
    @DisplayName("닉네임과 이메일이 일치하지 않으면 빈 결과를 반환한다.")
    void should_ReturnEmpty_When_NoMatchingNicknameAndEmail() {
        // given
        ScrollPosition scrollPosition = ScrollPosition.offset();
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Window<Member> window = memberRepository.findByNicknameContainingAndEmailContaining("nonexistent",
                "nonexistent@test.com", scrollPosition, pageRequest);

        // then
        assertThat(window.getContent()).isEmpty();
    }

}
