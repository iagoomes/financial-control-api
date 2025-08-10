package br.com.iagoomes.financialcontrol.infra.dataprovider;

import br.com.iagoomes.financialcontrol.domain.ExtractProvider;
import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import br.com.iagoomes.financialcontrol.domain.mapper.ExtractMapper;
import br.com.iagoomes.financialcontrol.infra.repository.ExtractDataRepository;
import br.com.iagoomes.financialcontrol.infra.repository.entity.ExtractData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExtractDataProvider implements ExtractProvider {

    private final ExtractDataRepository extractRepository;
    private final ExtractMapper extractMapper;

    @Override
    public Optional<Extract> findByBankAndPeriod(BankType bankType, Integer month, Integer year) {
        Optional<ExtractData> extractData = extractRepository.findByBankAndReferenceMonthAndReferenceYear(
                bankType, month, year);

        return extractData.map(extractMapper::toExtractDomain);
    }

    @Override
    public List<Extract> findByBank(BankType bankType) {
        List<ExtractData> extractDataList = extractRepository.findByBank(bankType);

        return extractDataList.stream()
                .map(extractMapper::toExtractDomain)
                .toList();
    }

    @Override
    public List<Extract> findByYear(Integer year) {
        List<ExtractData> extractDataList = extractRepository.findByReferenceYear(year);

        return extractDataList.stream()
                .map(extractMapper::toExtractDomain)
                .toList();
    }

    @Override
    public List<Extract> findAll() {
        List<ExtractData> extractDataList = extractRepository.findAll();

        return extractDataList.stream()
                .map(extractMapper::toExtractDomain)
                .toList();
    }

    @Override
    public Optional<Extract> findByIdWithTransactions(String extractId) {
        Optional<ExtractData> extractData = extractRepository.findByIdWithTransactions(extractId);

        return extractData.map(extractMapper::toExtractDomain);
    }

    @Override
    public Extract save(Extract extract) {
        ExtractData extractData = extractMapper.toExtractData(extract);
        ExtractData savedExtractData = extractRepository.save(extractData);
        return extractMapper.toExtractDomain(savedExtractData);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Extract> findByPeriod(Integer year, Integer month) {
        List<ExtractData> extractDataList = extractRepository.findByReferenceYearAndReferenceMonth(year, month);

        return extractDataList.stream()
                .map(extractMapper::toExtractDomain)
                .toList();
    }
}
