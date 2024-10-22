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
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("dev")
@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원 번호로 회원을 성공적으로 조회한다.")
    void should_FindMemberByNumber_When_NumberExists() {
        // given
        Long number = 1234L;
        memberRepository.save(Member.builder()
                .number(number)
                .nickname("testUser")
                .build());

        // when
        Optional<Member> foundMember = memberRepository.findByNumber(number);

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getNumber()).isEqualTo(number);
    }

    @Test
    @DisplayName("회원 번호가 존재하는지 확인한다.")
    void should_ReturnTrue_When_NumberExists() {
        // given
        Long number = 1234L;
        memberRepository.save(Member.builder()
                .number(number)
                .nickname("testUser")
                .build());

        // when
        boolean exists = memberRepository.existsByNumber(number);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("닉네임을 포함하여 스크롤 방식으로 회원 목록을 조회한다.")
    void should_FindByNicknameContaining_When_ValidScrollPosition() {
        // given
        String nickname = "test";
        Long number = 1234L;
        for (int i = 1; i <= 15; i++) {
            memberRepository.save(Member.builder()
                    .nickname(nickname + i)
                    .number(number + i)
                    .build());
        }

        // when
        ScrollPosition scrollPosition = ScrollPosition.offset();
        PageRequest pageRequest = PageRequest.of(0, 10);
        Window<Member> window = memberRepository.findByNicknameContaining(nickname, scrollPosition, pageRequest);

        // then
        assertThat(window).isNotNull();
        assertThat(window.getContent().size()).isEqualTo(10);
        assertThat(window.hasNext()).isTrue(); // 총 15개 중 10개를 조회했으므로 다음 페이지 존재
    }

    @Test
    @DisplayName("닉네임으로 검색 결과가 없다면 빈 결과를 반환한다.")
    void should_ReturnEmpty_When_NoMatchingNickname() {
        // given
        ScrollPosition scrollPosition = ScrollPosition.offset();
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Window<Member> window = memberRepository.findByNicknameContaining("nonexistent", scrollPosition, pageRequest);

        // then
        assertThat(window.getContent()).isEmpty();
    }

}
