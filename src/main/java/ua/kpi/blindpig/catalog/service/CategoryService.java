package ua.kpi.blindpig.catalog.service;

import ua.kpi.blindpig.catalog.dto.*;
import java.util.List;

public interface CategoryService {
    CategoryDto create(SaveCategoryRequest request);
    CategoryDto getById(Long categoryId);
    List<CategoryDto> list();
    CategoryDto update(Long categoryId, SaveCategoryRequest request);
    /** Deletion with linked menu items is rejected (409). */
    void delete(Long categoryId);
}
