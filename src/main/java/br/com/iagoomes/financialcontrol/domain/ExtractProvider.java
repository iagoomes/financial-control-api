package br.com.iagoomes.financialcontrol.domain;

import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;

import java.util.List;
import java.util.Optional;

public interface ExtractProvider {
    // User-scoped methods
    Optional<Extract> findByUserAndId(String userId, String extractId);
    Optional<Extract> findByUserAndIdWithTransactions(String userId, String extractId);
    List<Extract> findAllByUser(String userId);
    List<Extract> findByUserAndBank(String userId, BankType bankType);
    List<Extract> findByUserAndYear(String userId, Integer year);
    List<Extract> findByUserAndPeriod(String userId, Integer year, Integer month);
    Optional<Extract> findByUserBankAndPeriod(String userId, BankType bankType, Integer month, Integer year);

    Extract save(Extract extract);
}
