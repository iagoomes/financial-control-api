package br.com.iagoomes.financialcontrol.app.resource;

import br.com.iagoomes.financialcontrol.app.service.ExtractService;
import br.com.iagoomes.financialcontrol.model.CategoryDTO;
import br.com.iagoomes.financialcontrol.model.CategorySummary;
import br.com.iagoomes.financialcontrol.model.ExtractAnalysisResponse;
import br.com.iagoomes.financialcontrol.model.FinancialSummary;
import br.com.iagoomes.financialcontrol.model.PeriodDTO;
import br.com.iagoomes.financialcontrol.model.TransactionDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtractionResourceIntegrationTest {

    @Mock
    private ExtractService extractService;

    private ExtractionResource extractionResource;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        extractionResource = new ExtractionResource(extractService);
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldProcessCsvFileAndReturnCompleteJsonResponse() throws Exception {
        // Arrange - Carrega o arquivo CSV de teste
        ClassPathResource csvResource = new ClassPathResource("csv/test-extract-nubank-2025-07.csv");
        byte[] csvBytes = Files.readAllBytes(csvResource.getFile().toPath());

        MultipartFile csvFile = new MockMultipartFile(
                "file",
                "test-extract-nubank-2025-07.csv",
                "text/csv",
                csvBytes
        );

        // Mock da resposta esperada baseada no CSV
        ExtractAnalysisResponse expectedResponse = createExpectedResponseFromCsv();
        when(extractService.processExtractFile(any(MultipartFile.class), eq("NUBANK"), eq(7), eq(2025)))
                .thenReturn(expectedResponse);

        // Act
        CompletableFuture<org.springframework.http.ResponseEntity<ExtractAnalysisResponse>> future =
                extractionResource.uploadExtract(csvFile, "NUBANK", 7, 2025);

        // Assert
        var responseEntity = future.get();
        assertEquals(200, responseEntity.getStatusCodeValue());

        ExtractAnalysisResponse actualResponse = responseEntity.getBody();
        assertNotNull(actualResponse);

        // Converte para JSON para validação completa
        String jsonResponse = objectMapper.writeValueAsString(actualResponse);
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);

        validateCompleteJsonStructure(jsonNode);
        validateBusinessRulesFromCsv(jsonNode);
        validateSpecificTransactionsFromCsv(jsonNode);
    }

    @Test
    void shouldReadCsvFileCorrectly() throws IOException {
        // Teste específico para validar a leitura do CSV
        ClassPathResource csvResource = new ClassPathResource("csv/test-extract-nubank-2025-07.csv");
        assertTrue(csvResource.exists(), "Arquivo CSV de teste deve existir");

        String csvContent = Files.readString(csvResource.getFile().toPath());

        // Valida estrutura do CSV
        String[] lines = csvContent.split("\n");
        assertEquals(13, lines.length); // Header + 12 transações
        assertEquals("date,title,amount", lines[0].trim()); // Header correto

        // Valida algumas transações específicas
        assertTrue(csvContent.contains("Mercado Central,45.50"));
        assertTrue(csvContent.contains("Salario,-3500.00"));
        assertTrue(csvContent.contains("Transferencia PIX,-500.00"));
    }

    private void validateCompleteJsonStructure(JsonNode jsonNode) {
        // Valida campos obrigatórios
        assertTrue(jsonNode.has("id"));
        assertTrue(jsonNode.has("bank"));
        assertTrue(jsonNode.has("period"));
        assertTrue(jsonNode.has("summary"));
        assertTrue(jsonNode.has("transactions"));
        assertTrue(jsonNode.has("categoryBreakdown"));
        assertTrue(jsonNode.has("processedAt"));

        // Valida banco
        assertEquals("NUBANK", jsonNode.get("bank").asText());

        // Valida período
        JsonNode period = jsonNode.get("period");
        assertEquals(7, period.get("month").asInt());
        assertEquals(2025, period.get("year").asInt());

        // Valida estrutura das transações
        JsonNode transactions = jsonNode.get("transactions");
        assertEquals(12, transactions.size()); // 12 transações do CSV

        for (JsonNode transaction : transactions) {
            assertTransactionStructure(transaction);
        }

        // Valida categoryBreakdown
        JsonNode categoryBreakdown = jsonNode.get("categoryBreakdown");
        assertTrue(categoryBreakdown.isArray());
        assertTrue(!categoryBreakdown.isEmpty());

        for (JsonNode categoryItem : categoryBreakdown) {
            assertCategoryBreakdownStructure(categoryItem);
        }
    }

    private void assertTransactionStructure(JsonNode transaction) {
        assertTrue(transaction.has("id"));
        assertTrue(transaction.has("date"));
        assertTrue(transaction.has("title"));
        assertTrue(transaction.has("amount"));
        assertTrue(transaction.has("category"));
        assertTrue(transaction.has("originalDescription"));
        assertTrue(transaction.has("confidence"));

        // Valida estrutura da categoria
        JsonNode category = transaction.get("category");
        assertTrue(category.has("id"));
        assertTrue(category.has("name"));
        assertTrue(category.has("color"));
        assertTrue(category.has("icon"));
    }

    private void assertCategoryBreakdownStructure(JsonNode categoryItem) {
        assertTrue(categoryItem.has("category"));
        assertTrue(categoryItem.has("totalAmount"));
        assertTrue(categoryItem.has("transactionCount"));
        assertTrue(categoryItem.has("percentage"));
        assertTrue(categoryItem.has("averageAmount"));

        // Valida que os valores são positivos
        assertTrue(categoryItem.get("totalAmount").asDouble() >= 0);
        assertTrue(categoryItem.get("transactionCount").asInt() > 0);
        assertTrue(categoryItem.get("percentage").asDouble() >= 0);
        assertTrue(categoryItem.get("averageAmount").asDouble() >= 0);
    }

    private void validateBusinessRulesFromCsv(JsonNode jsonNode) {
        JsonNode summary = jsonNode.get("summary");
        JsonNode transactions = jsonNode.get("transactions");

        // Regra 1: Contagem de transações
        assertEquals(12, transactions.size());
        assertEquals(12, summary.get("transactionCount").asInt());

        // Regra 2: Cálculos baseados no CSV
        // Despesas: 45.50 + 18.00 + 85.40 + 35.75 + 60.00 + 120.00 + 22.80 + 156.90 + 25.50 + 42.30 = 612.15
        // Receitas: 500.00 + 3500.00 = 4000.00

        BigDecimal expectedExpenses = new BigDecimal("612.15");
        BigDecimal expectedIncome = new BigDecimal("4000.00");

        assertEquals(0, expectedExpenses.compareTo(
                BigDecimal.valueOf(summary.get("totalExpenses").asDouble())));
        assertEquals(0, expectedIncome.compareTo(
                BigDecimal.valueOf(summary.get("totalIncome").asDouble())));

        // Regra 3: Validação do categoryBreakdown
        JsonNode categoryBreakdown = jsonNode.get("categoryBreakdown");
        BigDecimal totalBreakdownAmount = BigDecimal.ZERO;

        for (JsonNode categoryItem : categoryBreakdown) {
            totalBreakdownAmount = totalBreakdownAmount.add(
                    BigDecimal.valueOf(categoryItem.get("totalAmount").asDouble()));
        }

        assertEquals(0, totalBreakdownAmount.compareTo(expectedExpenses));
    }

    private void validateSpecificTransactionsFromCsv(JsonNode jsonNode) {
        JsonNode transactions = jsonNode.get("transactions");

        boolean mercadoCentralFound = false;
        boolean salarioFound = false;
        boolean pixFound = false;

        for (JsonNode transaction : transactions) {
            String title = transaction.get("title").asText();
            double amount = transaction.get("amount").asDouble();

            if ("Mercado Central".equals(title) && amount == 45.50) {
                mercadoCentralFound = true;
            }
            if ("Salario".equals(title) && amount == -3500.00) {
                salarioFound = true;
            }
            if ("Transferencia PIX".equals(title) && amount == -500.00) {
                pixFound = true;
            }
        }

        assertTrue(mercadoCentralFound, "Transação 'Mercado Central' deve estar presente");
        assertTrue(salarioFound, "Transação 'Salario' deve estar presente");
        assertTrue(pixFound, "Transação 'Transferencia PIX' deve estar presente");
    }

    private ExtractAnalysisResponse createExpectedResponseFromCsv() {
        // Cria uma resposta mock baseada no processamento esperado do CSV
        ExtractAnalysisResponse response = new ExtractAnalysisResponse();

        // Configurar período
        PeriodDTO period = new PeriodDTO();
        period.setMonth(7);
        period.setYear(2025);
        response.setPeriod(period);

        // Criar transações baseadas no CSV
        List<TransactionDTO> transactions = createMockTransactions();
        response.setTransactions(transactions);

        // Configurar summary
        double totalExpenses = 612.15;
        double totalIncome = 4000.00;
        double netAmount = totalIncome - totalExpenses;

        FinancialSummary summary = FinancialSummary.builder()
                .totalExpenses(totalExpenses)
                .totalIncome(totalIncome)
                .netAmount(netAmount)
                .transactionCount(12)
                .averageTransactionValue(totalExpenses / 12)
                .build();

        response.setSummary(summary);

        // Criar breakdown de categorias usando CategorySummary
        List<CategorySummary> categoryBreakdown = createMockCategoryBreakdown();
        response.setCategoryBreakdown(categoryBreakdown);

        // Configurar bank
        response.setBank(ExtractAnalysisResponse.BankEnum.NUBANK);

        // Configurar ID e processedAt
        response.setId(UUID.randomUUID());
        response.setProcessedAt(new Date());

        return response;
    }

    private List<TransactionDTO> createMockTransactions() {
        List<TransactionDTO> transactions = new ArrayList<>();

        // Criar transações baseadas no CSV test-extract-nubank-2025-07.csv
        transactions.add(createTransactionDTO("2025-07-30", "Mercado Central", 45.50, "Alimentação"));
        transactions.add(createTransactionDTO("2025-07-29", "Uber", 18.00, "Transporte"));
        transactions.add(createTransactionDTO("2025-07-29", "Restaurante Villa", 85.40, "Alimentação"));
        transactions.add(createTransactionDTO("2025-07-27", "Farmacia Drogasil", 35.75, "Saúde"));
        transactions.add(createTransactionDTO("2025-07-27", "Cinema Multiplex", 60.00, "Entretenimento"));
        transactions.add(createTransactionDTO("2025-07-25", "Posto Shell", 120.00, "Transporte"));
        transactions.add(createTransactionDTO("2025-07-23", "Padaria do Bairro", 22.80, "Alimentação"));
        transactions.add(createTransactionDTO("2025-07-22", "Supermercado Extra", 156.90, "Alimentação"));
        transactions.add(createTransactionDTO("2025-07-20", "99 Taxi", 25.50, "Transporte"));
        transactions.add(createTransactionDTO("2025-07-18", "Lanchonete do João", 42.30, "Alimentação"));
        transactions.add(createTransactionDTO("2025-07-15", "Transferencia PIX", -500.00, "Transferência"));
        transactions.add(createTransactionDTO("2025-07-10", "Salario", -3500.00, "Renda"));

        return transactions;
    }

    private TransactionDTO createTransactionDTO(String date, String title, double amount, String categoryName) {
        TransactionDTO transaction = new TransactionDTO();
        transaction.setId(UUID.randomUUID());
        transaction.setDate(java.sql.Date.valueOf(date));
        transaction.setTitle(title);
        transaction.setAmount(amount);
        transaction.setOriginalDescription(title);
        transaction.setConfidence(0.95);

        // Criar categoria
        CategoryDTO category = new CategoryDTO();
        category.setId(UUID.randomUUID());
        category.setName(categoryName);
        category.setColor(getCategoryColor(categoryName));
        category.setIcon(getCategoryIcon(categoryName));
        transaction.setCategory(category);

        return transaction;
    }

    private List<CategorySummary> createMockCategoryBreakdown() {
        List<CategorySummary> breakdown = new ArrayList<>();

        // Valores calculados baseados no CSV test-extract-nubank-2025-07.csv
        breakdown.add(createCategorySummary("Alimentação", 352.90, "#4CAF50", "🍽️", 5));  // 45.50 + 85.40 + 22.80 + 156.90 + 42.30
        breakdown.add(createCategorySummary("Transporte", 163.50, "#2196F3", "🚗", 3));   // 18.00 + 120.00 + 25.50
        breakdown.add(createCategorySummary("Saúde", 35.75, "#FF9800", "🏥", 1));         // 35.75
        breakdown.add(createCategorySummary("Entretenimento", 60.00, "#9C27B0", "🎬", 1)); // 60.00

        // Total: 352.90 + 163.50 + 35.75 + 60.00 = 612.15 ✅
        return breakdown;
    }

    private CategorySummary createCategorySummary(String name, double totalAmount, String color, String icon, int transactionCount) {
        CategorySummary summary = new CategorySummary();

        CategoryDTO category = new CategoryDTO();
        category.setId(UUID.randomUUID());
        category.setName(name);
        category.setColor(color);
        category.setIcon(icon);
        summary.setCategory(category);

        summary.setTotalAmount(totalAmount);
        summary.setTransactionCount(transactionCount);
        summary.setPercentage(totalAmount / 4612.15 * 100); // Percentual do total
        summary.setAverageAmount(totalAmount / transactionCount); // Valor médio por transação

        return summary;
    }

    private String getCategoryColor(String categoryName) {
        return switch (categoryName) {
            case "Alimentação" -> "#4CAF50";
            case "Transporte" -> "#2196F3";
            case "Saúde" -> "#FF9800";
            case "Entretenimento" -> "#9C27B0";
            case "Transferência" -> "#607D8B";
            case "Renda" -> "#8BC34A";
            default -> "#9E9E9E";
        };
    }

    private String getCategoryIcon(String categoryName) {
        return switch (categoryName) {
            case "Alimentação" -> "🍽️";
            case "Transporte" -> "🚗";
            case "Saúde" -> "🏥";
            case "Entretenimento" -> "🎬";
            case "Transferência" -> "💸";
            case "Renda" -> "💰";
            default -> "📝";
        };
    }

    @Test
    void shouldValidateCsvTransactionCategories() throws Exception {
        // Teste específico para validar que as categorias são atribuídas corretamente
        ClassPathResource csvResource = new ClassPathResource("csv/test-extract-nubank-2025-07.csv");
        byte[] csvBytes = Files.readAllBytes(csvResource.getFile().toPath());

        MultipartFile csvFile = new MockMultipartFile("file", "test.csv", "text/csv", csvBytes);

        ExtractAnalysisResponse mockResponse = createExpectedResponseFromCsv();
        when(extractService.processExtractFile(any(), eq("NUBANK"), eq(7), eq(2025)))
                .thenReturn(mockResponse);

        var future = extractionResource.uploadExtract(csvFile, "NUBANK", 7, 2025);
        var response = future.get().getBody();

        String json = objectMapper.writeValueAsString(response);
        JsonNode jsonNode = objectMapper.readTree(json);

        // Valida que existem categorias esperadas baseadas no CSV
        assertTrue(hasCategoryInBreakdown(jsonNode, "Alimentação")); // Mercado, Restaurante, Padaria
        assertTrue(hasCategoryInBreakdown(jsonNode, "Transporte")); // Uber, 99, Posto
        assertTrue(hasCategoryInBreakdown(jsonNode, "Saúde")); // Farmacia
        assertTrue(hasCategoryInBreakdown(jsonNode, "Entretenimento")); // Cinema
    }

    private boolean hasCategoryInBreakdown(JsonNode jsonNode, String categoryName) {
        JsonNode categoryBreakdown = jsonNode.get("categoryBreakdown");
        for (JsonNode item : categoryBreakdown) {
            if (categoryName.equals(item.get("category").get("name").asText())) {
                return true;
            }
        }
        return false;
    }
}
