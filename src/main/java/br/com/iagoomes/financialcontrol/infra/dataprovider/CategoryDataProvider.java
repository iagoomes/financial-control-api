package br.com.iagoomes.financialcontrol.infra.dataprovider;

import br.com.iagoomes.financialcontrol.domain.entity.Category;
import br.com.iagoomes.financialcontrol.domain.CategoryProvider;
import br.com.iagoomes.financialcontrol.domain.mapper.CategoryMapper;
import br.com.iagoomes.financialcontrol.infra.repository.CategoryDataRepository;
import br.com.iagoomes.financialcontrol.infra.repository.entity.CategoryData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of CategoryProvider using JPA Repository
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryDataProvider implements CategoryProvider {

    private final CategoryDataRepository categoryDataRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public Category save(Category category) {
        log.debug("Saving category: {}", category.getName());

        CategoryData categoryData = categoryMapper.toCategoryData(category);
        CategoryData savedData = categoryDataRepository.save(categoryData);

        Category savedCategory = categoryMapper.toCategoryDomain(savedData);
        log.info("Successfully saved category: {} with ID: {}", savedCategory.getName(), savedCategory.getId());

        return savedCategory;
    }

    @Override
    public Optional<Category> findById(String id) {
        log.debug("Finding category by ID: {}", id);

        return categoryDataRepository.findById(id)
                .map(categoryMapper::toCategoryDomain);
    }

    @Override
    public Optional<Category> findByName(String name) {
        log.debug("Finding category by name: {}", name);

        return categoryDataRepository.findByNameIgnoreCase(name)
                .map(categoryMapper::toCategoryDomain);
    }

    @Override
    public List<Category> findAll() {
        log.debug("Finding all categories");

        return categoryDataRepository.findAllByOrderByNameAsc()
                .stream()
                .map(categoryMapper::toCategoryDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findByParentCategoryId(String parentCategoryId) {
        log.debug("Finding categories by parent ID: {}", parentCategoryId);

        return categoryDataRepository.findByParentCategoryIdOrderByNameAsc(parentCategoryId)
                .stream()
                .map(categoryMapper::toCategoryDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findRootCategories() {
        log.debug("Finding root categories");

        return categoryDataRepository.findByParentCategoryIdIsNullOrderByNameAsc()
                .stream()
                .map(categoryMapper::toCategoryDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        log.info("Deleting category with ID: {}", id);

        categoryDataRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        log.debug("Checking if category exists by name: {}", name);

        return categoryDataRepository.existsByNameIgnoreCase(name);
    }
}