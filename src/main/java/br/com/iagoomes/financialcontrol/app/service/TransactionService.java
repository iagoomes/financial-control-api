package br.com.iagoomes.financialcontrol.app.service;

import br.com.iagoomes.financialcontrol.app.mapper.AppMapper;
import br.com.iagoomes.financialcontrol.domain.entity.Transaction;
import br.com.iagoomes.financialcontrol.domain.usecase.UpdateTransactionCategoryUseCase;
import br.com.iagoomes.financialcontrol.model.TransactionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Application Service for Transaction operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final UpdateTransactionCategoryUseCase updateTransactionCategoryUseCase;
    private final AppMapper appMapper;

    /**
     * Updates the category of a transaction
     */
    public TransactionDTO updateTransactionCategory(UUID transactionId, UUID categoryId) {
        log.info("Service: Updating transaction {} with category {}", transactionId, categoryId);

        Transaction updatedTransaction = updateTransactionCategoryUseCase.execute(
                transactionId.toString(),
                categoryId.toString()
        );

        return appMapper.mapTransaction(updatedTransaction);
    }
}
