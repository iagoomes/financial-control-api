package br.com.iagoomes.financialcontrol.app.service;

import br.com.iagoomes.financialcontrol.app.mapper.AppMapper;
import br.com.iagoomes.financialcontrol.domain.entity.Category;
import br.com.iagoomes.financialcontrol.domain.usecase.CategorizeTransactionUseCase;
import br.com.iagoomes.financialcontrol.domain.usecase.ListCategoriesUseCase;
import br.com.iagoomes.financialcontrol.model.CategoryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Application Service for Category operations
 * Calls Use Cases and handles application logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final AppMapper appMapper;
    private final ListCategoriesUseCase listCategoriesUseCase;

    public List<CategoryDTO> listCategories() {
        return listCategoriesUseCase.execute().stream().map(appMapper::toCategoryDTO).toList();
    }
}