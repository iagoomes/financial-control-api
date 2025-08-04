package br.com.iagoomes.financialcontrol.app.service;

import br.com.iagoomes.financialcontrol.app.mapper.AppMapper;
import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import br.com.iagoomes.financialcontrol.domain.usecase.ProcessExtractFileUseCase;
import br.com.iagoomes.financialcontrol.infra.repository.entity.ExtractData;
import br.com.iagoomes.financialcontrol.model.ExtractAnalysisResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExtractService {

    private final ProcessExtractFileUseCase processExtractFileUseCase;
    private final AppMapper appMapper;

    public ExtractAnalysisResponse processExtractFile(MultipartFile file, String bank, Integer month, Integer year) {
        validateUploadParameters(file, bank, month, year);
        Extract extract = processExtractFileUseCase.execute(file, BankType.valueOf(bank.toUpperCase()), month, year);
        return appMapper.toExtractAnalysisResponse(extract);
    }

    /**
     * Save extract to database
     */
    @Transactional
    public ExtractData saveExtract(ExtractData extract) {
        return null;
    }

    /**
     * Find extract by ID
     */
    @Transactional
    public Optional<ExtractData> findById(String id) {
        return null;
    }

    /**
     * Find extracts by bank
     */
    public List<ExtractData> findByBank(BankType bank) {
        return null;
    }

    /**
     * Find extracts by year
     */
    public List<ExtractData> findByYear(Integer year) {
        return null;
    }

    /**
     * Find all extracts
     */
    public List<ExtractData> findAll() {
        return null;
    }

    public Optional<ExtractData> findByBankAndPeriod(BankType bank, Integer month, Integer year) {
        return null;
    }

    /**
     * Find extracts by year and month range
     */
    public List<ExtractData> findByYearAndMonthRange(Integer year, Integer startMonth, Integer endMonth) {
        return null;
    }

    /**
     * Delete extract by ID
     */
    @Transactional
    public void deleteById(String id) {
        log.info("Deleting extract with ID: {}", id);
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