package br.com.iagoomes.financialcontrol.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class Transaction {
    private String id;
    private Date date;
    private String title;
    private BigDecimal amount;
    private String originalDescription;
    private TransactionType transactionType;
    private Extract extract;
    private Category category;
    private BigDecimal confidence;

    public static Transaction create(LocalDate date, String title, BigDecimal amount, String originalDescription, TransactionType transactionType) {
        Transaction transaction = new Transaction();
        transaction.setDate(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        transaction.setTitle(title);
        transaction.setAmount(amount);
        transaction.setOriginalDescription(originalDescription);
        transaction.setTransactionType(transactionType);
        return transaction;
    }

    /**
     * Determines if transaction is income (negative amount) or expense (positive amount)
     */
    public boolean isIncome() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Determines if transaction is expense (positive amount)
     */
    public boolean isExpense() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Gets absolute value of amount
     */
    public BigDecimal getAbsoluteAmount() {
        return amount.abs();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }

    public void setOriginalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Extract getExtract() {
        return extract;
    }

    public void setExtract(Extract extract) {
        this.extract = extract;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }
}
