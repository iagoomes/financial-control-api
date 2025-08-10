package br.com.iagoomes.financialcontrol.app.service;

import br.com.iagoomes.financialcontrol.app.mapper.AppMapper;
import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import br.com.iagoomes.financialcontrol.domain.usecase.ProcessExtractFileUseCase;
import br.com.iagoomes.financialcontrol.domain.usecase.GetExtractByIdUseCase;
import br.com.iagoomes.financialcontrol.domain.usecase.ListExtractsUseCase;
import br.com.iagoomes.financialcontrol.model.ExtractAnalysisResponse;
import br.com.iagoomes.financialcontrol.model.ExtractSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service - Orchestrates Use Cases and handles DTOs
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExtractService {

    private final ProcessExtractFileUseCase processExtractFileUseCase;
    private final GetExtractByIdUseCase getExtractByIdUseCase;
    private final ListExtractsUseCase listExtractsUseCase;
    private final AppMapper appMapper;

    /**
     * Process extract file upload
     */
    public ExtractAnalysisResponse processExtractFile(MultipartFile file, String bank, Integer month, Integer year) {
        log.info("Processing extract file: bank={}, month={}, year={}", bank, month, year);

        validateUploadParameters(file, bank, month, year);

        BankType bankType = BankType.valueOf(bank.toUpperCase());
        Extract extract = processExtractFileUseCase.execute(file, bankType, month, year);

        return appMapper.toExtractAnalysisResponse(extract);
    }

    /**
     * Get extract by ID
     */
    public Optional<ExtractAnalysisResponse> getExtractById(UUID extractId) {
        log.info("Getting extract by ID: {}", extractId);

        Optional<Extract> extract = getExtractByIdUseCase.execute(extractId.toString());
        return extract.map(appMapper::toExtractAnalysisResponse);
    }

    /**
     * List extracts with filters
     */
    public List<ExtractSummary> listExtracts(String bank, Integer year, Integer month) {
        log.info("Listing extracts with filters - bank: {}, year: {}, month: {}", bank, year, month);

        BankType bankType = bank != null ? BankType.valueOf(bank.toUpperCase()) : null;
        List<Extract> extracts = listExtractsUseCase.execute(bankType, year, month);

        return extracts.stream()
                .map(appMapper::toExtractSummary)
                .toList();
    }

    /**
     * Validate upload parameters
     */
    private void validateUploadParameters(MultipartFile file, String bank, Integer month, Integer year) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        if (bank == null || bank.trim().isEmpty()) {
            throw new IllegalArgumentException("Bank is required");
        }

        if (month == null || month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        if (year == null || year < 2020 || year > 2030) {
            throw new IllegalArgumentException("Year must be between 2020 and 2030");
        }

        // Validate bank type
        try {
            BankType.valueOf(bank.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid bank type: " + bank);
        }
    }
}