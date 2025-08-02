package br.com.iagoomes.financialcontrol.infra.repository.entity;

import br.com.iagoomes.financialcontrol.domain.entity.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "date", nullable = false)
    private Date date;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "original_description", length = 1000)
    private String originalDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extract_id", nullable = false)
    private ExtractData extract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryData category;

    @Column(name = "confidence", precision = 3, scale = 2)
    private BigDecimal confidence;

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
}
