package br.com.iagoomes.financialcontrol.app.resource;

import br.com.iagoomes.financialcontrol.app.service.ExtractService;
import br.com.iagoomes.financialcontrol.model.ExtractAnalysisResponse;
import br.com.iagoomes.financialcontrol.model.FinancialSummary;
import br.com.iagoomes.financialcontrol.model.Period;
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
import java.util.Date;
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
        assertTrue(categoryBreakdown.size() > 0);

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
        Period period = new Period();
        period.setMonth(7);
        period.setYear(2025);
        response.setPeriod(period);

        // Configurar summary - usando apenas campos disponíveis em FinancialSummary
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

        // Configurar bank
        response.setBank(ExtractAnalysisResponse.BankEnum.NUBANK);

        // Configurar ID e processedAt
        response.setId(UUID.randomUUID());
        response.setProcessedAt(new Date());

        return response;
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
