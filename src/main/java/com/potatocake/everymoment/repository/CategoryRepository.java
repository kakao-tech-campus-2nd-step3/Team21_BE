package com.potatocake.everymoment.repository;

import com.potatocake.everymoment.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
