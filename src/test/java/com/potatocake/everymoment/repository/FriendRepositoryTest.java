package com.potatocake.everymoment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.potatocake.everymoment.entity.Friend;
import com.potatocake.everymoment.entity.Member;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@DataJpaTest
class FriendRepositoryTest {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("친구 관계가 성공적으로 저장된다.")
    void should_SaveFriend_When_ValidEntity() {
        // given
        Member member = createAndSaveMember("user", 123L);
        Member friend = createAndSaveMember("friend", 124L);

        Friend friendship = Friend.builder()
                .member(member)
                .friend(friend)
                .build();

        // when
        Friend savedFriendship = friendRepository.save(friendship);

        // then
        assertThat(savedFriendship.getId()).isNotNull();
        assertThat(savedFriendship.getMember()).isEqualTo(member);
        assertThat(savedFriendship.getFriend()).isEqualTo(friend);
    }

    @Test
    @DisplayName("회원의 모든 친구가 조회된다.")
    void should_FindFriends_When_ValidMember() {
        // given
        Member member = createAndSaveMember("user", 123L);
        Member friend1 = createAndSaveMember("friend1", 124L);
        Member friend2 = createAndSaveMember("friend2", 125L);

        friendRepository.save(Friend.builder()
                .member(member)
                .friend(friend1)
                .build());
        friendRepository.save(Friend.builder()
                .member(member)
                .friend(friend2)
                .build());

        // when
        List<Friend> friends = friendRepository.findFriendsByMember(member);

        // then
        assertThat(friends).hasSize(2);
        assertThat(friends).extracting(friend -> friend.getFriend().getNickname())
                .containsExactlyInAnyOrder("friend1", "friend2");
    }

    @Test
    @DisplayName("특정 친구 관계가 존재하는지 확인된다.")
    void should_CheckExistence_When_CheckingFriendship() {
        // given
        Member member = createAndSaveMember("user", 123L);
        Member friend = createAndSaveMember("friend", 124L);

        friendRepository.save(Friend.builder()
                .member(member)
                .friend(friend)
                .build());

        // when & then
        assertThat(friendRepository.existsByMemberIdAndFriendId(member.getId(), friend.getId()))
                .isTrue();
        assertThat(friendRepository.existsByMemberIdAndFriendId(member.getId(), 999L))
                .isFalse();
    }

    @Test
    @DisplayName("특정 친구 관계가 성공적으로 조회된다.")
    void should_FindFriendship_When_ValidMemberAndFriend() {
        // given
        Member member = createAndSaveMember("user", 123L);
        Member friend = createAndSaveMember("friend", 124L);

        Friend friendship = Friend.builder()
                .member(member)
                .friend(friend)
                .build();
        friendRepository.save(friendship);

        // when
        Optional<Friend> foundFriendship = friendRepository.findByMemberAndFriend(member, friend);

        // then
        assertThat(foundFriendship).isPresent();
        assertThat(foundFriendship.get().getMember()).isEqualTo(member);
        assertThat(foundFriendship.get().getFriend()).isEqualTo(friend);
    }

    @Test
    @DisplayName("존재하지 않는 친구 관계 조회시 빈 결과가 반환된다.")
    void should_ReturnEmpty_When_FriendshipNotFound() {
        // given
        Member member = createAndSaveMember("user", 123L);
        Member notFriend = createAndSaveMember("notFriend", 124L);

        // when
        Optional<Friend> foundFriendship = friendRepository.findByMemberAndFriend(member, notFriend);

        // then
        assertThat(foundFriendship).isEmpty();
    }

    @Test
    @DisplayName("친구 관계가 성공적으로 삭제된다.")
    void should_DeleteFriendship_When_ValidEntity() {
        // given
        Member member = createAndSaveMember("user", 123L);
        Member friend = createAndSaveMember("friend", 124L);

        Friend friendship = Friend.builder()
                .member(member)
                .friend(friend)
                .build();
        Friend savedFriendship = friendRepository.save(friendship);

        // when
        friendRepository.delete(savedFriendship);

        // then
        Optional<Friend> deletedFriendship = friendRepository.findByMemberAndFriend(member, friend);
        assertThat(deletedFriendship).isEmpty();
    }

    @Test
    @DisplayName("친구 관계가 양방향으로 생성된다.")
    void should_CreateBidirectionalFriendship_When_AddingFriend() {
        // given
        Member member1 = createAndSaveMember("user1", 123L);
        Member member2 = createAndSaveMember("user2", 124L);

        Friend friendship1 = Friend.builder()
                .member(member1)
                .friend(member2)
                .build();
        Friend friendship2 = Friend.builder()
                .member(member2)
                .friend(member1)
                .build();

        // when
        friendRepository.saveAll(List.of(friendship1, friendship2));

        // then
        assertThat(friendRepository.findByMemberAndFriend(member1, member2)).isPresent();
        assertThat(friendRepository.findByMemberAndFriend(member2, member1)).isPresent();
    }

    @Test
    @DisplayName("닉네임으로 친구를 검색할 수 있다.")
    void should_FindFriends_When_SearchingByNickname() {
        // given
        Member member = createAndSaveMember("user", 123L);
        Member friend1 = createAndSaveMember("john", 124L);
        Member friend2 = createAndSaveMember("johnny", 125L);
        Member friend3 = createAndSaveMember("peter", 126L);

        friendRepository.saveAll(List.of(
                Friend.builder().member(member).friend(friend1).build(),
                Friend.builder().member(member).friend(friend2).build(),
                Friend.builder().member(member).friend(friend3).build()
        ));

        // when
        Specification<Friend> spec = (root, query, builder) -> {
            return builder.and(
                    builder.equal(root.get("member"), member),
                    builder.like(root.get("friend").get("nickname"), "%john%")
            );
        };

        Page<Friend> searchResult = friendRepository.findAll(
                spec,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(searchResult.getContent())
                .hasSize(2)
                .extracting(friend -> friend.getFriend().getNickname())
                .containsExactlyInAnyOrder("john", "johnny");
    }

    @Test
    @DisplayName("페이징이 성공적으로 동작한다.")
    void should_ReturnPagedResult_When_UsingPagination() {
        // given
        Member member = createAndSaveMember("user", 123L);
        List<Member> friends = List.of(
                createAndSaveMember("friend1", 124L),
                createAndSaveMember("friend2", 125L),
                createAndSaveMember("friend3", 126L),
                createAndSaveMember("friend4", 127L),
                createAndSaveMember("friend5", 128L)
        );

        List<Friend> friendships = friends.stream()
                .map(friend -> Friend.builder()
                        .member(member)
                        .friend(friend)
                        .build())
                .toList();

        friendRepository.saveAll(friendships);

        // when
        Page<Friend> firstPage = friendRepository.findAll(
                (root, query, builder) -> builder.equal(root.get("member"), member),
                PageRequest.of(0, 2)
        );

        Page<Friend> secondPage = friendRepository.findAll(
                (root, query, builder) -> builder.equal(root.get("member"), member),
                PageRequest.of(1, 2)
        );

        // then
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.hasNext()).isTrue();

        assertThat(secondPage.getContent()).hasSize(2);
        assertThat(secondPage.hasNext()).isTrue();
    }

    private Member createAndSaveMember(String nickname, Long number) {
        Member member = Member.builder()
                .number(number)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.jpg")
                .build();
        return memberRepository.save(member);
    }

}
