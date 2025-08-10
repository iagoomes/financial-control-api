package br.com.iagoomes.financialcontrol.domain.entity;

import java.math.BigDecimal;
import java.util.List;

public class MonthlyReport {
    private Integer year;
    private Integer month;
    private List<Transaction> transactions;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private Integer transactionCount;
    private List<Transaction> topExpenses;
    private List<DailyExpense> dailyExpenses;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public Integer getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }

    public List<Transaction> getTopExpenses() {
        return topExpenses;
    }

    public void setTopExpenses(List<Transaction> topExpenses) {
        this.topExpenses = topExpenses;
    }

    public List<DailyExpense> getDailyExpenses() {
        return dailyExpenses;
    }

    public void setDailyExpenses(List<DailyExpense> dailyExpenses) {
        this.dailyExpenses = dailyExpenses;
    }
}
