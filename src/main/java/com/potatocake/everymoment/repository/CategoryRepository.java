package com.potatocake.everymoment.repository;

import com.potatocake.everymoment.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByMemberId(Long memberId);

}
