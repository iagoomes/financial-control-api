package br.com.iagoomes.financialcontrol.domain.usecase;

import br.com.iagoomes.financialcontrol.domain.ExtractProvider;
import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Use case for listing extracts with filters (user-scoped)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListExtractsUseCase {

    private final ExtractProvider extractProvider;

    public List<Extract> execute(String userId, BankType bankType, Integer year, Integer month) {
        log.debug("Executing ListExtractsUseCase for user: {} with filters - bank: {}, year: {}, month: {}",
                userId, bankType, year, month);

        // Apply filters based on parameters
        if (bankType != null && year != null && month != null) {
            // Find specific extract
            return extractProvider.findByUserBankAndPeriod(userId, bankType, month, year)
                    .map(List::of)
                    .orElse(List.of());

        } else if (bankType != null) {
            // Filter by bank
            return extractProvider.findByUserAndBank(userId, bankType);

        } else if (year != null && month != null) {
            // Filter by year and month
            return extractProvider.findByUserAndPeriod(userId, year, month);

        } else if (year != null) {
            // Filter by year
            return extractProvider.findByUserAndYear(userId, year);

        } else {
            // No filters - get all for user
            return extractProvider.findAllByUser(userId);
        }
    }
}