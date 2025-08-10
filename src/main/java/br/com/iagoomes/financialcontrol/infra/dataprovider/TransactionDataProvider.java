package br.com.iagoomes.financialcontrol.infra.dataprovider;

import br.com.iagoomes.financialcontrol.domain.TransactionProvider;
import br.com.iagoomes.financialcontrol.domain.entity.Transaction;
import br.com.iagoomes.financialcontrol.domain.mapper.TransactionMapper;
import br.com.iagoomes.financialcontrol.infra.repository.CategoryDataRepository;
import br.com.iagoomes.financialcontrol.infra.repository.TransactionDataRepository;
import br.com.iagoomes.financialcontrol.infra.repository.entity.CategoryData;
import br.com.iagoomes.financialcontrol.infra.repository.entity.TransactionData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of TransactionProvider using JPA Repository
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionDataProvider implements TransactionProvider {

    private final TransactionDataRepository transactionDataRepository;
    private final CategoryDataRepository categoryDataRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Transaction> findById(String id) {
        log.debug("Finding transaction by ID: {}", id);

        Optional<TransactionData> transactionDataOpt = transactionDataRepository.findById(id);

        if (transactionDataOpt.isEmpty()) {
            log.debug("Transaction not found: {}", id);
            return Optional.empty();
        }

        Transaction transaction = transactionMapper.toTransactionDomain(transactionDataOpt.get());
        log.debug("Found transaction: {}", transaction.getTitle());

        return Optional.of(transaction);
    }

    @Override
    @Transactional
    public Transaction save(Transaction transaction) {
        log.debug("Saving transaction: {} - {}", transaction.getId(), transaction.getTitle());

        TransactionData transactionData;

        if (transaction.getId() != null && transactionDataRepository.existsById(transaction.getId())) {
            // Update existing transaction
            transactionData = transactionDataRepository.findById(transaction.getId())
                    .orElseThrow(() -> new IllegalStateException("Transaction not found: " + transaction.getId()));

            // Update fields using mapper
            transactionMapper.updateTransactionData(transactionData, transaction);

            // Update category if changed
            if (transaction.getCategory() != null) {
                CategoryData categoryData = categoryDataRepository.findById(transaction.getCategory().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Category not found: " + transaction.getCategory().getId()));
                transactionData.setCategory(categoryData);
            } else {
                transactionData.setCategory(null);
            }

            log.debug("Updating existing transaction: {}", transaction.getId());

        } else {
            // Create new transaction
            transactionData = transactionMapper.toTransactionData(transaction);

            // Set category if exists
            if (transaction.getCategory() != null) {
                CategoryData categoryData = categoryDataRepository.findById(transaction.getCategory().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Category not found: " + transaction.getCategory().getId()));
                transactionData.setCategory(categoryData);
            }

            log.debug("Creating new transaction: {}", transaction.getTitle());
        }

        TransactionData savedData = transactionDataRepository.save(transactionData);
        Transaction savedTransaction = transactionMapper.toTransactionDomain(savedData);

        log.debug("Successfully saved transaction: {} - {}", savedTransaction.getId(), savedTransaction.getTitle());

        return savedTransaction;
    }
}
