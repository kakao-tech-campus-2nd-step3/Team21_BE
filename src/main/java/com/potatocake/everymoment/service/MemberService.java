package com.potatocake.everymoment.service;

import com.potatocake.everymoment.dto.response.MemberDetailResponse;
import com.potatocake.everymoment.dto.response.MemberResponse;
import com.potatocake.everymoment.dto.response.MemberSearchResponse;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.MemberRepository;
import com.potatocake.everymoment.util.PagingUtil;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PagingUtil pagingUtil;

    @Transactional(readOnly = true)
    public MemberSearchResponse searchMembers(String nickname, String email, Long key, int size) {
        Window<Member> window = fetchMemberWindow(nickname, email, key, size);
        List<MemberResponse> members = convertToMemberResponses(window.getContent());
        Long nextKey = pagingUtil.getNextKey(window);

        return MemberSearchResponse.builder()
                .members(members)
                .next(nextKey)
                .build();
    }

    private Window<Member> fetchMemberWindow(String nickname, String email, Long key, int size) {
        ScrollPosition scrollPosition = pagingUtil.createScrollPosition(key);
        Pageable pageable = pagingUtil.createPageable(size);

        String searchNickname = (nickname == null) ? "" : nickname;
        String searchEmail = (email == null) ? "" : email;

        return memberRepository.findByNicknameContainingAndEmailContaining(searchNickname, searchEmail, scrollPosition,
                pageable);
    }

    private List<MemberResponse> convertToMemberResponses(List<Member> members) {
        return members.stream()
                .map(member -> MemberResponse.builder()
                        .id(member.getId())
                        .profileImageUrl(member.getProfileImageUrl())
                        .nickname(member.getNickname())
                        .build())
                .collect(Collectors.toList());
    }

    public MemberDetailResponse getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberDetailResponse.builder()
                .id(member.getId())
                .profileImageUrl(member.getProfileImageUrl())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .build();
    }

}
