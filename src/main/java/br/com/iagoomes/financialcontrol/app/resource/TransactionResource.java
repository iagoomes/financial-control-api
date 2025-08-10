package br.com.iagoomes.financialcontrol.app.resource;

import br.com.iagoomes.financialcontrol.api.TransactionsApiDelegate;
import br.com.iagoomes.financialcontrol.app.service.TransactionService;
import br.com.iagoomes.financialcontrol.infra.exception.BusinessException;
import br.com.iagoomes.financialcontrol.model.CategoryUpdateRequest;
import br.com.iagoomes.financialcontrol.model.TransactionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Resource implementation for Transactions API operations
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionResource implements TransactionsApiDelegate {

    private final TransactionService transactionService;

    /**
     * Updates the category of a transaction
     */
    @Override
    public CompletableFuture<ResponseEntity<TransactionDTO>> categorizeTransaction(
            UUID transactionId,
            CategoryUpdateRequest categoryUpdateRequest) {

        log.info("Resource: Categorizing transaction {} with category {}",
                transactionId, categoryUpdateRequest.getCategoryId());

        return CompletableFuture.supplyAsync(() -> {
            try {
                TransactionDTO updatedTransaction = transactionService.updateTransactionCategory(
                        transactionId,
                        categoryUpdateRequest.getCategoryId()
                );

                return ResponseEntity.ok(updatedTransaction);

            } catch (BusinessException e) {
                log.warn("Business error categorizing transaction {}: {}", transactionId, e.getMessage());
                return ResponseEntity.notFound().build();

            } catch (Exception e) {
                log.error("Unexpected error categorizing transaction {}", transactionId, e);
                return ResponseEntity.internalServerError().build();
            }
        });
    }
}
