package com.potatocake.everymoment.util;

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

    public Pageable createPageable(int size, Sort.Direction direction) {
        return PageRequest.of(0, size, Sort.by(direction, "id"));
    }

    public <T> Long getNextKey(Window<T> window, IdExtractor<? super T> idExtractor) {
        if (!window.hasNext() || window.getContent().isEmpty()) {
            return null;
        }
        return idExtractor.extractId(window.getContent().get(window.getContent().size() - 1));
    }

}
