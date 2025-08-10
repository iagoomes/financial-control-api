package br.com.iagoomes.financialcontrol.domain.usecase;

import br.com.iagoomes.financialcontrol.domain.CategoryProvider;
import br.com.iagoomes.financialcontrol.domain.entity.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Use case for categorizing transactions
 * Contains ONLY business logic, no dependencies on Application Services
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CategorizeTransactionUseCase {

    private final CategoryProvider categoryProvider;

    /**
     * Categorize a transaction based on title and amount
     */
    public Optional<Category> execute(String title, BigDecimal amount) {
        log.debug("Executing CategorizeTransactionUseCase for transaction: {}", title);

        // 1. Determine category name based on business rules (inline)
        String categoryName = determineCategoryName(title.toLowerCase(), amount);

        // 2. Try to find existing category
        Optional<Category> existingCategory = categoryProvider.findByName(categoryName);
        if (existingCategory.isPresent()) {
            log.debug("Found existing category '{}' for transaction '{}'", categoryName, title);
            return existingCategory;
        }

        // 3. Create new category if it doesn't exist
        CategoryDetails details = getCategoryDetails(categoryName);
        Category newCategory = Category.create(details.name(), details.color(), details.icon());

        Category savedCategory = categoryProvider.save(newCategory);
        log.info("Created new category '{}' for transaction '{}'", categoryName, title);

        return Optional.of(savedCategory);
    }

    /**
     * Business rules for determining category name (moved from CategoryService)
     */
    private String determineCategoryName(String titleLower, BigDecimal amount) {
        // Food & Restaurants
        if (isFood(titleLower)) {
            return "Alimentação";
        }

        // Transportation
        if (isTransportation(titleLower)) {
            return "Transporte";
        }

        // Shopping
        if (isShopping(titleLower)) {
            return "Compras";
        }

        // Healthcare
        if (isHealthcare(titleLower)) {
            return "Saúde";
        }

        // Entertainment
        if (isEntertainment(titleLower)) {
            return "Entretenimento";
        }

        // Bills & Utilities
        if (isBill(titleLower)) {
            return "Contas";
        }

        // Default category for unmatched transactions
        return "Outros";
    }

    /**
     * Get category details for a given category name
     */
    private CategoryDetails getCategoryDetails(String categoryName) {
        return switch (categoryName) {
            case "Alimentação" -> new CategoryDetails("Alimentação", "#FF6B6B", "🍽️");
            case "Transporte" -> new CategoryDetails("Transporte", "#4ECDC4", "🚗");
            case "Compras" -> new CategoryDetails("Compras", "#45B7D1", "🛍️");
            case "Saúde" -> new CategoryDetails("Saúde", "#96CEB4", "🏥");
            case "Entretenimento" -> new CategoryDetails("Entretenimento", "#FFEAA7", "🎬");
            case "Contas" -> new CategoryDetails("Contas", "#DDA0DD", "📄");
            default -> new CategoryDetails("Outros", "#95A5A6", "❓");
        };
    }

    private boolean isFood(String title) {
        return title.contains("restaurante") || title.contains("lanchonete") ||
                title.contains("pizzaria") || title.contains("mercado") ||
                title.contains("supermercado") || title.contains("padaria") ||
                title.contains("ifood") || title.contains("uber eats") ||
                title.contains("delivery") || title.contains("hamburgueria") ||
                title.contains("sushi") || title.contains("gratin") ||
                title.contains("au gratin");
    }

    private boolean isTransportation(String title) {
        return title.contains("uber") || title.contains("99") ||
                title.contains("taxi") || title.contains("metro") ||
                title.contains("ônibus") || title.contains("combustível") ||
                title.contains("posto") || title.contains("gasolina") ||
                title.contains("álcool") || title.contains("estacionamento");
    }

    private boolean isShopping(String title) {
        return title.contains("americanas") || title.contains("magazine") ||
                title.contains("casas bahia") || title.contains("shopee") ||
                title.contains("mercado livre") || title.contains("amazon") ||
                title.contains("zara") || title.contains("c&a") ||
                title.contains("renner") || title.contains("shopping") ||
                title.contains("lojas americanas");
    }

    private boolean isHealthcare(String title) {
        return title.contains("farmácia") || title.contains("drogaria") ||
                title.contains("hospital") || title.contains("clínica") ||
                title.contains("médico") || title.contains("dentista") ||
                title.contains("laboratório") || title.contains("exame");
    }

    private boolean isEntertainment(String title) {
        return title.contains("cinema") || title.contains("netflix") ||
                title.contains("spotify") || title.contains("youtube") ||
                title.contains("steam") || title.contains("psn") ||
                title.contains("xbox") || title.contains("bar") ||
                title.contains("balada") || title.contains("show");
    }

    private boolean isBill(String title) {
        return title.contains("energia") || title.contains("água") ||
                title.contains("internet") || title.contains("telefone") ||
                title.contains("aluguel") || title.contains("condomínio") ||
                title.contains("seguro") || title.contains("financiamento") ||
                title.contains("empréstimo") || title.contains("cartão");
    }

    /**
     * Value object for category details
     */
    private record CategoryDetails(String name, String color, String icon) {}
}