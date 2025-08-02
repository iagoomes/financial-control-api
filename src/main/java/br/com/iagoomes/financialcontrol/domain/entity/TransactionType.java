package br.com.iagoomes.financialcontrol.domain.entity;

public enum TransactionType {
    DEBIT("Débito"),
    CREDIT("Crédito"),
    PIX("PIX"),
    TED("TED"),
    DOC("DOC"),
    BOLETO("Boleto"),
    TRANSFER("Transferência"),
    PAYMENT("Pagamento");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}