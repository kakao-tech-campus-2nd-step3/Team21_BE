package com.potatocake.everymoment.service;

import com.potatocake.everymoment.dto.response.FriendDiarySimpleResponse;
import com.potatocake.everymoment.dto.response.FriendListResponse;
import com.potatocake.everymoment.dto.response.FriendProfileResponse;
import com.potatocake.everymoment.dto.response.OneFriendDiariesResponse;
import com.potatocake.everymoment.dto.response.ThumbnailResponse;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.Friend;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.DiaryRepository;
import com.potatocake.everymoment.repository.FriendRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import com.potatocake.everymoment.security.MemberDetails;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;

    //특정 친구 일기 조회
    @Transactional(readOnly = true)
    public OneFriendDiariesResponse OneFriendDiariesResponse(Long memberid, Long friendId, LocalDate date, int key, int size) {
        Member currentMember = memberRepository.findById(memberid)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        //친구인지 확인
        Member friend = memberRepository.findById(friendId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
        friendRepository.findByMemberIdAndFriendId(currentMember, friend)
                .orElseThrow(() -> new GlobalException(ErrorCode.FRIEND_NOT_FOUND));

        Pageable pageable = PageRequest.of(key, size);

        Page<Diary> diaries = diaryRepository.findAll(DiarySpecification.filterDiaries(null, null, date, null, null, null)
                .and((root, query, builder) -> builder.equal(root.get("memberId").get("id"), friendId)), pageable);

        List<FriendDiarySimpleResponse> diaryList = diaries.getContent().stream()
                .map(this::convertToFriendDiariesResponseDTO)
                .collect(Collectors.toList());

        Integer nextPage = diaries.hasNext() ? key + 1 : null;

        return OneFriendDiariesResponse.builder()
                .diaries(diaryList)
                .next(nextPage)
                .build();
    }

    //내 친구 목록 조회
    @Transactional(readOnly = true)
    public FriendListResponse getFriendList(Long memberIdFromController, String nickname, String email, int key, int size) {
        Member currentMember = memberRepository.findById(memberIdFromController)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
        Long memberId = currentMember.getId();

        Pageable pageable = PageRequest.of(key, size);

        Specification<Friend> spec = FriendSpecification.filterFriends(memberId, nickname, email)
                .and((root, query, builder) -> builder.equal(root.get("memberId").get("id"), memberId));

        Page<Friend> friends = friendRepository.findAll(spec, pageable);

        List<FriendProfileResponse> friendProfiles = friends.stream()
                .map(this::convertToFriendProfileResponseDTO)
                .collect(Collectors.toList());

        Integer nextPage = friends.hasNext() ? key + 1 : null;

        return FriendListResponse.builder()
                .friends(friendProfiles)
                .next(nextPage)
                .build();
    }

    // 친구 삭제
    public void deleteFriend(Long memberId, Long firendId) {
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
        Member friendMember = memberRepository.findById(firendId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        Friend friendMine = friendRepository.findByMemberIdAndFriendId(currentMember, friendMember)
                .orElseThrow(() -> new GlobalException(ErrorCode.FRIEND_NOT_FOUND));
        Friend friendFriends = friendRepository.findByMemberIdAndFriendId(friendMember, currentMember)
                .orElseThrow(() -> new GlobalException(ErrorCode.FRIEND_NOT_FOUND));

        friendRepository.delete(friendMine);
        friendRepository.delete(friendFriends);
    }

    // 친한 친구 설정(토글)
    public void toggleCloseFriend(Long memberId, Long friendId) {
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
        Member friendMember = memberRepository.findById(friendId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        Friend friend = friendRepository.findByMemberIdAndFriendId(currentMember, friendMember)
                .orElseThrow(() -> new GlobalException(ErrorCode.FRIEND_NOT_FOUND));

        friend.toggleIsClose();
    }

    //다이어리 DTO 변환
    private FriendDiarySimpleResponse convertToFriendDiariesResponseDTO(Diary savedDiary) {
        //파일 찾음
        ThumbnailResponse thumbnailResponse = ThumbnailResponse.builder()
                .id(1L)
                .imageUrl("image1.url")
                .build();

        return FriendDiarySimpleResponse.builder()
                .id(savedDiary.getId())
                .address(savedDiary.getAddress())
                .locationName(savedDiary.getLocationName())
                .isBookmark(savedDiary.isBookmark())
                .isPublic(savedDiary.isPublic())
                .emoji(savedDiary.getEmoji())
                .thumbnailResponse(thumbnailResponse)
                .content(savedDiary.getContent())
                .createAt(savedDiary.getCreateAt())
                .build();
    }

    //친구 프로필 DTO 변환
    private FriendProfileResponse convertToFriendProfileResponseDTO(Friend friend){
        Member friendMember = friend.getFriendId();
        return FriendProfileResponse.builder()
                .id(friendMember.getId())
                .nickname(friendMember.getNickname())
                .profileImageUrl(friendMember.getProfileImageUrl())
                .isClose(friend.isClose())
                .build();
    }
}
