package com.potatocake.everymoment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.potatocake.everymoment.entity.Member;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원이 성공적으로 저장된다.")
    void should_SaveMember_When_ValidEntity() {
        // given
        Member member = Member.builder()
                .number(1234L)
                .nickname("testUser")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();

        // when
        Member savedMember = memberRepository.save(member);

        // then
        assertThat(savedMember.getId()).isNotNull();
        assertThat(savedMember.getNumber()).isEqualTo(1234L);
        assertThat(savedMember.getNickname()).isEqualTo("testUser");
        assertThat(savedMember.getProfileImageUrl()).isEqualTo("https://example.com/profile.jpg");
    }

    @Test
    @DisplayName("회원 번호로 회원이 성공적으로 조회된다.")
    void should_FindMember_When_SearchingByNumber() {
        // given
        Member member = Member.builder()
                .number(1234L)
                .nickname("testUser")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();
        memberRepository.save(member);

        // when
        Optional<Member> foundMember = memberRepository.findByNumber(1234L);

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getNickname()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("닉네임으로 회원이 성공적으로 검색된다.")
    void should_FindMembers_When_SearchingByNickname() {
        // given
        Member member1 = Member.builder()
                .number(1234L)
                .nickname("john")
                .profileImageUrl("https://example.com/profile1.jpg")
                .build();
        Member member2 = Member.builder()
                .number(5678L)
                .nickname("johnny")
                .profileImageUrl("https://example.com/profile2.jpg")
                .build();
        Member member3 = Member.builder()
                .number(9012L)
                .nickname("peter")
                .profileImageUrl("https://example.com/profile3.jpg")
                .build();

        memberRepository.saveAll(List.of(member1, member2, member3));

        // when
        Window<Member> result = memberRepository.findByNicknameContaining(
                "john",
                ScrollPosition.offset(),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).hasSize(2)
                .extracting("nickname")
                .containsExactlyInAnyOrder("john", "johnny");
    }

    @Test
    @DisplayName("회원 번호 존재 여부가 성공적으로 확인된다.")
    void should_CheckExistence_When_CheckingNumber() {
        // given
        Member member = Member.builder()
                .number(1234L)
                .nickname("testUser")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();
        memberRepository.save(member);

        // when & then
        assertThat(memberRepository.existsByNumber(1234L)).isTrue();
        assertThat(memberRepository.existsByNumber(5678L)).isFalse();
    }

    @Test
    @DisplayName("다음 익명 회원 번호가 성공적으로 생성된다.")
    void should_GenerateNextNumber_When_CreatingAnonymous() {
        // given
        Member member1 = Member.builder()
                .number(-1L)
                .nickname("anonymous1")
                .profileImageUrl("https://example.com/profile1.jpg")
                .build();
        Member member2 = Member.builder()
                .number(-2L)
                .nickname("anonymous2")
                .profileImageUrl("https://example.com/profile2.jpg")
                .build();

        memberRepository.saveAll(List.of(member1, member2));

        // when
        Long nextNumber = memberRepository.findNextAnonymousNumber();

        // then
        assertThat(nextNumber).isEqualTo(-3L);
    }

    @Test
    @DisplayName("회원이 성공적으로 삭제된다.")
    void should_DeleteMember_When_ValidEntity() {
        // given
        Member member = Member.builder()
                .number(1234L)
                .nickname("testUser")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();
        Member savedMember = memberRepository.save(member);

        // when
        memberRepository.delete(savedMember);

        // then
        Optional<Member> deletedMember = memberRepository.findById(savedMember.getId());
        assertThat(deletedMember).isEmpty();
    }

    @Test
    @DisplayName("회원 정보가 성공적으로 업데이트된다.")
    void should_UpdateMember_When_ValidInput() {
        // given
        Member member = Member.builder()
                .number(1234L)
                .nickname("oldNickname")
                .profileImageUrl("https://example.com/old.jpg")
                .build();
        memberRepository.save(member);

        // when
        member.update("newNickname", "https://example.com/new.jpg");

        // then
        Member updatedMember = memberRepository.findByNumber(1234L).orElseThrow();
        assertThat(updatedMember.getNickname()).isEqualTo("newNickname");
        assertThat(updatedMember.getProfileImageUrl()).isEqualTo("https://example.com/new.jpg");
    }

}
