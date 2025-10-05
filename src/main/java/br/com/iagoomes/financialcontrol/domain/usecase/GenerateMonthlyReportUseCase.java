package br.com.iagoomes.financialcontrol.domain.usecase;

import br.com.iagoomes.financialcontrol.domain.ExtractProvider;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import br.com.iagoomes.financialcontrol.domain.entity.MonthlyReport;
import br.com.iagoomes.financialcontrol.domain.entity.Transaction;
import br.com.iagoomes.financialcontrol.domain.entity.DailyExpense;
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
    public MonthlyReport execute(Integer year, Integer month) {
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

        MonthlyReport report = new MonthlyReport();
        report.setYear(year);
        report.setMonth(month);
        report.setTransactions(allTransactions);
        report.setTotalIncome(calculateTotalIncome(allTransactions));
        report.setTotalExpenses(calculateTotalExpenses(allTransactions));
        report.setTransactionCount(allTransactions.size());
        report.setTopExpenses(getTopExpenses(allTransactions, 10));
        report.setDailyExpenses(calculateDailyExpenses(allTransactions));

        return report;
    }

    private MonthlyReport createEmptyReport(Integer year, Integer month) {
        MonthlyReport report = new MonthlyReport();
        report.setYear(year);
        report.setMonth(month);
        report.setTransactions(List.of());
        report.setTotalIncome(BigDecimal.ZERO);
        report.setTotalExpenses(BigDecimal.ZERO);
        report.setTransactionCount(0);
        report.setTopExpenses(List.of());
        report.setDailyExpenses(List.of());

        return report;
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

    private List<DailyExpense> calculateDailyExpenses(List<Transaction> transactions) {
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

                    DailyExpense dailyExpense = new DailyExpense();
                    dailyExpense.setDate(date);
                    dailyExpense.setTotalAmount(totalAmount);
                    dailyExpense.setTransactionCount(dayTransactions.size());

                    return dailyExpense;
                })
                .sorted(Comparator.comparing(DailyExpense::getDate))
                .collect(Collectors.toList());
    }


}
