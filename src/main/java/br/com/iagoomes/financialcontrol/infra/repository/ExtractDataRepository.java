package br.com.iagoomes.financialcontrol.infra.repository;

import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.infra.repository.entity.ExtractData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExtractDataRepository extends JpaRepository<ExtractData, String> {

    /**
     * Find extracts by bank type
     */
    List<ExtractData> findByBankOrderByProcessedAtDesc(BankType bank);

    /**
     * Find extracts by year
     */
    List<ExtractData> findByReferenceYearOrderByReferenceMonthDesc(Integer year);

    /**
     * Find extract by bank, month and year
     */
    Optional<ExtractData> findByBankAndReferenceMonthAndReferenceYear(
            BankType bank, Integer month, Integer year);

    /**
     * Find extracts by year and month range
     */
    @Query("SELECT e FROM ExtractData e WHERE e.referenceYear = :year " +
            "AND e.referenceMonth BETWEEN :startMonth AND :endMonth " +
            "ORDER BY e.referenceMonth ASC")
    List<ExtractData> findByYearAndMonthRange(@Param("year") Integer year,
                                              @Param("startMonth") Integer startMonth,
                                              @Param("endMonth") Integer endMonth);

    /**
     * Get all extracts ordered by most recent
     */
    List<ExtractData> findAllByOrderByProcessedAtDesc();
}