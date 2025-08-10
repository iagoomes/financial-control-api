package br.com.iagoomes.financialcontrol.infra.repository;

import br.com.iagoomes.financialcontrol.infra.repository.entity.TransactionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionDataRepository extends JpaRepository<TransactionData, String> {

    /**
     * Find transactions by extract ID
     */
    List<TransactionData> findByExtractIdOrderByDateDesc(String extractId);

    /**
     * Find transactions by category ID
     */
    List<TransactionData> findByCategoryIdOrderByDateDesc(String categoryId);

    /**
     * Find transactions by extract ID and category ID
     */
    List<TransactionData> findByExtractIdAndCategoryIdOrderByDateDesc(String extractId, String categoryId);

    /**
     * Find transactions without category (null category)
     */
    @Query("SELECT t FROM TransactionData t WHERE t.category IS NULL ORDER BY t.date DESC")
    List<TransactionData> findUncategorizedTransactions();

    /**
     * Count transactions by category ID
     */
    long countByCategoryId(String categoryId);

    /**
     * Check if transaction exists by ID
     */
    boolean existsById(String id);
}
