package br.com.iagoomes.financialcontrol.app.resource;

import br.com.iagoomes.financialcontrol.api.ReportsApiDelegate;
import br.com.iagoomes.financialcontrol.app.service.ReportService;
import br.com.iagoomes.financialcontrol.model.MonthlyReportDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Resource implementation for Reports API operations
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportResource implements ReportsApiDelegate {

    private final ReportService reportService;

    /**
     * Generate monthly report for the specified period (user-scoped)
     */
    @Override
    public CompletableFuture<ResponseEntity<MonthlyReportDTO>> getUserMonthlyReport(UUID userId,
                                                                                    Integer year,
                                                                                    Integer month) {
        log.info("Resource: Generating monthly report for user: {}, period: {}/{}", userId, month, year);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Generate report for user
                MonthlyReportDTO monthlyReport = reportService.getMonthlyReport(userId.toString(), year, month);

                log.info("Successfully generated monthly report for user: {}, period: {}/{}", userId, month, year);
                return ResponseEntity.ok(monthlyReport);

            } catch (Exception e) {
                log.error("Unexpected error generating monthly report for user: {}, period: {}/{}", userId, month, year, e);
                return ResponseEntity.internalServerError().build();
            }
        });
    }
}
