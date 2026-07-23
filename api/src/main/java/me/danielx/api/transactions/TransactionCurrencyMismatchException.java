package me.danielx.api.transactions;

public class TransactionCurrencyMismatchException extends RuntimeException {
  public TransactionCurrencyMismatchException(String transactionCurrency, String accountCurrency) {
    super(
        "Transaction currency "
            + transactionCurrency
            + " does not match account currency "
            + accountCurrency);
  }
}
