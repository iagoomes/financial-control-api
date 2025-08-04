package br.com.iagoomes.financialcontrol.app.service;

import br.com.iagoomes.financialcontrol.domain.entity.Category;
import br.com.iagoomes.financialcontrol.domain.usecase.CategorizeTransactionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Application Service for Category operations
 * Calls Use Cases and handles application logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategorizeTransactionUseCase categorizeTransactionUseCase;

    /**
     * Auto-categorize transaction based on title and amount
     */
    public Optional<Category> categorizeTransaction(String title, BigDecimal amount) {
        log.debug("Categorizing transaction: {}", title);

        return categorizeTransactionUseCase.execute(title, amount);
    }
}