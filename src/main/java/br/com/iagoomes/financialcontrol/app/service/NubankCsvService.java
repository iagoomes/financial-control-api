package br.com.iagoomes.financialcontrol.app.service;

import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.domain.entity.TransactionType;
import br.com.iagoomes.financialcontrol.infra.repository.entity.ExtractData;
import br.com.iagoomes.financialcontrol.infra.repository.entity.TransactionData;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Service for processing Nubank CSV files
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NubankCsvService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Process Nubank CSV file and ExtractData transactions
     *
     * @param file CSV file uploaded
     * @param month Reference month
     * @param year Reference year
     * @return ExtractData with processed transactions
     */
    public ExtractData processCsvFile(MultipartFile file, Integer month, Integer year) {
        log.info("Starting to process Nubank CSV file: {}", file.getOriginalFilename());

        try {
            List<TransactionData> transactions = parseTransactions(file);
            ExtractData ExtractData = createExtract(transactions, month, year);

            log.info("Successfully processed {} transactions from Nubank CSV", transactions.size());
            return ExtractData;

        } catch (Exception e) {
            log.error("Error processing Nubank CSV file", e);
            throw new RuntimeException("Failed to process Nubank CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Parse CSV file and create TransactionData entities
     */
    private List<TransactionData> parseTransactions(MultipartFile file) throws IOException, CsvException {
        List<TransactionData> transactions = new ArrayList<>();

        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                .withSkipLines(1) // Skip header
                .build()) {

            List<String[]> records = reader.readAll();

            for (int i = 0; i < records.size(); i++) {
                String[] record = records.get(i);

                try {
                    TransactionData TransactionData = parseTransactionRecord(record, i + 2); // +2 because of header and 0-based index
                    transactions.add(TransactionData);
                } catch (Exception e) {
                    log.warn("Failed to parse TransactionData at line {}: {}", i + 2, e.getMessage());
                    // Continue processing other transactions
                }
            }
        }

        return transactions;
    }

    /**
     * Parse individual CSV record into TransactionData
     */
    private TransactionData parseTransactionRecord(String[] record, int lineNumber) {
        if (record.length < 3) {
            throw new IllegalArgumentException("Invalid CSV format at line " + lineNumber + ": expected 3 columns, got " + record.length);
        }

        String dateString = record[0].trim();
        String title = record[1].trim();
        String amountString = record[2].trim();

        // Parse date
        LocalDate date;
        try {
            date = LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format at line " + lineNumber + ": " + dateString);
        }

        // Parse amount
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format at line " + lineNumber + ": " + amountString);
        }

        // Determine TransactionData type based on amount and title
        TransactionType transactionType = determineTransactionType(title, amount);

        return TransactionData.builder()
                .date(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .title(title)
                .amount(amount)
                .originalDescription(title)
                .transactionType(transactionType)
                .build();
    }

    /**
     * Determine TransactionData type based on title and amount
     */
    private TransactionType determineTransactionType(String title, BigDecimal amount) {
        String titleLower = title.toLowerCase();

        // Check for specific patterns
        if (titleLower.contains("pix")) {
            return TransactionType.PIX;
        }
        if (titleLower.contains("ted")) {
            return TransactionType.TED;
        }
        if (titleLower.contains("doc")) {
            return TransactionType.DOC;
        }
        if (titleLower.contains("boleto")) {
            return TransactionType.BOLETO;
        }
        if (titleLower.contains("transferência") || titleLower.contains("transfer")) {
            return TransactionType.TRANSFER;
        }
        if (titleLower.contains("pagamento")) {
            return TransactionType.PAYMENT;
        }

        // Default based on amount (Nubank usually uses positive for expenses, negative for income)
        return amount.compareTo(BigDecimal.ZERO) >= 0 ? TransactionType.DEBIT : TransactionType.CREDIT;
    }

    /**
     * Create ExtractData entity with calculated totals
     */
    private ExtractData createExtract(List<TransactionData> transactions, Integer month, Integer year) {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (TransactionData transactionData : transactions) {
            if (transactionData.isIncome()) {
                totalIncome = totalIncome.add(transactionData.getAbsoluteAmount());
            } else {
                totalExpenses = totalExpenses.add(transactionData.getAbsoluteAmount());
            }
        }

        ExtractData extractData = ExtractData.builder()
                .bank(BankType.NUBANK)
                .referenceMonth(month)
                .referenceYear(year)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .transactionCount(transactions.size())
                .transactions(transactions)
                .build();

        transactions.forEach(transactionData -> transactionData.setExtract(extractData));

        return extractData;
    }

    /**
     * Validate CSV file format
     */
    public void validateCsvFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("File must be a CSV file");
        }

        // Check file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 10MB");
        }
    }
}