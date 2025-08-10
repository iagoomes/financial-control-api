package br.com.iagoomes.financialcontrol.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailyExpense {
    private LocalDate date;
    private BigDecimal totalAmount;
    private Integer transactionCount;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }
}