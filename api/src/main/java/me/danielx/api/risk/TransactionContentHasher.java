package me.danielx.api.risk;

import me.danielx.api.common.utils.Sha256Util;
import me.danielx.api.transactions.Transaction;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

public final class TransactionContentHasher {
  private TransactionContentHasher() {}

  public static String hash(Transaction transaction) {
    String payload =
        String.join(
            "|",
            nullSafe(transaction.getMerchant()),
            nullSafe(transaction.getDisplayName()),
            nullSafe(transaction.getOriginalDescription()),
            amount(transaction.getAmount()),
            nullSafe(transaction.getCurrencyCode()).toUpperCase(Locale.ROOT),
            String.valueOf(transaction.isPending()),
            Objects.toString(transaction.getPostedAt(), ""),
            Objects.toString(transaction.getAuthorizedAt(), ""),
            nullSafe(transaction.getCategory()),
            nullSafe(transaction.getPaymentChannel()),
            nullSafe(transaction.getMerchantCountry()),
            transaction.getStatus().name());
    return Sha256Util.sha256(payload);
  }

  private static String amount(BigDecimal value) {
    return value == null ? "" : value.stripTrailingZeros().toPlainString();
  }

  private static String nullSafe(String value) {
    return value == null ? "" : value;
  }
}
