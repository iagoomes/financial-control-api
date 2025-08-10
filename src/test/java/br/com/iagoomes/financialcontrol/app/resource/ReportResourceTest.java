package br.com.iagoomes.financialcontrol.app.resource;

import br.com.iagoomes.financialcontrol.app.service.ReportService;
import br.com.iagoomes.financialcontrol.domain.ExtractProvider;
import br.com.iagoomes.financialcontrol.model.MonthlyReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Teste para ReportResource
 */
@ExtendWith(MockitoExtension.class)
class ReportResourceTest {

    @Mock
    private ExtractProvider extractProvider;

    private ReportResource reportResource;

    @BeforeEach
    void setUp() {
        // Setup real instances seguindo o padrão da aplicação
        var generateMonthlyReportUseCase = new br.com.iagoomes.financialcontrol.domain.usecase.GenerateMonthlyReportUseCase(extractProvider);
        var appMapper = new br.com.iagoomes.financialcontrol.app.mapper.AppMapper();
        var reportService = new ReportService(generateMonthlyReportUseCase, appMapper);

        reportResource = new ReportResource(reportService);
    }

    @Test
    void shouldGenerateEmptyMonthlyReportWhenNoExtracts() throws Exception {
        // Arrange
        Integer year = 2025;
        Integer month = 8;

        when(extractProvider.findByPeriod(year, month)).thenReturn(List.of());

        // Act
        CompletableFuture<ResponseEntity<MonthlyReport>> future =
                reportResource.getMonthlyReport(year, month);

        // Assert
        ResponseEntity<MonthlyReport> response = future.get();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        MonthlyReport monthlyReport = response.getBody();

        // Validate period
        assertNotNull(monthlyReport.getPeriod());
        assertEquals(month, monthlyReport.getPeriod().getMonth());
        assertEquals(year, monthlyReport.getPeriod().getYear());

        // Validate empty summary
        assertNotNull(monthlyReport.getSummary());
        assertEquals(0.0, monthlyReport.getSummary().getTotalIncome());
        assertEquals(0.0, monthlyReport.getSummary().getTotalExpenses());
        assertEquals(0, monthlyReport.getSummary().getTransactionCount());

        // Validate empty collections
        assertNotNull(monthlyReport.getCategoryBreakdown());
        assertTrue(monthlyReport.getCategoryBreakdown().isEmpty());

        assertNotNull(monthlyReport.getDailyExpenses());
        assertTrue(monthlyReport.getDailyExpenses().isEmpty());

        assertNotNull(monthlyReport.getTopExpenses());
        assertTrue(monthlyReport.getTopExpenses().isEmpty());
    }

    @Test
    void shouldReturn400ForInvalidParameters() throws Exception {
        // Test invalid month
        CompletableFuture<ResponseEntity<MonthlyReport>> future1 =
                reportResource.getMonthlyReport(2025, 13);
        assertEquals(400, future1.get().getStatusCodeValue());

        // Test invalid year
        CompletableFuture<ResponseEntity<MonthlyReport>> future2 =
                reportResource.getMonthlyReport(2019, 7);
        assertEquals(400, future2.get().getStatusCodeValue());

        // Test null parameters
        CompletableFuture<ResponseEntity<MonthlyReport>> future3 =
                reportResource.getMonthlyReport(null, 7);
        assertEquals(400, future3.get().getStatusCodeValue());
    }

    @Test
    void shouldReturn200ForValidParameters() throws Exception {
        // Arrange
        Integer year = 2025;
        Integer month = 7;

        when(extractProvider.findByPeriod(year, month)).thenReturn(List.of());

        // Act
        CompletableFuture<ResponseEntity<MonthlyReport>> future =
                reportResource.getMonthlyReport(year, month);

        // Assert
        ResponseEntity<MonthlyReport> response = future.get();
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}
