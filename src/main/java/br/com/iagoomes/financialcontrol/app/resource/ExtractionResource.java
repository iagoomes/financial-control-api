package br.com.iagoomes.financialcontrol.app.resource;

import br.com.iagoomes.financialcontrol.api.ExtractsApiDelegate;
import br.com.iagoomes.financialcontrol.model.ExtractAnalysisResponse;
import br.com.iagoomes.financialcontrol.model.ExtractSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExtractionResource implements ExtractsApiDelegate {


    @Override
    public CompletableFuture<ResponseEntity<ExtractAnalysisResponse>> getExtractById(UUID extractId) {
        return ExtractsApiDelegate.super.getExtractById(extractId);
    }

    @Override
    public CompletableFuture<ResponseEntity<List<ExtractSummary>>> listExtracts(String bank, Integer year, Integer month) {
        return ExtractsApiDelegate.super.listExtracts(bank, year, month);
    }

    @Override
    public CompletableFuture<ResponseEntity<ExtractAnalysisResponse>> uploadExtract(MultipartFile file, String bank, Integer month, Integer year) {
        log.info("Receiving extract upload: bank={}, month={}, year={}, fileName={}", bank, month, year, file != null ? file.getOriginalFilename() : "null");
        return ExtractsApiDelegate.super.uploadExtract(file, bank, month, year);
    }
}
