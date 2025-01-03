package com.potatocake.everymoment.service;

import com.potatocake.everymoment.dto.response.FriendDiarySimpleResponse;
import com.potatocake.everymoment.dto.response.FriendListResponse;
import com.potatocake.everymoment.dto.response.FriendProfileResponse;
import com.potatocake.everymoment.dto.response.OneFriendDiariesResponse;
import com.potatocake.everymoment.dto.response.ThumbnailResponse;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.File;
import com.potatocake.everymoment.entity.Friend;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.DiaryRepository;
import com.potatocake.everymoment.repository.FileRepository;
import com.potatocake.everymoment.repository.FriendRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final FileRepository fileRepository;

    //특정 친구 일기 조회
    @Transactional(readOnly = true)
    public OneFriendDiariesResponse OneFriendDiariesResponse(Long memberid, Long friendId, LocalDate date, int key,
                                                             int size) {
        Member currentMember = memberRepository.findById(memberid)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        //친구인지 확인
        Member friend = memberRepository.findById(friendId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
        friendRepository.findByMemberAndFriend(currentMember, friend)
                .orElseThrow(() -> new GlobalException(ErrorCode.FRIEND_NOT_FOUND));

        Pageable pageable = PageRequest.of(key, size, Sort.by(Sort.Direction.DESC, "createAt"));

        Page<Diary> diaries = diaryRepository.findAll(
                FriendDiarySpecification.filterDiaries(null, null, null, date, null, null)
                        .and((root, query, builder) -> builder.equal(root.get("member").get("id"), friendId)),
                pageable);

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
    public FriendListResponse getFriendList(Long memberIdFromController, String nickname, int key, int size) {
        Member currentMember = memberRepository.findById(memberIdFromController)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        Long memberId = currentMember.getId();

        Pageable pageable = PageRequest.of(key, size);

        Specification<Friend> spec = FriendSpecification.filterFriends(memberId, nickname)
                .and((root, query, builder) -> builder.equal(root.get("member").get("id"), memberId));

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

        Friend friendMine = friendRepository.findByMemberAndFriend(currentMember, friendMember)
                .orElseThrow(() -> new GlobalException(ErrorCode.FRIEND_NOT_FOUND));
        Friend friendFriends = friendRepository.findByMemberAndFriend(friendMember, currentMember)
                .orElseThrow(() -> new GlobalException(ErrorCode.FRIEND_NOT_FOUND));

        friendRepository.delete(friendMine);
        friendRepository.delete(friendFriends);
    }

    //다이어리 DTO 변환
    private FriendDiarySimpleResponse convertToFriendDiariesResponseDTO(Diary savedDiary) {
        //파일 찾음
        File thumbnailFile = fileRepository.findByDiaryAndOrder(savedDiary, 1);
        ThumbnailResponse thumbnailResponse = null;
        if (thumbnailFile != null) {
            thumbnailResponse = ThumbnailResponse.builder()
                    .id(thumbnailFile.getId())
                    .imageUrl(thumbnailFile.getImageUrl())
                    .build();
        }

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
    private FriendProfileResponse convertToFriendProfileResponseDTO(Friend friend) {
        Member friendMember = friend.getFriend();
        return FriendProfileResponse.builder()
                .id(friendMember.getId())
                .nickname(friendMember.getNickname())
                .profileImageUrl(friendMember.getProfileImageUrl())
                .build();
    }
}
