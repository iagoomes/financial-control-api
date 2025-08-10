package br.com.iagoomes.financialcontrol.app.service;

import br.com.iagoomes.financialcontrol.app.mapper.AppMapper;
import br.com.iagoomes.financialcontrol.domain.entity.Transaction;
import br.com.iagoomes.financialcontrol.domain.usecase.GenerateMonthlyReportUseCase;
import br.com.iagoomes.financialcontrol.model.CategorySummary;
import br.com.iagoomes.financialcontrol.model.DailyExpense;
import br.com.iagoomes.financialcontrol.model.FinancialSummary;
import br.com.iagoomes.financialcontrol.model.MonthlyReport;
import br.com.iagoomes.financialcontrol.model.Period;
import br.com.iagoomes.financialcontrol.model.TransactionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Application Service for Report operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final GenerateMonthlyReportUseCase generateMonthlyReportUseCase;
    private final AppMapper appMapper;

    /**
     * Generate monthly report for the specified period
     */
    public MonthlyReport getMonthlyReport(Integer year, Integer month) {
        log.info("Service: Generating monthly report for {}/{}", month, year);

        // Execute use case
        GenerateMonthlyReportUseCase.MonthlyReportData reportData =
                generateMonthlyReportUseCase.execute(year, month);

        // Map to DTO
        MonthlyReport monthlyReport = new MonthlyReport();

        monthlyReport.setPeriod(createPeriod(month, year));
        monthlyReport.setSummary(createFinancialSummary(reportData));
        monthlyReport.setCategoryBreakdown(createCategoryBreakdown(reportData.getTransactions()));
        monthlyReport.setDailyExpenses(mapDailyExpenses(reportData.getDailyExpenses()));
        monthlyReport.setTopExpenses(mapTopExpenses(reportData.getTopExpenses()));

        log.info("Generated monthly report with {} transactions", reportData.getTransactionCount());

        return monthlyReport;
    }

    private Period createPeriod(Integer month, Integer year) {
        Period period = new Period();
        period.setMonth(month);
        period.setYear(year);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        period.setStartDate(Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        period.setEndDate(Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        return period;
    }

    private FinancialSummary createFinancialSummary(GenerateMonthlyReportUseCase.MonthlyReportData reportData) {
        FinancialSummary summary = new FinancialSummary();

        summary.setTotalIncome(reportData.getTotalIncome().doubleValue());
        summary.setTotalExpenses(reportData.getTotalExpenses().doubleValue());

        BigDecimal netAmount = reportData.getTotalIncome().subtract(reportData.getTotalExpenses());
        summary.setNetAmount(netAmount.doubleValue());

        summary.setTransactionCount(reportData.getTransactionCount());

        if (reportData.getTransactionCount() > 0) {
            BigDecimal totalAmount = reportData.getTotalIncome().add(reportData.getTotalExpenses());
            BigDecimal average = totalAmount.divide(BigDecimal.valueOf(reportData.getTransactionCount()), 2, RoundingMode.HALF_UP);
            summary.setAverageTransactionValue(average.doubleValue());
        } else {
            summary.setAverageTransactionValue(0.0);
        }

        return summary;
    }

    private List<CategorySummary> createCategoryBreakdown(List<Transaction> transactions) {
        List<Transaction> expenseTransactions = transactions.stream()
                .filter(Transaction::isExpense)
                .filter(t -> t.getCategory() != null)
                .toList();

        if (expenseTransactions.isEmpty()) {
            return List.of();
        }

        double totalExpenses = expenseTransactions.stream()
                .mapToDouble(t -> t.getAbsoluteAmount().doubleValue())
                .sum();

        Map<String, List<Transaction>> transactionsByCategory = expenseTransactions.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory().getId()));

        return transactionsByCategory.values().stream()
                .map(categoryTransactions -> {
                    Transaction firstTransaction = categoryTransactions.get(0);

                    double totalAmount = categoryTransactions.stream()
                            .mapToDouble(t -> t.getAbsoluteAmount().doubleValue())
                            .sum();

                    int transactionCount = categoryTransactions.size();
                    double percentage = totalExpenses > 0 ? (totalAmount / totalExpenses) * 100 : 0;
                    double averageAmount = transactionCount > 0 ? totalAmount / transactionCount : 0;

                    CategorySummary summary = new CategorySummary();
                    summary.setCategory(appMapper.toCategoryDTO(firstTransaction.getCategory()));
                    summary.setTotalAmount(totalAmount);
                    summary.setTransactionCount(transactionCount);
                    summary.setPercentage(percentage);
                    summary.setAverageAmount(averageAmount);

                    return summary;
                })
                .sorted((a, b) -> Double.compare(b.getTotalAmount(), a.getTotalAmount()))
                .toList();
    }

    private List<DailyExpense> mapDailyExpenses(List<GenerateMonthlyReportUseCase.DailyExpenseData> dailyExpenseData) {
        return dailyExpenseData.stream()
                .map(data -> {
                    DailyExpense dailyExpense = new DailyExpense();
                    dailyExpense.setDate(Date.from(data.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    dailyExpense.setTotalAmount(data.getTotalAmount().doubleValue());
                    dailyExpense.setTransactionCount(data.getTransactionCount());
                    return dailyExpense;
                })
                .toList();
    }

    private List<TransactionDTO> mapTopExpenses(List<Transaction> topExpenses) {
        return topExpenses.stream()
                .map(appMapper::mapTransaction)
                .toList();
    }
}
