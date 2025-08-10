package br.com.iagoomes.financialcontrol.domain.usecase;

import br.com.iagoomes.financialcontrol.domain.CategoryProvider;
import br.com.iagoomes.financialcontrol.domain.TransactionProvider;
import br.com.iagoomes.financialcontrol.domain.entity.Category;
import br.com.iagoomes.financialcontrol.domain.entity.Transaction;
import br.com.iagoomes.financialcontrol.infra.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Use case for updating transaction category
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateTransactionCategoryUseCase {

    private final TransactionProvider transactionProvider;
    private final CategoryProvider categoryProvider;

    /**
     * Updates the category of a transaction
     */
    public Transaction execute(String transactionId, String categoryId) {
        log.info("Updating transaction {} with category {}", transactionId, categoryId);

        // 1. Find transaction
        Optional<Transaction> transactionOpt = transactionProvider.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            throw new BusinessException("Transaction not found: " + transactionId);
        }

        // 2. Find category
        Optional<Category> categoryOpt = categoryProvider.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            throw new BusinessException("Category not found: " + categoryId);
        }

        Transaction transaction = transactionOpt.get();
        Category category = categoryOpt.get();

        // 3. Update transaction category
        transaction.setCategory(category);
        transaction.setConfidence(BigDecimal.ONE); // 100% confidence for manual categorization

        // 4. Save updated transaction
        Transaction updatedTransaction = transactionProvider.save(transaction);

        log.info("Successfully updated transaction {} with category '{}'",
                transactionId, category.getName());

        return updatedTransaction;
    }
}
