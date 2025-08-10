package br.com.iagoomes.financialcontrol.domain;

import br.com.iagoomes.financialcontrol.domain.entity.Transaction;

import java.util.Optional;

/**
 * Domain interface for Transaction persistence operations
 */
public interface TransactionProvider {

    /**
     * Find transaction by ID
     */
    Optional<Transaction> findById(String id);

    /**
     * Save or update transaction
     */
    Transaction save(Transaction transaction);
}
