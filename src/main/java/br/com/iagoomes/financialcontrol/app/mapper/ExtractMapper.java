package br.com.iagoomes.financialcontrol.app.mapper;

import br.com.iagoomes.financialcontrol.infra.repository.entity.CategoryData;
import br.com.iagoomes.financialcontrol.infra.repository.entity.ExtractData;
import br.com.iagoomes.financialcontrol.infra.repository.entity.TransactionData;
import br.com.iagoomes.financialcontrol.model.Category;
import br.com.iagoomes.financialcontrol.model.CategorySummary;
import br.com.iagoomes.financialcontrol.model.ExtractAnalysisResponse;
import br.com.iagoomes.financialcontrol.model.ExtractSummary;
import br.com.iagoomes.financialcontrol.model.FinancialSummary;
import br.com.iagoomes.financialcontrol.model.Period;
import br.com.iagoomes.financialcontrol.model.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Mapper between JPA entities and OpenAPI generated DTOs
 */
@Component
public class ExtractMapper {

    /**
     * Convert Extract entity to ExtractAnalysisResponse DTO
     */
    public ExtractAnalysisResponse toExtractAnalysisResponse(ExtractData extract) {
        ExtractAnalysisResponse response = new ExtractAnalysisResponse();

        // Set basic fields
        response.setId(UUID.fromString(extract.getId()));
        response.setBank(ExtractAnalysisResponse.BankEnum.fromValue(extract.getBank().name()));
        response.setPeriod(createPeriod(extract.getReferenceMonth(), extract.getReferenceYear()));
        response.setSummary(createFinancialSummary(extract));
        response.setTransactions(mapTransactions(extract.getTransactions()));
        response.setCategoryBreakdown(createCategoryBreakdown(extract));
        response.setProcessedAt(Date.from(extract.getProcessedAt().atZone(ZoneId.systemDefault()).toInstant()));

        return response;
    }

    /**
     * Convert Extract entity to ExtractSummary DTO
     */
    public ExtractSummary toExtractSummary(ExtractData extract) {
        ExtractSummary summary = new ExtractSummary();

        summary.setId(UUID.fromString(extract.getId()));
        summary.setBank(ExtractSummary.BankEnum.fromValue(extract.getBank().name()));
        summary.setPeriod(createPeriod(extract.getReferenceMonth(), extract.getReferenceYear()));
        summary.setTotalExpenses(extract.getTotalExpenses().doubleValue());
        summary.setTotalIncome(extract.getTotalIncome().doubleValue());
        summary.setTransactionCount(extract.getTransactionCount());
        summary.setProcessedAt(Date.from(extract.getProcessedAt().atZone(ZoneId.systemDefault()).toInstant()));

        return summary;
    }

    /**
     * Create Period DTO
     */
    private Period createPeriod(Integer month, Integer year) {
        Period period = new Period();
        period.setMonth(month);
        period.setYear(year);

        // Calcular datas de início e fim
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        period.setStartDate(Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        period.setEndDate(Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        return period;
    }

    /**
     * Create FinancialSummary DTO
     */
    private FinancialSummary createFinancialSummary(ExtractData extract) {
        FinancialSummary summary = new FinancialSummary();

        summary.setTotalIncome(extract.getTotalIncome().doubleValue());
        summary.setTotalExpenses(extract.getTotalExpenses().doubleValue());

        // Calculate net amount (income - expenses)
        BigDecimal netAmount = extract.getTotalIncome().subtract(extract.getTotalExpenses());
        summary.setNetAmount(netAmount.doubleValue());

        summary.setTransactionCount(extract.getTransactionCount());

        // Calculate average transaction value
        if (extract.getTransactionCount() > 0) {
            BigDecimal totalAmount = extract.getTotalIncome().add(extract.getTotalExpenses());
            BigDecimal average = totalAmount.divide(BigDecimal.valueOf(extract.getTransactionCount()), 2, BigDecimal.ROUND_HALF_UP);
            summary.setAverageTransactionValue(average.doubleValue());
        } else {
            summary.setAverageTransactionValue(0.0);
        }

        return summary;
    }

    /**
     * Map Transaction entities to Transaction DTOs
     */
    private List<Transaction> mapTransactions(List<TransactionData> transactions) {
        return transactions.stream()
                .map(this::mapTransaction)
                .toList();
    }

    /**
     * Map single Transaction entity to Transaction DTO
     */
    private Transaction mapTransaction(TransactionData transaction) {
        Transaction dto = new Transaction();

        dto.setId(UUID.fromString(transaction.getId()));
        dto.setDate(transaction.getDate());
        dto.setTitle(transaction.getTitle());
        dto.setAmount(transaction.getAmount().doubleValue());
        dto.setOriginalDescription(transaction.getOriginalDescription());

        // Set category if available
        if (transaction.getCategory() != null) {
            dto.setCategory(mapCategory(transaction.getCategory()));
        }

        // Set confidence if available
        if (transaction.getConfidence() != null) {
            dto.setConfidence(transaction.getConfidence().doubleValue());
        }

        return dto;
    }

    /**
     * Map Category entity to Category DTO
     */
    private Category mapCategory(CategoryData category) {
        Category dto = new Category();

        dto.setId(UUID.fromString(category.getId()));
        dto.setName(category.getName());
        dto.setColor(category.getColor());
        dto.setIcon(category.getIcon());

        if (category.getParentCategoryId() != null) {
            dto.setParentCategoryId(UUID.fromString(category.getParentCategoryId()));
        }

        return dto;
    }

    /**
     * Create category breakdown (placeholder - implement based on business logic)
     */
    private List<CategorySummary> createCategoryBreakdown(ExtractData extract) {
        // For now, return empty list
        // TODO: Implement category breakdown logic
        return List.of();
    }
}