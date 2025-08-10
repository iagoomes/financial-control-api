package br.com.iagoomes.financialcontrol.domain;

import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;

import java.util.List;
import java.util.Optional;

public interface ExtractProvider {
    Optional<Extract> findByBankAndPeriod(BankType bankType, Integer month, Integer year);
    List<Extract> findByBank(BankType bankType);
    List<Extract> findByYear(Integer year);
    List<Extract> findAll();
    Optional<Extract> findByIdWithTransactions(String extractId);
    Extract save(Extract extract);
}
