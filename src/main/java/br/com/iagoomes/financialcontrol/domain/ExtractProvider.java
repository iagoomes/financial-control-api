package br.com.iagoomes.financialcontrol.domain;

import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;

import java.util.Optional;

public interface ExtractProvider {
    Optional<Extract> findByBankAndPeriod(BankType bankType, Integer month, Integer year);

    Extract save(Extract extract);
}
