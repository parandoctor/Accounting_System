package com.accounting.system.repository;

import com.accounting.system.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByTypeOrderBySortOrder(Category.CategoryType type);
    List<Category> findAllByOrderBySortOrder();
}
