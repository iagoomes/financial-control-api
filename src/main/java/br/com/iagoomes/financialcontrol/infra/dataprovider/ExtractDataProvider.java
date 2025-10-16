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
    @Transactional(readOnly = true)
    public Optional<Extract> findByUserAndId(String userId, String extractId) {
        return extractRepository.findByUser_IdAndId(userId, extractId)
                .map(extractMapper::toExtractDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Extract> findByUserAndIdWithTransactions(String userId, String extractId) {
        return extractRepository.findByUser_IdAndIdWithTransactions(userId, extractId)
                .map(extractMapper::toExtractDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Extract> findAllByUser(String userId) {
        return extractRepository.findByUser_Id(userId).stream()
                .map(extractMapper::toExtractDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Extract> findByUserAndBank(String userId, BankType bankType) {
        return extractRepository.findByUser_IdAndBank(userId, bankType).stream()
                .map(extractMapper::toExtractDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Extract> findByUserAndYear(String userId, Integer year) {
        return extractRepository.findByUser_IdAndReferenceYear(userId, year).stream()
                .map(extractMapper::toExtractDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Extract> findByUserAndPeriod(String userId, Integer year, Integer month) {
        return extractRepository.findByUser_IdAndReferenceYearAndReferenceMonth(userId, year, month).stream()
                .map(extractMapper::toExtractDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Extract> findByUserBankAndPeriod(String userId, BankType bankType, Integer month, Integer year) {
        return extractRepository.findByUser_IdAndBankAndReferenceMonthAndReferenceYear(userId, bankType, month, year)
                .map(extractMapper::toExtractDomain);
    }

    @Override
    public Extract save(Extract extract) {
        ExtractData extractData = extractMapper.toExtractData(extract);
        ExtractData savedExtractData = extractRepository.save(extractData);
        return extractMapper.toExtractDomain(savedExtractData);
    }
}
