package br.com.iagoomes.financialcontrol.app.mapper;

import br.com.iagoomes.financialcontrol.domain.entity.Category;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import br.com.iagoomes.financialcontrol.domain.entity.Transaction;
import br.com.iagoomes.financialcontrol.model.CategoryDTO;
import br.com.iagoomes.financialcontrol.model.CategorySummary;
import br.com.iagoomes.financialcontrol.model.ExtractAnalysisResponse;
import br.com.iagoomes.financialcontrol.model.ExtractSummary;
import br.com.iagoomes.financialcontrol.model.FinancialSummary;
import br.com.iagoomes.financialcontrol.model.PeriodDTO;
import br.com.iagoomes.financialcontrol.model.TransactionDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper between JPA entities and OpenAPI generated DTOs
 */
@Component
public class AppMapper {

    /**
     * Convert Extract entity to ExtractAnalysisResponse DTO
     */
    public ExtractAnalysisResponse toExtractAnalysisResponse(Extract extract) {
        ExtractAnalysisResponse response = new ExtractAnalysisResponse();

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
    public ExtractSummary toExtractSummary(Extract extract) {
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
    private PeriodDTO createPeriod(Integer month, Integer year) {
        PeriodDTO period = new PeriodDTO();
        period.setMonth(month);
        period.setYear(year);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        period.setStartDate(Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        period.setEndDate(Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        return period;
    }

    /**
     * Create FinancialSummary DTO
     */
    private FinancialSummary createFinancialSummary(Extract extract) {
        FinancialSummary summary = new FinancialSummary();

        summary.setTotalIncome(extract.getTotalIncome().doubleValue());
        summary.setTotalExpenses(extract.getTotalExpenses().doubleValue());

        BigDecimal netAmount = extract.getTotalIncome().subtract(extract.getTotalExpenses());
        summary.setNetAmount(netAmount.doubleValue());

        summary.setTransactionCount(extract.getTransactionCount());

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
    private List<TransactionDTO> mapTransactions(List<Transaction> transactions) {
        return transactions.stream()
                .map(this::mapTransaction)
                .toList();
    }

    /**
     * Map single Transaction entity to API Transaction model
     */
    public TransactionDTO mapTransaction(Transaction transaction) {
        TransactionDTO apiTransaction = new TransactionDTO();

        apiTransaction.setId(UUID.fromString(transaction.getId()));
        apiTransaction.setDate(transaction.getDate());
        apiTransaction.setTitle(transaction.getTitle());
        apiTransaction.setAmount(transaction.getAmount().doubleValue());
        apiTransaction.setOriginalDescription(transaction.getOriginalDescription());

        // Set category if available
        if (transaction.getCategory() != null) {
            apiTransaction.setCategory(toCategoryDTO(transaction.getCategory()));
        }

        // Set confidence if available
        if (transaction.getConfidence() != null) {
            apiTransaction.setConfidence(transaction.getConfidence().doubleValue());
        }

        return apiTransaction;
    }

    /**
     * Map Category entity to API Category model
     */
    public CategoryDTO toCategoryDTO(Category category) {
        CategoryDTO apiCategory = new CategoryDTO();

        apiCategory.setId(UUID.fromString(category.getId()));
        apiCategory.setName(category.getName());
        apiCategory.setColor(category.getColor());
        apiCategory.setIcon(category.getIcon());

        if (category.getParentCategoryId() != null) {
            apiCategory.setParentCategoryId(UUID.fromString(category.getParentCategoryId().toString()));
        }

        return apiCategory;
    }

    /**
     * Create category breakdown (placeholder - implement based on business logic)
     */
    private List<CategorySummary> createCategoryBreakdown(Extract extract) {
        if (extract.getTransactions().isEmpty()) {
            return List.of();
        }

        // Calcular total de gastos para percentuais
        double totalExpenses = extract.getTotalExpenses().doubleValue();

        // Agrupar transações por ID da categoria
        Map<String, List<Transaction>> transactionsByCategory = extract.getTransactions().stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(t -> t.getCategory().getId()));

        // Criar um CategorySummary para cada categoria
        return transactionsByCategory.values().stream()
                .map(transactions -> {
                    Category category = transactions.get(0).getCategory();

                    double totalAmount = transactions.stream()
                            .mapToDouble(t -> t.getAmount().doubleValue())
                            .sum();

                    int transactionCount = transactions.size();
                    double percentage = totalExpenses > 0 ? (totalAmount / totalExpenses) * 100 : 0;
                    double averageAmount = transactionCount > 0 ? totalAmount / transactionCount : 0;

                    CategorySummary summary = new CategorySummary();
                    summary.setCategory(toCategoryDTO(category));
                    summary.setTotalAmount(totalAmount);
                    summary.setTransactionCount(transactionCount);
                    summary.setPercentage(percentage);
                    summary.setAverageAmount(averageAmount);

                    return summary;
                })
                .toList();
    }
}