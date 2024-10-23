package com.potatocake.everymoment.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    COMMENT("새로운 댓글", "%s님이 회원님의 일기에 댓글을 남겼습니다."),
    LIKE("새로운 좋아요", "%s님이 회원님의 일기를 좋아합니다."),
    FRIEND_REQUEST("새로운 친구 요청", "%s님이 친구 요청을 보냈습니다."),
    FRIEND_ACCEPT("친구 요청 수락", "%s님이 친구 요청을 수락했습니다."),
    AUTO_DIARY("새로운 장소", "현재 %s에 머무르고 있어요! 지금 기분은 어떠신가요?");

    private final String title;
    private final String messageFormat;

    public String formatMessage(String... args) {
        return String.format(messageFormat, (Object[]) args);
    }

}
