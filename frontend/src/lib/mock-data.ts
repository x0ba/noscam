/** UI-only shapes aligned with the backend Account / Transaction models. */

export type SourceType = "PLAID" | "CSV";

export type AccountType = "CREDIT_CARD" | "CHECKING" | "SAVINGS" | "INVESTMENT";

export type RiskLevel = "low" | "medium" | "high";

export type ConnectedAccount = {
  id: string;
  bank: string;
  accountName: string;
  accountType: AccountType;
  balance: number;
  currency: string;
  sourceType: SourceType;
  connectedAt: string;
};

export type Transaction = {
  id: string;
  accountId: string;
  merchant: string;
  amount: number;
  currency: string;
  date: string;
  sourceType: SourceType;
  /** 0–100. Higher = more likely scam / suspicious. */
  riskScore: number;
  riskLevel: RiskLevel;
  riskReason: string;
};

export const SAMPLE_ACCOUNTS: ConnectedAccount[] = [
  {
    id: "acc_chase_checking",
    bank: "Chase",
    accountName: "Everyday Checking",
    accountType: "CHECKING",
    balance: 2840.12,
    currency: "USD",
    sourceType: "PLAID",
    connectedAt: "Jun 12, 2026",
  },
  {
    id: "acc_amex_csv",
    bank: "American Express",
    accountName: "Blue Cash Preferred",
    accountType: "CREDIT_CARD",
    balance: -412.55,
    currency: "USD",
    sourceType: "CSV",
    connectedAt: "Jul 2, 2026",
  },
];

export const SAMPLE_TRANSACTIONS: Transaction[] = [
  {
    id: "txn_01",
    accountId: "acc_amex_csv",
    merchant: "AMZN REFUND CONFIRM",
    amount: -47.82,
    currency: "USD",
    date: "Jul 9, 2026",
    sourceType: "CSV",
    riskScore: 92,
    riskLevel: "high",
    riskReason: "Unsolicited refund pattern with unusual merchant name.",
  },
  {
    id: "txn_02",
    accountId: "acc_chase_checking",
    merchant: "VENMO PAYMENT *ALEXM",
    amount: -38.0,
    currency: "USD",
    date: "Jul 8, 2026",
    sourceType: "PLAID",
    riskScore: 61,
    riskLevel: "medium",
    riskReason: "Peer payment to a new counterparty.",
  },
  {
    id: "txn_03",
    accountId: "acc_chase_checking",
    merchant: "BLUE BOTTLE SF MISSION",
    amount: -6.45,
    currency: "USD",
    date: "Jul 8, 2026",
    sourceType: "PLAID",
    riskScore: 8,
    riskLevel: "low",
    riskReason: "Matches a normal local merchant charge.",
  },
  {
    id: "txn_04",
    accountId: "acc_amex_csv",
    merchant: "CUSTOMS FEE *PKGHOLD",
    amount: -1.99,
    currency: "USD",
    date: "Jul 7, 2026",
    sourceType: "CSV",
    riskScore: 88,
    riskLevel: "high",
    riskReason: "Tiny customs-fee charges are a common scam lure.",
  },
  {
    id: "txn_05",
    accountId: "acc_chase_checking",
    merchant: "PG&E UTILITY",
    amount: -94.2,
    currency: "USD",
    date: "Jul 5, 2026",
    sourceType: "PLAID",
    riskScore: 12,
    riskLevel: "low",
    riskReason: "Recurring utility payment on a known schedule.",
  },
  {
    id: "txn_06",
    accountId: "acc_amex_csv",
    merchant: "APPLE.COM/BILL",
    amount: -14.99,
    currency: "USD",
    date: "Jul 4, 2026",
    sourceType: "CSV",
    riskScore: 18,
    riskLevel: "low",
    riskReason: "Familiar digital subscription merchant.",
  },
  {
    id: "txn_07",
    accountId: "acc_chase_checking",
    merchant: "WIRE TO UNKNOWN *INTL",
    amount: -500.0,
    currency: "USD",
    date: "Jul 3, 2026",
    sourceType: "PLAID",
    riskScore: 79,
    riskLevel: "high",
    riskReason: "Large outbound wire to an unrecognized recipient.",
  },
  {
    id: "txn_08",
    accountId: "acc_chase_checking",
    merchant: "TRADER JOE'S #142",
    amount: -52.18,
    currency: "USD",
    date: "Jul 2, 2026",
    sourceType: "PLAID",
    riskScore: 5,
    riskLevel: "low",
    riskReason: "Ordinary grocery purchase.",
  },
];

export function riskLevelFor(score: number): RiskLevel {
  if (score >= 70) return "high";
  if (score >= 40) return "medium";
  return "low";
}

export function formatMoney(amount: number, currency = "USD") {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency,
  }).format(amount);
}

export function accountTypeLabel(type: AccountType) {
  switch (type) {
    case "CREDIT_CARD":
      return "Credit card";
    case "CHECKING":
      return "Checking";
    case "SAVINGS":
      return "Savings";
    case "INVESTMENT":
      return "Investment";
  }
}
