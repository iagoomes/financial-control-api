package br.com.iagoomes.financialcontrol.app.service;

import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.infra.repository.ExtractDataRepository;
import br.com.iagoomes.financialcontrol.infra.repository.entity.ExtractData;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExtractService {

    private final ExtractDataRepository extractRepository;

    /**
     * Save extract to database
     */
    @Transactional
    public ExtractData saveExtract(ExtractData extract) {
        log.info("Saving extract for bank {} - {}/{}",
                extract.getBank(), extract.getReferenceMonth(), extract.getReferenceYear());

        // Check if extract already exists for same bank/month/year
        Optional<ExtractData> existingExtract = extractRepository
                .findByBankAndReferenceMonthAndReferenceYear(
                        extract.getBank(),
                        extract.getReferenceMonth(),
                        extract.getReferenceYear());

        if (existingExtract.isPresent()) {
            log.warn("Extract already exists for bank {} - {}/{}",
                    extract.getBank(), extract.getReferenceMonth(), extract.getReferenceYear());
            throw new IllegalStateException("Extract already exists for this bank and period");
        }

        ExtractData savedExtract = extractRepository.save(extract);
        log.info("Successfully saved extract with ID: {}", savedExtract.getId());

        return savedExtract;
    }

    /**
     * Find extract by ID
     */
    @Transactional
    public Optional<ExtractData> findById(String id) {
        return extractRepository.findById(id);
    }

    /**
     * Find extracts by bank
     */
    public List<ExtractData> findByBank(BankType bank) {
        return extractRepository.findByBankOrderByProcessedAtDesc(bank);
    }

    /**
     * Find extracts by year
     */
    public List<ExtractData> findByYear(Integer year) {
        return extractRepository.findByReferenceYearOrderByReferenceMonthDesc(year);
    }

    /**
     * Find all extracts
     */
    public List<ExtractData> findAll() {
        return extractRepository.findAllByOrderByProcessedAtDesc();
    }

    public Optional<ExtractData> findByBankAndPeriod(BankType bank, Integer month, Integer year) {
        return extractRepository.findByBankAndReferenceMonthAndReferenceYear(bank, month, year);
    }

    /**
     * Find extracts by year and month range
     */
    public List<ExtractData> findByYearAndMonthRange(Integer year, Integer startMonth, Integer endMonth) {
        return extractRepository.findByYearAndMonthRange(year, startMonth, endMonth);
    }

    /**
     * Delete extract by ID
     */
    @Transactional
    public void deleteById(String id) {
        log.info("Deleting extract with ID: {}", id);
        extractRepository.deleteById(id);
    }
}