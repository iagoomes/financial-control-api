package br.com.iagoomes.financialcontrol.app.resource;

import br.com.iagoomes.financialcontrol.api.ExtractsApiDelegate;
import br.com.iagoomes.financialcontrol.app.mapper.ExtractMapper;
import br.com.iagoomes.financialcontrol.app.service.ExtractService;
import br.com.iagoomes.financialcontrol.app.service.NubankCsvService;
import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.infra.repository.entity.ExtractData;
import br.com.iagoomes.financialcontrol.model.ExtractAnalysisResponse;
import br.com.iagoomes.financialcontrol.model.ExtractSummary;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Delegate implementation for Extracts API
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExtractionResource implements ExtractsApiDelegate {

    private final NubankCsvService nubankCsvService;
    private final ExtractService extractService;
    private final ExtractMapper extractMapper;

    @Override
    @Transactional
    public CompletableFuture<ResponseEntity<ExtractAnalysisResponse>> getExtractById(UUID extractId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Fetching extract by ID: {}", extractId);

                Optional<ExtractData> extractOpt = extractService.findById(extractId.toString());

                if (extractOpt.isEmpty()) {
                    log.warn("Extract not found with ID: {}", extractId);
                    return ResponseEntity.notFound().build();
                }

                ExtractData extract = extractOpt.get();
                ExtractAnalysisResponse response = extractMapper.toExtractAnalysisResponse(extract);

                log.info("Successfully retrieved extract: {}", extractId);
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                log.error("Error fetching extract by ID: {}", extractId, e);
                return ResponseEntity.internalServerError().build();
            }
        });
    }

    @Override
    public CompletableFuture<ResponseEntity<List<ExtractSummary>>> listExtracts(String bank, Integer year, Integer month) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Listing extracts with filters - bank: {}, year: {}, month: {}", bank, year, month);

                List<ExtractData> extracts;

                // Apply filters based on parameters
                if (bank != null && year != null && month != null) {
                    // Find specific extract
                    BankType bankType = BankType.valueOf(bank.toUpperCase());
                    Optional<ExtractData> extractOpt = extractService.findByBankAndPeriod(bankType, month, year);
                    extracts = extractOpt.map(List::of).orElse(List.of());

                } else if (bank != null) {
                    // Filter by bank
                    BankType bankType = BankType.valueOf(bank.toUpperCase());
                    extracts = extractService.findByBank(bankType);

                } else if (year != null) {
                    // Filter by year
                    extracts = extractService.findByYear(year);

                } else {
                    // No filters - get all
                    extracts = extractService.findAll();
                }

                List<ExtractSummary> summaries = extracts.stream()
                        .map(extractMapper::toExtractSummary)
                        .collect(Collectors.toList());

                log.info("Successfully retrieved {} extracts", summaries.size());
                return ResponseEntity.ok(summaries);

            } catch (IllegalArgumentException e) {
                log.warn("Invalid bank parameter: {}", bank, e);
                return ResponseEntity.badRequest().build();

            } catch (Exception e) {
                log.error("Error listing extracts", e);
                return ResponseEntity.internalServerError().build();
            }
        });
    }

    @Override
    public CompletableFuture<ResponseEntity<ExtractAnalysisResponse>> uploadExtract(
            MultipartFile file, String bank, Integer month, Integer year) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Received extract upload request: file={}, bank={}, month={}, year={}",
                        file.getOriginalFilename(), bank, month, year);

                // Validate parameters
                validateUploadParameters(file, bank, month, year);

                ExtractAnalysisResponse response;

                // Process based on bank type
                if ("NUBANK".equalsIgnoreCase(bank)) {
                    response = processNubankCsv(file, month, year);
                } else {
                    log.warn("Unsupported bank type: {}", bank);
                    return ResponseEntity.badRequest().build();
                }

                log.info("Successfully processed extract upload");
                return ResponseEntity.ok(response);

            } catch (IllegalArgumentException e) {
                log.warn("Invalid request for extract upload: {}", e.getMessage());
                return ResponseEntity.badRequest().build();

            } catch (Exception e) {
                log.error("Error processing extract upload", e);
                return ResponseEntity.internalServerError().build();
            }
        });
    }

    /**
     * Process Nubank CSV file
     */
    private ExtractAnalysisResponse processNubankCsv(MultipartFile file, Integer month, Integer year) {
        // Validate CSV file
        nubankCsvService.validateCsvFile(file);

        // Process CSV
        ExtractData extract = nubankCsvService.processCsvFile(file, month, year);

        // Save to database
        ExtractData savedExtract = extractService.saveExtract(extract);

        // Convert to response DTO
        return extractMapper.toExtractAnalysisResponse(savedExtract);
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