package br.com.iagoomes.financialcontrol.domain.usecase;

import br.com.iagoomes.financialcontrol.domain.ExtractProvider;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Use case for getting extract by ID (user-scoped)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetExtractByIdUseCase {

    private final ExtractProvider extractProvider;

    public Optional<Extract> execute(String userId, String extractId) {
        log.debug("Executing GetExtractByIdUseCase for user: {}, extractId: {}", userId, extractId);

        Optional<Extract> extract = extractProvider.findByUserAndIdWithTransactions(userId, extractId);

        if (extract.isEmpty()) {
            log.warn("Extract not found with ID: {} for user: {}", extractId, userId);
        }

        return extract;
    }
}