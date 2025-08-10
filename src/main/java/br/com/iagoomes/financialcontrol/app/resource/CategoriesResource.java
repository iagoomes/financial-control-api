package br.com.iagoomes.financialcontrol.app.resource;

import br.com.iagoomes.financialcontrol.api.CategoriesApiDelegate;
import br.com.iagoomes.financialcontrol.app.service.CategoryService;
import br.com.iagoomes.financialcontrol.model.CategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class CategoriesResource implements CategoriesApiDelegate {

    private final CategoryService categoryService;

    @Override
    public CompletableFuture<ResponseEntity<List<CategoryDTO>>> listCategories() {
        return CompletableFuture.supplyAsync(() -> {
            List<CategoryDTO> categories = categoryService.listCategories();
            return ResponseEntity.ok(categories);
        });
    }
}
