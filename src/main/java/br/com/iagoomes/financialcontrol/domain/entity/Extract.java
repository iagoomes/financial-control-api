package br.com.iagoomes.financialcontrol.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Extract {

    private String id;

    private BankType bank;

    private Integer referenceMonth;

    private Integer referenceYear;

    private BigDecimal totalIncome;

    private BigDecimal totalExpenses;

    private Integer transactionCount;

    private LocalDateTime processedAt;

    private List<Transaction> transactions = new ArrayList<>();

    public static Extract create(List<Transaction> transactions, Integer month, Integer year) {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            if (transaction.isIncome()) {
                totalIncome = totalIncome.add(transaction.getAbsoluteAmount());
            } else {
                totalExpenses = totalExpenses.add(transaction.getAbsoluteAmount());
            }
        }

        Extract extract = new Extract();
        extract.setBank(BankType.NUBANK);
        extract.setReferenceMonth(month);
        extract.setReferenceYear(year);
        extract.setTotalIncome(totalIncome);
        extract.setTotalExpenses(totalExpenses);
        extract.setTransactionCount(transactions.size());
        extract.setProcessedAt(LocalDateTime.now());
        extract.setTransactions(transactions);

        transactions.forEach(transaction -> transaction.setExtract(extract));

        return extract;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BankType getBank() {
        return bank;
    }

    public void setBank(BankType bank) {
        this.bank = bank;
    }

    public Integer getReferenceMonth() {
        return referenceMonth;
    }

    public void setReferenceMonth(Integer referenceMonth) {
        this.referenceMonth = referenceMonth;
    }

    public Integer getReferenceYear() {
        return referenceYear;
    }

    public void setReferenceYear(Integer referenceYear) {
        this.referenceYear = referenceYear;
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

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
