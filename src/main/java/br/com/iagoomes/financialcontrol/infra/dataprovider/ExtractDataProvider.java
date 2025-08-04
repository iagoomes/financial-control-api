package br.com.iagoomes.financialcontrol.infra.dataprovider;

import br.com.iagoomes.financialcontrol.domain.ExtractProvider;
import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import br.com.iagoomes.financialcontrol.domain.mapper.DomainMapper;
import br.com.iagoomes.financialcontrol.infra.repository.ExtractDataRepository;
import br.com.iagoomes.financialcontrol.infra.repository.entity.ExtractData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExtractDataProvider implements ExtractProvider {

    private final ExtractDataRepository extractRepository;
    private final DomainMapper domainMapper;

    @Override
    public Optional<Extract> findByBankAndPeriod(BankType bankType, Integer month, Integer year) {
        Optional<ExtractData> extractData = extractRepository.findByBankAndReferenceMonthAndReferenceYear(
                bankType, month, year);

        return extractData.map(domainMapper::toExtractDomain);
    }

    @Override
    public Extract save(Extract extract) {
        ExtractData extractData = domainMapper.toExtractData(extract);
        ExtractData savedExtractData = extractRepository.save(extractData);
        return domainMapper.toExtractDomain(savedExtractData);
    }
}
