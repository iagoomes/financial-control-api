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

    // User-scoped queries
    Optional<ExtractData> findByUser_IdAndId(String userId, String extractId);

    @Query("SELECT e FROM ExtractData e LEFT JOIN FETCH e.transactions WHERE e.user.id = :userId AND e.id = :extractId")
    Optional<ExtractData> findByUser_IdAndIdWithTransactions(@Param("userId") String userId, @Param("extractId") String extractId);

    List<ExtractData> findByUser_Id(String userId);

    List<ExtractData> findByUser_IdAndBank(String userId, BankType bank);

    List<ExtractData> findByUser_IdAndReferenceYear(String userId, Integer year);

    @EntityGraph(attributePaths = {"transactions"})
    List<ExtractData> findByUser_IdAndReferenceYearAndReferenceMonth(String userId, Integer year, Integer month);

    Optional<ExtractData> findByUser_IdAndBankAndReferenceMonthAndReferenceYear(
            String userId, BankType bank, Integer month, Integer year);
}
