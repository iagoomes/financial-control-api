package br.com.iagoomes.financialcontrol.domain.usecase;

import br.com.iagoomes.financialcontrol.domain.ExtractProvider;
import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import br.com.iagoomes.financialcontrol.infra.exception.BusinessException;
import br.com.iagoomes.financialcontrol.infra.strategy.FileProcessorStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProcessExtractFileUseCase {

    private final Map<BankType, FileProcessorStrategy> fileProcessors;
    private final ExtractProvider extractProvider;

    public Extract execute(MultipartFile file, BankType bankType, Integer month, Integer year) {
        FileProcessorStrategy processor = fileProcessors.get(bankType);
        if (processor == null) {
            throw new UnsupportedOperationException("Bank not supported: " + bankType);
        }

        Extract extract = processor.processFile(file, month, year);

        Optional<Extract> existingExtract = extractProvider.findByBankAndPeriod(bankType, month, year);
        if (existingExtract.isPresent()) {
            throw new BusinessException("Extract already exists for this period");
        }

        return extractProvider.save(extract);
    }
}