package com.potatocake.everymoment.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.potatocake.everymoment.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IdExtractorTest {

    @Test
    @DisplayName("IdExtractor 가 성공적으로 ID를 추출한다.")
    void should_ExtractId_When_ValidInput() {
        // given
        Member member = Member.builder()
                .id(1L)
                .build();

        IdExtractor<Member> extractor = Member::getId;

        // when
        Long extractedId = extractor.extractId(member);

        // then
        assertThat(extractedId).isEqualTo(1L);
    }

}
