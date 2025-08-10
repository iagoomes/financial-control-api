package br.com.iagoomes.financialcontrol.app.resource;

import br.com.iagoomes.financialcontrol.api.ReportsApiDelegate;
import br.com.iagoomes.financialcontrol.app.service.ReportService;
import br.com.iagoomes.financialcontrol.model.MonthlyReportDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

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
     * Generate monthly report for the specified period
     */
    @Override
    public CompletableFuture<ResponseEntity<MonthlyReportDTO>> getMonthlyReport(Integer year, Integer month) {
        log.info("Resource: Generating monthly report for {}/{}", month, year);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Generate report
                MonthlyReportDTO monthlyReport = reportService.getMonthlyReport(year, month);

                log.info("Successfully generated monthly report for {}/{}", month, year);
                return ResponseEntity.ok(monthlyReport);

            } catch (Exception e) {
                log.error("Unexpected error generating monthly report for {}/{}", month, year, e);
                return ResponseEntity.internalServerError().build();
            }
        });
    }
}
