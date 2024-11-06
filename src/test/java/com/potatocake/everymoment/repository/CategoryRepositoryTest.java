package com.potatocake.everymoment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.potatocake.everymoment.entity.Category;
import com.potatocake.everymoment.entity.Member;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member createAndSaveMember() {
        Member member = Member.builder()
                .number(1234L)
                .nickname("testUser")
                .profileImageUrl("http://example.com/image.jpg")
                .build();
        return memberRepository.save(member);
    }

    @Test
    @DisplayName("카테고리가 성공적으로 저장된다.")
    void should_SaveCategory_When_ValidEntity() {
        // given
        Member member = createAndSaveMember();
        Category category = Category.builder()
                .member(member)
                .categoryName("Test Category")
                .build();

        // when
        Category savedCategory = categoryRepository.save(category);

        // then
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getCategoryName()).isEqualTo("Test Category");
        assertThat(savedCategory.getMember()).isEqualTo(member);
    }

    @Test
    @DisplayName("회원의 카테고리 목록이 성공적으로 조회된다.")
    void should_FindCategories_When_FilteringByMemberId() {
        // given
        Member member = createAndSaveMember();

        Category category1 = Category.builder()
                .member(member)
                .categoryName("Category 1")
                .build();

        Category category2 = Category.builder()
                .member(member)
                .categoryName("Category 2")
                .build();

        categoryRepository.saveAll(List.of(category1, category2));

        // when
        List<Category> categories = categoryRepository.findByMemberId(member.getId());

        // then
        assertThat(categories).hasSize(2);
        assertThat(categories).extracting("categoryName")
                .containsExactlyInAnyOrder("Category 1", "Category 2");
    }

}
