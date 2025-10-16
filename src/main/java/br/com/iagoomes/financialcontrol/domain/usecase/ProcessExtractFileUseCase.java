package br.com.iagoomes.financialcontrol.domain.usecase;

import br.com.iagoomes.financialcontrol.domain.ExtractProvider;
import br.com.iagoomes.financialcontrol.domain.UserProvider;
import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.domain.entity.Category;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import br.com.iagoomes.financialcontrol.domain.entity.User;
import br.com.iagoomes.financialcontrol.infra.exception.BusinessException;
import br.com.iagoomes.financialcontrol.infra.strategy.FileProcessorStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessExtractFileUseCase {

    private final Map<BankType, FileProcessorStrategy> fileProcessors;
    private final ExtractProvider extractProvider;
    private final CategorizeTransactionUseCase categorizeTransactionUseCase;
    private final UserProvider userProvider;

    public Extract execute(String userId, MultipartFile file, BankType bankType, Integer month, Integer year) {
        log.debug("Executing ProcessExtractFileUseCase for user: {}, bank: {}, period: {}/{}", userId, bankType, month, year);

        // Validate user exists
        User user = userProvider.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found: " + userId));

        // Get processor for bank type
        FileProcessorStrategy processor = fileProcessors.get(bankType);
        if (processor == null) {
            throw new UnsupportedOperationException("Bank not supported: " + bankType);
        }

        // Process file and get extract with transactions
        Extract extract = processor.processFile(file, month, year, user);

        // Auto-categorize transactions
        extract.getTransactions().forEach(tx -> {
            Optional<Category> optionalCategory = categorizeTransactionUseCase.execute(tx.getTitle(), tx.getAmount());
            if (optionalCategory.isPresent()) {
                tx.setCategory(optionalCategory.get());
                tx.setConfidence(BigDecimal.valueOf(0.85)); // 85% confidence for auto-categorization
                log.debug("Auto-categorized '{}' as '{}'", tx.getTitle(), optionalCategory.get().getName());
            } else {
                tx.setConfidence(BigDecimal.valueOf(0.0)); // No categorization
            }
        });

        // Check if extract already exists for this user, bank, and period
        Optional<Extract> existingExtract = extractProvider.findByUserBankAndPeriod(userId, bankType, month, year);
        if (existingExtract.isPresent()) {
            throw new BusinessException("Extract already exists for this user, bank, and period");
        }

        // Save and return
        Extract savedExtract = extractProvider.save(extract);
        log.info("Extract processed successfully for user: {}, extractId: {}", userId, savedExtract.getId());

        return savedExtract;
    }
}