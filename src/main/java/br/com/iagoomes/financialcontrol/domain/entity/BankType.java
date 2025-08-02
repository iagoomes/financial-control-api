package br.com.iagoomes.financialcontrol.domain.entity;

public enum BankType {
    NUBANK("Nubank"),
    SANTANDER("Santander"),
    ITAU("Itaú"),
    BRADESCO("Bradesco"),
    CAIXA("Caixa Econômica Federal"),
    BB("Banco do Brasil");

    private final String displayName;

    BankType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}