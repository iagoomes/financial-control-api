package br.com.iagoomes.financialcontrol.infra.strategy;

import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import br.com.iagoomes.financialcontrol.domain.entity.Transaction;
import br.com.iagoomes.financialcontrol.domain.entity.TransactionType;
import br.com.iagoomes.financialcontrol.domain.entity.User;
import br.com.iagoomes.financialcontrol.infra.exception.FileProcessingException;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for processing Nubank CSV files with automatic categorization
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NubankCsvProcessor implements FileProcessorStrategy {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Extract processFile(MultipartFile file, Integer month, Integer year, User user) {
        log.info("Starting to process Nubank CSV file: {} for user: {}", file.getOriginalFilename(), user.getId());

        validateFile(file);

        try {
            List<Transaction> transactions = parseTransactions(file);
            Extract extract = Extract.create(transactions, month, year, user);

            log.info("Successfully processed {} transactions from Nubank CSV for user: {}",
                    transactions.size(), user.getId());
            return extract;

        } catch (Exception e) {
            log.error("Error processing Nubank CSV file for user: {}", user.getId(), e);
            throw new FileProcessingException("Failed to process Nubank CSV: " + e.getMessage(), e);
        }
    }

    private List<Transaction> parseTransactions(MultipartFile file) throws IOException, CsvException {
        List<Transaction> transactions = new ArrayList<>();

        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                .withSkipLines(1) // Skip header
                .build()) {

            List<String[]> rows = reader.readAll();

            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);

                try {
                    Transaction transaction = parseCsvRow(row, i + 2);

                    // Ignore credit card invoice payments (e.g., "Pagamento recebido" with negative amount)
                    if (isCreditCardPayment(transaction.getTitle(), transaction.getAmount())) {
                        log.debug("Skipping credit card payment at line {}: {} {}", i + 2, transaction.getTitle(), transaction.getAmount());
                        continue;
                    }

                    transactions.add(transaction);
                } catch (Exception e) {
                    log.warn("Failed to parse transaction at line {}: {}", i + 2, e.getMessage());
                    // Continue processing other transactions
                }
            }
        }

        return transactions;
    }

    private Transaction parseCsvRow(String[] row, int lineNumber) {
        if (row.length < 3) {
            throw new IllegalArgumentException("Invalid CSV format at line " + lineNumber +
                    ": expected 3 columns, got " + row.length);
        }

        String dateString = row[0].trim();
        String title = row[1].trim();
        String amountString = row[2].trim();

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

        // Determine transaction type
        TransactionType transactionType = determineTransactionType(title, amount);

        // Create transaction
        return Transaction.create(date, title, amount, title, transactionType);
    }

    private boolean isCreditCardPayment(String title, BigDecimal amount) {
        if (title == null) return false;
        String t = title.trim().toLowerCase();
        // Typical credit card payment entry on Nubank invoice CSV
        if (t.equals("pagamento recebido")) {
            return true;
        }
        // Any other "pagamento" line with negative amount (credit on the invoice) should also be excluded
        return t.contains("pagamento") && amount != null && amount.signum() < 0;
    }

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

    @Override
    public void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("File must be a CSV file");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 10MB");
        }
    }
}