package com.potatocake.everymoment.util;

import com.potatocake.everymoment.entity.Member;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Component;

@Component
public class PagingUtil {

    public ScrollPosition createScrollPosition(Long key) {
        return key == null ? ScrollPosition.offset() : ScrollPosition.forward(Map.of("id", key));
    }

    public Pageable createPageable(int size) {
        return PageRequest.of(0, size, Sort.by(Sort.Direction.ASC, "id"));
    }

    public Long getNextKey(Window<?> window) {
        return window.hasNext()
                ? ((Member) window.getContent().get(window.getContent().size() - 1)).getId()
                : null;
    }

}
