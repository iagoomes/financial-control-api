package br.com.iagoomes.financialcontrol.domain.mapper;

import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import br.com.iagoomes.financialcontrol.domain.entity.Transaction;
import br.com.iagoomes.financialcontrol.infra.repository.entity.ExtractData;
import br.com.iagoomes.financialcontrol.infra.repository.entity.TransactionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExtractMapper {

    private final TransactionMapper transactionMapper;

    @Autowired
    public ExtractMapper(TransactionMapper transactionMapper) {
        this.transactionMapper = transactionMapper;
    }

    public Extract toExtractDomain(ExtractData extractData) {
        if (extractData == null) {
            return null;
        }
        Extract extract = new Extract();
        extract.setId(extractData.getId());
        extract.setBank(extractData.getBank());
        extract.setReferenceMonth(extractData.getReferenceMonth());
        extract.setReferenceYear(extractData.getReferenceYear());
        extract.setTotalIncome(extractData.getTotalIncome());
        extract.setTotalExpenses(extractData.getTotalExpenses());
        extract.setTransactionCount(extractData.getTransactionCount());
        extract.setProcessedAt(extractData.getProcessedAt());
        if (extractData.getTransactions() != null) {
            List<Transaction> transactions = extractData.getTransactions()
                    .stream()
                    .map(transactionMapper::toTransactionDomain)
                    .toList();
            extract.setTransactions(transactions);
        }
        return extract;
    }

    public ExtractData toExtractData(Extract extract) {
        if (extract == null) {
            return null;
        }
        ExtractData extractData = ExtractData.builder()
                .id(extract.getId())
                .bank(extract.getBank())
                .referenceMonth(extract.getReferenceMonth())
                .referenceYear(extract.getReferenceYear())
                .totalIncome(extract.getTotalIncome())
                .totalExpenses(extract.getTotalExpenses())
                .transactionCount(extract.getTransactionCount())
                .processedAt(extract.getProcessedAt())
                .build();
        if (extract.getTransactions() != null) {
            List<TransactionData> transactions = extract.getTransactions()
                    .stream()
                    .map(transactionMapper::toTransactionData)
                    .toList();
            extractData.setTransactions(transactions);
            transactions.forEach(transaction -> transaction.setExtract(extractData));
        }
        return extractData;
    }
}

