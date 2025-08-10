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
                // Validate parameters
                if (year == null || month == null) {
                    log.warn("Invalid parameters: year={}, month={}", year, month);
                    return ResponseEntity.badRequest().build();
                }

                if (month < 1 || month > 12) {
                    log.warn("Invalid month: {}", month);
                    return ResponseEntity.badRequest().build();
                }

                if (year < 2020 || year > 2030) {
                    log.warn("Invalid year: {}", year);
                    return ResponseEntity.badRequest().build();
                }

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
