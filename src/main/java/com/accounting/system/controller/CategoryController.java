package com.accounting.system.controller;

import com.accounting.system.model.entity.Category;
import com.accounting.system.model.vo.ResultVO;
import com.accounting.system.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResultVO<List<Category>> listCategories() {
        return ResultVO.success(categoryRepository.findAllByOrderBySortOrder());
    }
}
