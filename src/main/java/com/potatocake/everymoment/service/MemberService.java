package com.potatocake.everymoment.service;

import static org.springframework.data.domain.Sort.Direction.ASC;

import com.potatocake.everymoment.dto.response.AnonymousLoginResponse;
import com.potatocake.everymoment.dto.response.FriendRequestStatus;
import com.potatocake.everymoment.dto.response.MemberDetailResponse;
import com.potatocake.everymoment.dto.response.MemberMyResponse;
import com.potatocake.everymoment.dto.response.MemberSearchResponse;
import com.potatocake.everymoment.dto.response.MemberSearchResultResponse;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.FriendRepository;
import com.potatocake.everymoment.repository.FriendRequestRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import com.potatocake.everymoment.util.JwtUtil;
import com.potatocake.everymoment.util.PagingUtil;
import com.potatocake.everymoment.util.S3FileUploader;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Transactional
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendRepository friendRepository;
    private final PagingUtil pagingUtil;
    private final S3FileUploader s3FileUploader;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public MemberSearchResponse searchMembers(String nickname, Long key, int size, Long currentMemberId) {
        Window<Member> window = fetchMemberWindow(nickname, key, size);
        List<MemberSearchResultResponse> members = convertToMemberResponses(window.getContent(), currentMemberId);
        Long nextKey = pagingUtil.getNextKey(window, Member::getId);

        return MemberSearchResponse.builder()
                .members(members)
                .next(nextKey)
                .build();
    }

    @Transactional(readOnly = true)
    public MemberDetailResponse getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberDetailResponse.builder()
                .id(member.getId())
                .profileImageUrl(member.getProfileImageUrl())
                .nickname(member.getNickname())
                .build();
    }

    @Transactional(readOnly = true)
    public MemberMyResponse getMemberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberMyResponse.builder()
                .id(member.getId())
                .profileImageUrl(member.getProfileImageUrl())
                .nickname(member.getNickname())
                .build();
    }

    public AnonymousLoginResponse anonymousLogin(Long memberNumber) {
        if (memberNumber != null) {
            // 기존 회원번호로 로그인 시도
            return memberRepository.findByNumber(memberNumber)
                    .map(member -> AnonymousLoginResponse.builder()
                            .token(jwtUtil.create(member.getId()))
                            .build())
                    .orElseGet(this::createAnonymousLoginResponse);
        }

        // 새로운 익명 회원 생성 및 응답
        return createAnonymousLoginResponse();
    }

    private AnonymousLoginResponse createAnonymousLoginResponse() {
        Member newMember = createAnonymousMember();
        return AnonymousLoginResponse.builder()
                .number(newMember.getNumber())
                .token(jwtUtil.create(newMember.getId()))
                .build();
    }

    private Member createAnonymousMember() {
        Long nextNumber = memberRepository.findNextAnonymousNumber();

        Member member = Member.builder()
                .nickname("Anonymous")
                .number(nextNumber)
                .build();

        return memberRepository.save(member);
    }

    public void updateMemberInfo(Long id, MultipartFile profileImage, String nickname) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        String profileImageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageUrl = s3FileUploader.uploadFile(profileImage);
        }

        member.update(nickname, profileImageUrl);
    }

    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        memberRepository.delete(member);
    }

    private Window<Member> fetchMemberWindow(String nickname, Long key, int size) {
        ScrollPosition scrollPosition = pagingUtil.createScrollPosition(key);
        Pageable pageable = pagingUtil.createPageable(size, ASC);

        String searchNickname = (nickname == null) ? "" : nickname;

        return memberRepository.findByNicknameContaining(searchNickname, scrollPosition, pageable);
    }

    private List<MemberSearchResultResponse> convertToMemberResponses(List<Member> members, Long currentMemberId) {
        return members.stream()
                .map(member -> MemberSearchResultResponse.builder()
                        .id(member.getId())
                        .profileImageUrl(member.getProfileImageUrl())
                        .nickname(member.getNickname())
                        .friendRequestStatus(getFriendRequestStatus(currentMemberId, member.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    private FriendRequestStatus getFriendRequestStatus(Long currentMemberId, Long targetMemberId) {
        if (currentMemberId.equals(targetMemberId)) {
            return FriendRequestStatus.SELF;
        }

        if (friendRepository.existsByMemberIdAndFriendId(currentMemberId, targetMemberId)) {
            return FriendRequestStatus.FRIEND;
        }

        return friendRequestRepository.findBySenderIdAndReceiverId(currentMemberId, targetMemberId)
                .map(request -> FriendRequestStatus.SENT)
                .orElseGet(() ->
                        friendRequestRepository.findBySenderIdAndReceiverId(targetMemberId, currentMemberId)
                                .map(request -> FriendRequestStatus.RECEIVED)
                                .orElse(FriendRequestStatus.NONE)
                );
    }

}
