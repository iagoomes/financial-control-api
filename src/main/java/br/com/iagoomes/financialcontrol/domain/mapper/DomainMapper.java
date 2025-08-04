package br.com.iagoomes.financialcontrol.domain.mapper;

import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import br.com.iagoomes.financialcontrol.domain.entity.Transaction;
import br.com.iagoomes.financialcontrol.infra.repository.entity.ExtractData;
import br.com.iagoomes.financialcontrol.infra.repository.entity.TransactionData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DomainMapper {

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
                    .map(this::toTransactionDomain)
                    .collect(Collectors.toList());
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
                    .map(this::toTransactionData)
                    .collect(Collectors.toList());
            extractData.setTransactions(transactions);

            // Definir o relacionamento bidirecional
            transactions.forEach(transaction -> transaction.setExtract(extractData));
        }

        return extractData;
    }

    public Transaction toTransactionDomain(TransactionData transactionData) {
        if (transactionData == null) {
            return null;
        }

        Transaction transaction = new Transaction();
        transaction.setId(transactionData.getId());
        transaction.setDate(transactionData.getDate());
        transaction.setTitle(transactionData.getTitle());
        transaction.setAmount(transactionData.getAmount());
        transaction.setOriginalDescription(transactionData.getOriginalDescription());
        transaction.setTransactionType(transactionData.getTransactionType());
        transaction.setConfidence(transactionData.getConfidence());

        return transaction;
    }

    public TransactionData toTransactionData(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionData.builder()
                .id(transaction.getId())
                .date(transaction.getDate())
                .title(transaction.getTitle())
                .amount(transaction.getAmount())
                .originalDescription(transaction.getOriginalDescription())
                .transactionType(transaction.getTransactionType())
                .confidence(transaction.getConfidence())
                .build();
    }
}
