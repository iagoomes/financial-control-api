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

    public Optional<Extract> findByBankAndPeriod(BankType bankType, Integer month, Integer year) {
        Optional<ExtractData> extractData = extractRepository.findByBankAndReferenceMonthAndReferenceYear(
                bankType, month, year);

        return extractData.map(extractMapper::toExtractDomain);
    }

    public List<Extract> findByBank(BankType bankType) {
        List<ExtractData> extractDataList = extractRepository.findByBank(bankType);

        return extractDataList.stream()
                .map(extractMapper::toExtractDomain)
                .toList();
    }

    public List<Extract> findByYear(Integer year) {
        List<ExtractData> extractDataList = extractRepository.findByReferenceYear(year);

        return extractDataList.stream()
                .map(extractMapper::toExtractDomain)
                .toList();
    }

    public List<Extract> findAll() {
        List<ExtractData> extractDataList = extractRepository.findAll();

        return extractDataList.stream()
                .map(extractMapper::toExtractDomain)
                .toList();
    }

    public Optional<Extract> findByIdWithTransactions(String extractId) {
        Optional<ExtractData> extractData = extractRepository.findByIdWithTransactions(extractId);

        return extractData.map(extractMapper::toExtractDomain);
    }

    public Extract save(Extract extract) {
        ExtractData extractData = extractMapper.toExtractData(extract);
        ExtractData savedExtractData = extractRepository.save(extractData);
        return extractMapper.toExtractDomain(savedExtractData);
    }

    @Transactional(readOnly = true)
    public List<Extract> findByPeriod(Integer year, Integer month) {
        List<ExtractData> extractDataList = extractRepository.findByReferenceYearAndReferenceMonth(year, month);

        return extractDataList.stream()
                .map(extractMapper::toExtractDomain)
                .toList();
    }

    // --------------------
    // User-scoped implementations
    // --------------------

    @Transactional(readOnly = true)
    public List<Extract> findByUserAndPeriod(String userId, Integer year, Integer month) {
        List<ExtractData> extractDataList = extractRepository
                .findByUser_IdAndReferenceYearAndReferenceMonth(userId, year, month);
        return extractDataList.stream().map(extractMapper::toExtractDomain).toList();
    }

    @Transactional(readOnly = true)
    public Optional<Extract> findByUserAndBankAndPeriod(String userId, BankType bankType, Integer month, Integer year) {
        return extractRepository
                .findByUser_IdAndBankAndReferenceMonthAndReferenceYear(userId, bankType, month, year)
                .map(extractMapper::toExtractDomain);
    }

    @Transactional(readOnly = true)
    public List<Extract> findByUser(String userId) {
        return extractRepository.findByUser_Id(userId).stream()
                .map(extractMapper::toExtractDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Extract> findByUserAndYear(String userId, Integer year) {
        return extractRepository.findByUser_IdAndReferenceYear(userId, year).stream()
                .map(extractMapper::toExtractDomain)
                .toList();
    }
}
