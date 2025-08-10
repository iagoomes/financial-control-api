package br.com.iagoomes.financialcontrol.domain.usecase;

import br.com.iagoomes.financialcontrol.domain.ExtractProvider;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import br.com.iagoomes.financialcontrol.domain.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Use case for generating monthly reports
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateMonthlyReportUseCase {

    private final ExtractProvider extractProvider;

    /**
     * Generate monthly report for the specified period
     */
    public MonthlyReportData execute(Integer year, Integer month) {
        log.info("Generating monthly report for {}/{}", month, year);

        // Find all extracts for the specified period
        List<Extract> extracts = extractProvider.findByPeriod(year, month);

        if (extracts.isEmpty()) {
            log.warn("No extracts found for period {}/{}", month, year);
            return createEmptyReport(year, month);
        }

        // Aggregate all transactions from all extracts
        List<Transaction> allTransactions = extracts.stream()
                .flatMap(extract -> extract.getTransactions().stream())
                .toList();

        log.debug("Found {} transactions for monthly report", allTransactions.size());

        return MonthlyReportData.builder()
                .year(year)
                .month(month)
                .transactions(allTransactions)
                .totalIncome(calculateTotalIncome(allTransactions))
                .totalExpenses(calculateTotalExpenses(allTransactions))
                .transactionCount(allTransactions.size())
                .topExpenses(getTopExpenses(allTransactions, 10))
                .dailyExpenses(calculateDailyExpenses(allTransactions))
                .build();
    }

    private MonthlyReportData createEmptyReport(Integer year, Integer month) {
        return MonthlyReportData.builder()
                .year(year)
                .month(month)
                .transactions(List.of())
                .totalIncome(BigDecimal.ZERO)
                .totalExpenses(BigDecimal.ZERO)
                .transactionCount(0)
                .topExpenses(List.of())
                .dailyExpenses(List.of())
                .build();
    }

    private BigDecimal calculateTotalIncome(List<Transaction> transactions) {
        return transactions.stream()
                .filter(Transaction::isIncome)
                .map(Transaction::getAbsoluteAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalExpenses(List<Transaction> transactions) {
        return transactions.stream()
                .filter(Transaction::isExpense)
                .map(Transaction::getAbsoluteAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Transaction> getTopExpenses(List<Transaction> transactions, int limit) {
        return transactions.stream()
                .filter(Transaction::isExpense)
                .sorted(Comparator.comparing(Transaction::getAbsoluteAmount).reversed())
                .limit(limit)
                .toList();
    }

    private List<DailyExpenseData> calculateDailyExpenses(List<Transaction> transactions) {
        // Group expense transactions by date
        Map<LocalDate, List<Transaction>> transactionsByDate = transactions.stream()
                .filter(Transaction::isExpense)
                .collect(Collectors.groupingBy(transaction -> {
                    return transaction.getDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                }));

        return transactionsByDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Transaction> dayTransactions = entry.getValue();

                    BigDecimal totalAmount = dayTransactions.stream()
                            .map(Transaction::getAbsoluteAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return DailyExpenseData.builder()
                            .date(date)
                            .totalAmount(totalAmount)
                            .transactionCount(dayTransactions.size())
                            .build();
                })
                .sorted(Comparator.comparing(DailyExpenseData::getDate))
                .collect(Collectors.toList());
    }

    /**
     * Data structure to hold monthly report information
     */
    @lombok.Builder
    @lombok.Data
    public static class MonthlyReportData {
        private Integer year;
        private Integer month;
        private List<Transaction> transactions;
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private Integer transactionCount;
        private List<Transaction> topExpenses;
        private List<DailyExpenseData> dailyExpenses;
    }

    /**
     * Data structure for daily expense information
     */
    @lombok.Builder
    @lombok.Data
    public static class DailyExpenseData {
        private LocalDate date;
        private BigDecimal totalAmount;
        private Integer transactionCount;
    }
}
