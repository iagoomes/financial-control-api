package br.com.iagoomes.financialcontrol.app.resource;

import br.com.iagoomes.financialcontrol.app.service.ReportService;
import br.com.iagoomes.financialcontrol.domain.ExtractProvider;
import br.com.iagoomes.financialcontrol.model.MonthlyReportDTO;
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
        CompletableFuture<ResponseEntity<MonthlyReportDTO>> future =
                reportResource.getMonthlyReport(year, month);

        // Assert
        ResponseEntity<MonthlyReportDTO> response = future.get();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        MonthlyReportDTO monthlyReport = response.getBody();

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
    void shouldReturn200ForValidParameters() throws Exception {
        // Arrange
        Integer year = 2025;
        Integer month = 7;

        when(extractProvider.findByPeriod(year, month)).thenReturn(List.of());

        // Act
        CompletableFuture<ResponseEntity<MonthlyReportDTO>> future =
                reportResource.getMonthlyReport(year, month);

        // Assert
        ResponseEntity<MonthlyReportDTO> response = future.get();
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}
