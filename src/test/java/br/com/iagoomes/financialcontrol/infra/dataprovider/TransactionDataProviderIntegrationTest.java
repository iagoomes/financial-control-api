package br.com.iagoomes.financialcontrol.infra.dataprovider;

import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.domain.entity.Category;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import br.com.iagoomes.financialcontrol.domain.entity.Transaction;
import br.com.iagoomes.financialcontrol.domain.entity.TransactionType;
import br.com.iagoomes.financialcontrol.infra.repository.CategoryDataRepository;
import br.com.iagoomes.financialcontrol.infra.repository.ExtractDataRepository;
import br.com.iagoomes.financialcontrol.infra.repository.TransactionDataRepository;
import br.com.iagoomes.financialcontrol.infra.repository.entity.CategoryData;
import br.com.iagoomes.financialcontrol.infra.repository.entity.ExtractData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for TransactionDataProvider
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionDataProviderIntegrationTest {

    @Autowired
    private TransactionDataProvider transactionDataProvider;

    @Autowired
    private TransactionDataRepository transactionRepository;

    @Autowired
    private CategoryDataRepository categoryRepository;

    @Autowired
    private ExtractDataRepository extractRepository;

    private CategoryData testCategory;
    private ExtractData testExtract;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        transactionRepository.deleteAll();
        categoryRepository.deleteAll();
        extractRepository.deleteAll();

        // Create test extract first (required for transactions)
        testExtract = ExtractData.builder()
                .bank(BankType.NUBANK)
                .referenceMonth(8)
                .referenceYear(2025)
                .totalIncome(BigDecimal.ZERO)
                .totalExpenses(BigDecimal.ZERO)
                .transactionCount(0)
                .processedAt(LocalDateTime.now())
                .build();
        testExtract = extractRepository.save(testExtract);

        // Create test category
        testCategory = CategoryData.builder()
                .name("Test Category")
                .color("#FF0000")
                .icon("🧪")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    void shouldSaveAndFindTransactionSuccessfully() {
        // Arrange
        Transaction transaction = createTestTransaction();

        // Act - Save transaction
        Transaction savedTransaction = transactionDataProvider.save(transaction);

        // Assert - Verify save
        assertNotNull(savedTransaction.getId());
        assertEquals("Test Transaction", savedTransaction.getTitle());
        assertEquals(BigDecimal.valueOf(100.50), savedTransaction.getAmount());

        // Act - Find transaction
        Optional<Transaction> foundTransaction = transactionDataProvider.findById(savedTransaction.getId());

        // Assert - Verify find
        assertTrue(foundTransaction.isPresent());
        assertEquals(savedTransaction.getId(), foundTransaction.get().getId());
        assertEquals("Test Transaction", foundTransaction.get().getTitle());
        assertEquals(BigDecimal.valueOf(100.50), foundTransaction.get().getAmount());
    }

    @Test
    void shouldReturnEmptyWhenTransactionNotFound() {
        // Act
        Optional<Transaction> result = transactionDataProvider.findById("non-existent-id");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFoundDuringSave() {
        // Arrange
        Transaction transaction = createTestTransaction();
        Category nonExistentCategory = Category.create("Non Existent", "#000000", "❌");
        nonExistentCategory.setId("non-existent-category-id");
        transaction.setCategory(nonExistentCategory);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            transactionDataProvider.save(transaction);
        });
    }

    private Transaction createTestTransaction() {
        LocalDate date = LocalDate.of(2025, 8, 10);
        Transaction transaction = Transaction.create(
                date,
                "Test Transaction",
                BigDecimal.valueOf(100.50),
                "Test Original Description",
                TransactionType.DEBIT
        );

        // Create and associate extract to transaction
        Extract extract = new Extract();
        extract.setId(testExtract.getId());
        extract.setBank(testExtract.getBank());
        extract.setReferenceMonth(testExtract.getReferenceMonth());
        extract.setReferenceYear(testExtract.getReferenceYear());
        extract.setTotalIncome(testExtract.getTotalIncome());
        extract.setTotalExpenses(testExtract.getTotalExpenses());
        extract.setTransactionCount(testExtract.getTransactionCount());
        extract.setProcessedAt(testExtract.getProcessedAt());

        transaction.setExtract(extract);
        return transaction;
    }
    
}
