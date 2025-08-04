package br.com.iagoomes.financialcontrol.domain.mapper;

import br.com.iagoomes.financialcontrol.domain.entity.Category;
import br.com.iagoomes.financialcontrol.infra.repository.entity.CategoryData;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    /**
     * Converts domain model to data model
     */
    public CategoryData toCategoryData(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryData.builder()
                .id(category.getId())
                .name(category.getName())
                .color(category.getColor())
                .icon(category.getIcon())
                .parentCategoryId(category.getParentCategoryId())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    /**
     * Converts data model to domain model
     */
    public Category toCategoryDomain(CategoryData categoryData) {
        if (categoryData == null) {
            return null;
        }

        Category category = new Category();
        category.setId(categoryData.getId());
        category.setName(categoryData.getName());
        category.setColor(categoryData.getColor());
        category.setIcon(categoryData.getIcon());
        category.setParentCategoryId(categoryData.getParentCategoryId());
        category.setCreatedAt(categoryData.getCreatedAt());
        category.setUpdatedAt(categoryData.getUpdatedAt());

        return category;
    }
}
