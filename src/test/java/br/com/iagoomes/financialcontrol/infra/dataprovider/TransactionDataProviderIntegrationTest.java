package br.com.iagoomes.financialcontrol.infra.dataprovider;

import br.com.iagoomes.financialcontrol.domain.entity.Category;
import br.com.iagoomes.financialcontrol.domain.entity.Transaction;
import br.com.iagoomes.financialcontrol.domain.entity.TransactionType;
import br.com.iagoomes.financialcontrol.infra.repository.CategoryDataRepository;
import br.com.iagoomes.financialcontrol.infra.repository.TransactionDataRepository;
import br.com.iagoomes.financialcontrol.infra.repository.entity.CategoryData;
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

    private CategoryData testCategory;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        transactionRepository.deleteAll();
        categoryRepository.deleteAll();

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
    void shouldUpdateTransactionCategorySuccessfully() {
        // Arrange - Save transaction without category
        Transaction transaction = createTestTransaction();
        Transaction savedTransaction = transactionDataProvider.save(transaction);
        assertNull(savedTransaction.getCategory());

        // Create domain category from test data
        Category domainCategory = createDomainCategory();

        // Act - Update transaction with category
        savedTransaction.setCategory(domainCategory);
        savedTransaction.setConfidence(BigDecimal.ONE);
        Transaction updatedTransaction = transactionDataProvider.save(savedTransaction);

        // Assert - Verify category was updated
        assertNotNull(updatedTransaction.getCategory());
        assertEquals(testCategory.getId(), updatedTransaction.getCategory().getId());
        assertEquals("Test Category", updatedTransaction.getCategory().getName());
        assertEquals(BigDecimal.ONE, updatedTransaction.getConfidence());

        // Verify in database
        Optional<Transaction> dbTransaction = transactionDataProvider.findById(updatedTransaction.getId());
        assertTrue(dbTransaction.isPresent());
        assertNotNull(dbTransaction.get().getCategory());
        assertEquals("Test Category", dbTransaction.get().getCategory().getName());
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

    @Test
    void shouldRemoveCategoryWhenSetToNull() {
        // Arrange - Save transaction with category
        Transaction transaction = createTestTransaction();
        transaction.setCategory(createDomainCategory());
        Transaction savedTransaction = transactionDataProvider.save(transaction);
        assertNotNull(savedTransaction.getCategory());

        // Act - Remove category
        savedTransaction.setCategory(null);
        Transaction updatedTransaction = transactionDataProvider.save(savedTransaction);

        // Assert - Verify category was removed
        assertNull(updatedTransaction.getCategory());

        // Verify in database
        Optional<Transaction> dbTransaction = transactionDataProvider.findById(updatedTransaction.getId());
        assertTrue(dbTransaction.isPresent());
        assertNull(dbTransaction.get().getCategory());
    }

    private Transaction createTestTransaction() {
        LocalDate date = LocalDate.of(2025, 8, 10);
        return Transaction.create(
                date,
                "Test Transaction",
                BigDecimal.valueOf(100.50),
                "Test Original Description",
                TransactionType.DEBIT
        );
    }

    private Category createDomainCategory() {
        Category category = Category.create(
                testCategory.getName(),
                testCategory.getColor(),
                testCategory.getIcon()
        );
        category.setId(testCategory.getId());
        category.setCreatedAt(testCategory.getCreatedAt());
        category.setUpdatedAt(testCategory.getUpdatedAt());
        return category;
    }
}
