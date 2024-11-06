package com.potatocake.everymoment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.potatocake.everymoment.entity.FriendRequest;
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
class FriendRequestRepositoryTest {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("친구 요청이 성공적으로 저장된다.")
    void should_SaveFriendRequest_When_ValidEntity() {
        // given
        Member sender = createAndSaveMember("sender", 123L);
        Member receiver = createAndSaveMember("receiver", 124L);

        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .build();

        // when
        FriendRequest savedRequest = friendRequestRepository.save(request);

        // then
        assertThat(savedRequest.getId()).isNotNull();
        assertThat(savedRequest.getSender()).isEqualTo(sender);
        assertThat(savedRequest.getReceiver()).isEqualTo(receiver);
    }

    @Test
    @DisplayName("사용자의 받은 친구 요청 목록이 성공적으로 조회된다.")
    void should_FindReceivedRequests_When_ValidReceiver() {
        // given
        Member receiver = createAndSaveMember("receiver", 123L);
        Member sender1 = createAndSaveMember("sender1", 124L);
        Member sender2 = createAndSaveMember("sender2", 125L);

        friendRequestRepository.saveAll(List.of(
                FriendRequest.builder()
                        .sender(sender1)
                        .receiver(receiver)
                        .build(),
                FriendRequest.builder()
                        .sender(sender2)
                        .receiver(receiver)
                        .build()
        ));

        // when
        Window<FriendRequest> requests = friendRequestRepository.findByReceiverId(
                receiver.getId(),
                ScrollPosition.offset(),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(requests.getContent()).hasSize(2);
        assertThat(requests.getContent())
                .extracting(request -> request.getSender().getNickname())
                .containsExactlyInAnyOrder("sender1", "sender2");
    }

    @Test
    @DisplayName("이미 존재하는 친구 요청인지 확인된다.")
    void should_CheckExistence_When_CheckingRequest() {
        // given
        Member sender = createAndSaveMember("sender", 123L);
        Member receiver = createAndSaveMember("receiver", 124L);

        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .build();
        friendRequestRepository.save(request);

        // when & then
        assertThat(friendRequestRepository.existsBySenderIdAndReceiverId(sender.getId(), receiver.getId()))
                .isTrue();
        assertThat(friendRequestRepository.existsBySenderIdAndReceiverId(receiver.getId(), sender.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("특정 친구 요청이 성공적으로 조회된다.")
    void should_FindRequest_When_ValidSenderAndReceiver() {
        // given
        Member sender = createAndSaveMember("sender", 123L);
        Member receiver = createAndSaveMember("receiver", 124L);

        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .build();
        friendRequestRepository.save(request);

        // when
        Optional<FriendRequest> foundRequest = friendRequestRepository
                .findBySenderIdAndReceiverId(sender.getId(), receiver.getId());

        // then
        assertThat(foundRequest).isPresent();
        assertThat(foundRequest.get().getSender()).isEqualTo(sender);
        assertThat(foundRequest.get().getReceiver()).isEqualTo(receiver);
    }

    @Test
    @DisplayName("친구 요청이 성공적으로 삭제된다.")
    void should_DeleteRequest_When_ValidEntity() {
        // given
        Member sender = createAndSaveMember("sender", 123L);
        Member receiver = createAndSaveMember("receiver", 124L);

        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .build();
        FriendRequest savedRequest = friendRequestRepository.save(request);

        // when
        friendRequestRepository.delete(savedRequest);

        // then
        Optional<FriendRequest> deletedRequest = friendRequestRepository.findById(savedRequest.getId());
        assertThat(deletedRequest).isEmpty();
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
