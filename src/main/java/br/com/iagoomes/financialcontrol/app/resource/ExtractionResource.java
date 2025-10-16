package br.com.iagoomes.financialcontrol.app.resource;

import br.com.iagoomes.financialcontrol.api.ExtractsApiDelegate;
import br.com.iagoomes.financialcontrol.app.service.ExtractService;
import br.com.iagoomes.financialcontrol.model.ExtractAnalysisResponse;
import br.com.iagoomes.financialcontrol.model.ExtractSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Resource implementation for Extracts API - calls Application Service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExtractionResource implements ExtractsApiDelegate {

    private final ExtractService extractService; // ✅ Chama Application Service

    @Override
    public CompletableFuture<ResponseEntity<ExtractAnalysisResponse>> getUserExtractById(UUID userId, UUID extractId) {
        try {
            log.info("Resource: Fetching extract by ID: {} for user: {}", extractId, userId);

            Optional<ExtractAnalysisResponse> response = extractService.getExtractById(userId, extractId);

            return response.map(extractAnalysisResponse -> CompletableFuture.completedFuture(ResponseEntity.ok(extractAnalysisResponse))).orElseGet(() -> CompletableFuture.completedFuture(ResponseEntity.notFound().build()));

        } catch (Exception e) {
            log.error("Resource: Error fetching extract by ID: {} for user: {}", extractId, userId, e);
            return CompletableFuture.completedFuture(ResponseEntity.internalServerError().build());
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<ExtractSummary>>> listUserExtracts(UUID userId,
                                                                                    String bank,
                                                                                    Integer year,
                                                                                    Integer month) {
        try {
            log.info("Resource: Listing extracts for user: {} with filters - bank: {}, year: {}, month: {}", userId, bank, year, month);

            List<ExtractSummary> summaries = extractService.listExtracts(userId, bank, year, month);

            return CompletableFuture.completedFuture(ResponseEntity.ok(summaries));

        } catch (IllegalArgumentException e) {
            log.warn("Resource: Invalid parameters: {}", e.getMessage());
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().build());

        } catch (Exception e) {
            log.error("Resource: Error listing extracts for user: {}", userId, e);
            return CompletableFuture.completedFuture(ResponseEntity.internalServerError().build());
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<ExtractAnalysisResponse>> uploadUserExtract(UUID userId,
                                                                                        MultipartFile file,
                                                                                        String bank,
                                                                                        Integer month,
                                                                                        Integer year) {

        try {
            log.info("Resource: Processing extract upload for user: {}, bank: {}, month: {}, year: {}", userId, bank, month, year);

            ExtractAnalysisResponse response = extractService.processExtractFile(userId, file, bank, month, year);

            return CompletableFuture.completedFuture(ResponseEntity.ok(response));

        } catch (IllegalArgumentException e) {
            log.warn("Resource: Invalid request: {}", e.getMessage());
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().build());

        } catch (Exception e) {
            log.error("Resource: Error processing extract upload for user: {}", userId, e);
            return CompletableFuture.completedFuture(ResponseEntity.internalServerError().build());
        }
    }
}