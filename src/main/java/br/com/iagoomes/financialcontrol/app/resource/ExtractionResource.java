package br.com.iagoomes.financialcontrol.app.resource;

import br.com.iagoomes.financialcontrol.api.ExtractsApiDelegate;
import br.com.iagoomes.financialcontrol.app.mapper.AppMapper;
import br.com.iagoomes.financialcontrol.app.service.ExtractService;
import br.com.iagoomes.financialcontrol.infra.strategy.NubankCsvProcessor;
import br.com.iagoomes.financialcontrol.model.ExtractAnalysisResponse;
import br.com.iagoomes.financialcontrol.model.ExtractSummary;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Delegate implementation for Extracts API
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExtractionResource implements ExtractsApiDelegate {

    private final NubankCsvProcessor nubankCsvProcessor;
    private final ExtractService extractService;
    private final AppMapper appMapper;

    @Override
    @Transactional
    public CompletableFuture<ResponseEntity<ExtractAnalysisResponse>> getExtractById(UUID extractId) {
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                log.info("Fetching extract by ID: {}", extractId);
//
//                Optional<ExtractData> extractOpt = extractService.findById(extractId.toString());
//
//                if (extractOpt.isEmpty()) {
//                    log.warn("Extract not found with ID: {}", extractId);
//                    return ResponseEntity.notFound().build();
//                }
//
//                ExtractData extract = extractOpt.get();
//                ExtractAnalysisResponse response = extractMapper.toExtractAnalysisResponse(extract);
//
//                log.info("Successfully retrieved extract: {}", extractId);
//                return ResponseEntity.ok(response);
//
//            } catch (Exception e) {
//                log.error("Error fetching extract by ID: {}", extractId, e);
//                return ResponseEntity.internalServerError().build();
//            }
//        });
        return null;
    }

    @Override
    public CompletableFuture<ResponseEntity<List<ExtractSummary>>> listExtracts(String bank, Integer year, Integer month) {
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                log.info("Listing extracts with filters - bank: {}, year: {}, month: {}", bank, year, month);
//
//                List<ExtractData> extracts;
//
//                // Apply filters based on parameters
//                if (bank != null && year != null && month != null) {
//                    // Find specific extract
//                    BankType bankType = BankType.valueOf(bank.toUpperCase());
//                    Optional<ExtractData> extractOpt = extractService.findByBankAndPeriod(bankType, month, year);
//                    extracts = extractOpt.map(List::of).orElse(List.of());
//
//                } else if (bank != null) {
//                    // Filter by bank
//                    BankType bankType = BankType.valueOf(bank.toUpperCase());
//                    extracts = extractService.findByBank(bankType);
//
//                } else if (year != null) {
//                    // Filter by year
//                    extracts = extractService.findByYear(year);
//
//                } else {
//                    // No filters - get all
//                    extracts = extractService.findAll();
//                }
//
//                List<ExtractSummary> summaries = extracts.stream()
//                        .map(extractMapper::toExtractSummary)
//                        .toList();
//
//                log.info("Successfully retrieved {} extracts", summaries.size());
//                return ResponseEntity.ok(summaries);
//
//            } catch (IllegalArgumentException e) {
//                log.warn("Invalid bank parameter: {}", bank, e);
//                return ResponseEntity.badRequest().build();
//
//            } catch (Exception e) {
//                log.error("Error listing extracts", e);
//                return ResponseEntity.internalServerError().build();
//            }
//        });
        return null;
    }

    @Override
    public CompletableFuture<ResponseEntity<ExtractAnalysisResponse>> uploadExtract(
            MultipartFile file, String bank, Integer month, Integer year) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                ExtractAnalysisResponse response = extractService.processExtractFile(file, bank, month, year);
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            } catch (Exception e) {
                log.error("Error processing extract upload", e);
                return ResponseEntity.internalServerError().build();
            }
        });
    }
}