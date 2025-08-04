package br.com.iagoomes.financialcontrol.domain;

import br.com.iagoomes.financialcontrol.domain.entity.Category;

import java.util.List;
import java.util.Optional;

/**
 * Domain interface for Category data access
 */
public interface CategoryProvider {

    /**
     * Save a category
     */
    Category save(Category category);

    /**
     * Find category by ID
     */
    Optional<Category> findById(String id);

    /**
     * Find category by name
     */
    Optional<Category> findByName(String name);

    /**
     * Find all categories
     */
    List<Category> findAll();

    /**
     * Find categories by parent ID
     */
    List<Category> findByParentCategoryId(String parentCategoryId);

    /**
     * Find root categories (no parent)
     */
    List<Category> findRootCategories();

    /**
     * Delete category by ID
     */
    void deleteById(String id);

    boolean existsByName(String name);
}