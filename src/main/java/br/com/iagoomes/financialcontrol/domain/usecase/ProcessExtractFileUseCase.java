package br.com.iagoomes.financialcontrol.domain.usecase;

import br.com.iagoomes.financialcontrol.domain.ExtractProvider;
import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.domain.entity.Category;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;
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

    public Extract execute(MultipartFile file, BankType bankType, Integer month, Integer year) {
        FileProcessorStrategy processor = fileProcessors.get(bankType);
        if (processor == null) {
            throw new UnsupportedOperationException("Bank not supported: " + bankType);
        }

        Extract extract = processor.processFile(file, month, year);
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

        Optional<Extract> existingExtract = extractProvider.findByBankAndPeriod(bankType, month, year);
        if (existingExtract.isPresent()) {
            throw new BusinessException("Extract already exists for this period");
        }

        return extractProvider.save(extract);
    }
}