package br.com.iagoomes.financialcontrol.domain.usecase;

import br.com.iagoomes.financialcontrol.domain.CategoryProvider;
import br.com.iagoomes.financialcontrol.domain.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListCategoriesUseCase {

    private final CategoryProvider categoryProvider;

    /**
     * List all categories
     *
     * @return List of Category
     */
    public List<Category> execute() {
        return categoryProvider.findAll();
    }
}
