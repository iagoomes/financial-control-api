package br.com.iagoomes.financialcontrol.domain.usecase;

import br.com.iagoomes.financialcontrol.domain.ExtractProvider;
import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Use case for listing extracts with filters
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListExtractsUseCase {

    private final ExtractProvider extractProvider;

    public List<Extract> execute(BankType bankType, Integer year, Integer month) {
        log.debug("Executing ListExtractsUseCase with filters - bank: {}, year: {}, month: {}",
                bankType, year, month);

        // Apply filters based on parameters
        if (bankType != null && year != null && month != null) {
            // Find specific extract
            return extractProvider.findByBankAndPeriod(bankType, month, year)
                    .map(List::of)
                    .orElse(List.of());

        } else if (bankType != null) {
            // Filter by bank
            return extractProvider.findByBank(bankType);

        } else if (year != null) {
            // Filter by year
            return extractProvider.findByYear(year);

        } else {
            // No filters - get all
            return extractProvider.findAll();
        }
    }
}