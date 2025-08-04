package br.com.iagoomes.financialcontrol.infra.repository;

import br.com.iagoomes.financialcontrol.infra.repository.entity.CategoryData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryDataRepository extends JpaRepository<CategoryData, String> {
    /**
     * Find category by name (case-insensitive)
     */
    Optional<CategoryData> findByNameIgnoreCase(String name);

    /**
     * Check if category exists by name (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find all categories ordered by name
     */
    List<CategoryData> findAllByOrderByNameAsc();

    /**
     * Find categories by parent category ID ordered by name
     */
    List<CategoryData> findByParentCategoryIdOrderByNameAsc(String parentCategoryId);

    /**
     * Find root categories (no parent) ordered by name
     */
    List<CategoryData> findByParentCategoryIdIsNullOrderByNameAsc();
}
