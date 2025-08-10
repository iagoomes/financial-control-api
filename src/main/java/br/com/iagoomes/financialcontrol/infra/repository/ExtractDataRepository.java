package br.com.iagoomes.financialcontrol.infra.repository;

import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.infra.repository.entity.ExtractData;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExtractDataRepository extends JpaRepository<ExtractData, String> {

    @Override
    @EntityGraph(attributePaths = {"transactions"})
    Optional<ExtractData> findById(String id);

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

    List<ExtractData> findByBank(BankType bankType);

    List<ExtractData> findByReferenceYear(Integer referenceYear);

    @Query("SELECT e FROM ExtractData e LEFT JOIN FETCH e.transactions WHERE e.id = :extractId")
    Optional<ExtractData> findByIdWithTransactions(@Param("extractId") String extractId);

}