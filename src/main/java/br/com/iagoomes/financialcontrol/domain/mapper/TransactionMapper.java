package br.com.iagoomes.financialcontrol.domain.mapper;

import br.com.iagoomes.financialcontrol.domain.entity.Transaction;
import br.com.iagoomes.financialcontrol.infra.repository.entity.TransactionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    private final CategoryMapper categoryMapper;

    @Autowired
    public TransactionMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
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
        transaction.setCategory(categoryMapper.toCategoryDomain(transactionData.getCategory()));
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
                .category(categoryMapper.toCategoryData(transaction.getCategory()))
                .build();
    }
}

