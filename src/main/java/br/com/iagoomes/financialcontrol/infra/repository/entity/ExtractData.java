package br.com.iagoomes.financialcontrol.infra.repository.entity;

import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "extracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "bank", nullable = false)
    private BankType bank;

    @Column(name = "reference_month", nullable = false)
    private Integer referenceMonth;

    @Column(name = "reference_year", nullable = false)
    private Integer referenceYear;

    @Column(name = "total_income", precision = 15, scale = 2)
    private BigDecimal totalIncome;

    @Column(name = "total_expenses", precision = 15, scale = 2)
    private BigDecimal totalExpenses;

    @Column(name = "transaction_count")
    private Integer transactionCount;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @OneToMany(mappedBy = "extract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TransactionData> transactions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (processedAt == null) {
            processedAt = LocalDateTime.now();
        }
    }
}
