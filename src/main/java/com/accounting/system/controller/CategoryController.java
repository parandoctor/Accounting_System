package com.accounting.system.controller;

import com.accounting.system.model.entity.Category;
import com.accounting.system.model.vo.ResultVO;
import com.accounting.system.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================
 * 分类控制器
 * ============================================================
 *
 * 【为什么直接注入Repository而不是Service？】
 * 分类是纯字典数据，只有查询操作，不需要业务逻辑。
 * 直接在Controller注入Repository可以减少不必要的中间层。
 *
 * 如果将来分类需要增删改操作（管理员功能），再抽象到Service层。
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    /**
     * 获取所有分类（按sortOrder排序）
     */
    @GetMapping
    public ResultVO<List<Category>> listCategories() {
        return ResultVO.success(categoryRepository.findAllByOrderBySortOrder());
    }
}
