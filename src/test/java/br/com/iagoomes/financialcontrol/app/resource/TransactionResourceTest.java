package br.com.iagoomes.financialcontrol.app.resource;

import br.com.iagoomes.financialcontrol.app.service.TransactionService;
import br.com.iagoomes.financialcontrol.domain.CategoryProvider;
import br.com.iagoomes.financialcontrol.domain.TransactionProvider;
import br.com.iagoomes.financialcontrol.domain.entity.Category;
import br.com.iagoomes.financialcontrol.domain.entity.Transaction;
import br.com.iagoomes.financialcontrol.domain.entity.TransactionType;
import br.com.iagoomes.financialcontrol.model.CategoryUpdateRequest;
import br.com.iagoomes.financialcontrol.model.TransactionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Teste de integração para TransactionResource
 */
@ExtendWith(MockitoExtension.class)
class TransactionResourceTest {

    @Mock
    private TransactionProvider transactionProvider;

    @Mock
    private CategoryProvider categoryProvider;

    private TransactionResource transactionResource;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Setup real instances following the same pattern as ExtractionResourceRealFlowTest
        var updateTransactionCategoryUseCase = new br.com.iagoomes.financialcontrol.domain.usecase.UpdateTransactionCategoryUseCase(
                transactionProvider, categoryProvider);

        var appMapper = new br.com.iagoomes.financialcontrol.app.mapper.AppMapper();
        var transactionService = new TransactionService(updateTransactionCategoryUseCase, appMapper);

        transactionResource = new TransactionResource(transactionService);
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldSuccessfullyCategorizeTransaction() throws Exception {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        // Create mock transaction
        Transaction mockTransaction = createMockTransaction(transactionId.toString());

        // Create mock category
        Category mockCategory = createMockCategory(categoryId.toString(), "Alimentação");

        // Setup mocks
        when(transactionProvider.findById(transactionId.toString()))
                .thenReturn(Optional.of(mockTransaction));

        when(categoryProvider.findById(categoryId.toString()))
                .thenReturn(Optional.of(mockCategory));

        when(transactionProvider.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction savedTransaction = invocation.getArgument(0);
                    savedTransaction.setCategory(mockCategory);
                    savedTransaction.setConfidence(BigDecimal.ONE);
                    return savedTransaction;
                });

        // Create request
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setCategoryId(categoryId);

        // Act
        CompletableFuture<ResponseEntity<TransactionDTO>> future =
                transactionResource.categorizeTransaction(transactionId, request);

        // Assert
        ResponseEntity<TransactionDTO> response = future.get();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        TransactionDTO transactionDTO = response.getBody();

        // Validate transaction data
        assertEquals(transactionId, transactionDTO.getId());
        assertEquals("Mercado Central", transactionDTO.getTitle());
        assertEquals(45.50, transactionDTO.getAmount());

        // Validate category was updated
        assertNotNull(transactionDTO.getCategory());
        assertEquals(categoryId, transactionDTO.getCategory().getId());
        assertEquals("Alimentação", transactionDTO.getCategory().getName());
        assertEquals("#FF6B6B", transactionDTO.getCategory().getColor());
        assertEquals("🍽️", transactionDTO.getCategory().getIcon());

        // Validate confidence was set to 100%
        assertEquals(1.0, transactionDTO.getConfidence());
    }

    @Test
    void shouldReturn404WhenTransactionNotFound() throws Exception {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(transactionProvider.findById(transactionId.toString()))
                .thenReturn(Optional.empty());

        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setCategoryId(categoryId);

        // Act
        CompletableFuture<ResponseEntity<TransactionDTO>> future =
                transactionResource.categorizeTransaction(transactionId, request);

        // Assert
        ResponseEntity<TransactionDTO> response = future.get();
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void shouldReturn404WhenCategoryNotFound() throws Exception {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Transaction mockTransaction = createMockTransaction(transactionId.toString());

        when(transactionProvider.findById(transactionId.toString()))
                .thenReturn(Optional.of(mockTransaction));

        when(categoryProvider.findById(categoryId.toString()))
                .thenReturn(Optional.empty());

        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setCategoryId(categoryId);

        // Act
        CompletableFuture<ResponseEntity<TransactionDTO>> future =
                transactionResource.categorizeTransaction(transactionId, request);

        // Assert
        ResponseEntity<TransactionDTO> response = future.get();
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    private Transaction createMockTransaction(String id) {
        LocalDate date = LocalDate.of(2025, 7, 30);
        Transaction transaction = Transaction.create(
                date,
                "Mercado Central",
                BigDecimal.valueOf(45.50),
                "Mercado Central - Compra",
                TransactionType.DEBIT
        );
        transaction.setId(id);
        return transaction;
    }

    private Category createMockCategory(String id, String name) {
        Category category = Category.create(name, "#FF6B6B", "🍽️");
        category.setId(id);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }
}
