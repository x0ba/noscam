import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react";

import {
  SAMPLE_ACCOUNTS,
  SAMPLE_TRANSACTIONS,
  riskLevelFor,
  type ConnectedAccount,
  type Transaction,
} from "@/src/lib/mock-data";

type ConnectPhase = "idle" | "connecting" | "importing" | "error";

type DemoState = {
  accounts: ConnectedAccount[];
  transactions: Transaction[];
  connectPhase: ConnectPhase;
  connectError: string | null;
  connectPlaid: () => void;
  importCsv: (fileName: string) => void;
  disconnectAccount: (id: string) => void;
  resetDemo: () => void;
};

const DemoContext = createContext<DemoState | null>(null);

const PLAID_DEMO_ACCOUNT: ConnectedAccount = {
  id: "acc_plaid_demo",
  bank: "Bank of America",
  accountName: "Advantage Plus Banking",
  accountType: "CHECKING",
  balance: 1520.4,
  currency: "USD",
  sourceType: "PLAID",
  connectedAt: "Just now",
};

const PLAID_DEMO_TXNS: Transaction[] = [
  {
    id: "txn_plaid_01",
    accountId: "acc_plaid_demo",
    merchant: "SHELL OIL 5748291",
    amount: -48.2,
    currency: "USD",
    date: "Jul 9, 2026",
    sourceType: "PLAID",
    riskScore: 14,
    riskLevel: "low",
    riskReason: "Ordinary fuel purchase.",
  },
  {
    id: "txn_plaid_02",
    accountId: "acc_plaid_demo",
    merchant: "ACCT VERIFY *SECURELINK",
    amount: -0.01,
    currency: "USD",
    date: "Jul 9, 2026",
    sourceType: "PLAID",
    riskScore: 74,
    riskLevel: "high",
    riskReason: "Micro-charge with a verification-style merchant name.",
  },
];

export function DemoProvider({ children }: { children: ReactNode }) {
  const [accounts, setAccounts] = useState(SAMPLE_ACCOUNTS);
  const [transactions, setTransactions] = useState(SAMPLE_TRANSACTIONS);
  const [connectPhase, setConnectPhase] = useState<ConnectPhase>("idle");
  const [connectError, setConnectError] = useState<string | null>(null);

  const connectPlaid = useCallback(() => {
    setConnectPhase("connecting");
    setConnectError(null);

    window.setTimeout(() => {
      setAccounts((prev) => {
        if (prev.some((a) => a.id === PLAID_DEMO_ACCOUNT.id)) return prev;
        return [PLAID_DEMO_ACCOUNT, ...prev];
      });
      setTransactions((prev) => {
        const existing = new Set(prev.map((t) => t.id));
        const next = PLAID_DEMO_TXNS.filter((t) => !existing.has(t.id));
        return [...next, ...prev];
      });
      setConnectPhase("idle");
    }, 1100);
  }, []);

  const importCsv = useCallback((fileName: string) => {
    if (!fileName.toLowerCase().endsWith(".csv")) {
      setConnectPhase("error");
      setConnectError("Choose a .csv file exported from your bank or card.");
      return;
    }

    setConnectPhase("importing");
    setConnectError(null);

    window.setTimeout(() => {
      const id = `acc_csv_${Date.now()}`;
      const account: ConnectedAccount = {
        id,
        bank: fileName.replace(/\.csv$/i, "").replace(/[_-]+/g, " ") || "CSV import",
        accountName: "Imported transactions",
        accountType: "CREDIT_CARD",
        balance: 0,
        currency: "USD",
        sourceType: "CSV",
        connectedAt: "Just now",
      };

      const score = 55;
      const imported: Transaction = {
        id: `txn_csv_${Date.now()}`,
        accountId: id,
        merchant: "IMPORTED CHARGE *SAMPLE",
        amount: -29.99,
        currency: "USD",
        date: "Jul 9, 2026",
        sourceType: "CSV",
        riskScore: score,
        riskLevel: riskLevelFor(score),
        riskReason: "Imported from CSV — review unfamiliar merchants carefully.",
      };

      setAccounts((prev) => [account, ...prev]);
      setTransactions((prev) => [imported, ...prev]);
      setConnectPhase("idle");
    }, 900);
  }, []);

  const disconnectAccount = useCallback((id: string) => {
    setAccounts((prev) => prev.filter((a) => a.id !== id));
    setTransactions((prev) => prev.filter((t) => t.accountId !== id));
  }, []);

  const resetDemo = useCallback(() => {
    setAccounts(SAMPLE_ACCOUNTS);
    setTransactions(SAMPLE_TRANSACTIONS);
    setConnectPhase("idle");
    setConnectError(null);
  }, []);

  const value = useMemo(
    () => ({
      accounts,
      transactions,
      connectPhase,
      connectError,
      connectPlaid,
      importCsv,
      disconnectAccount,
      resetDemo,
    }),
    [
      accounts,
      transactions,
      connectPhase,
      connectError,
      connectPlaid,
      importCsv,
      disconnectAccount,
      resetDemo,
    ],
  );

  return <DemoContext.Provider value={value}>{children}</DemoContext.Provider>;
}

export function useDemo() {
  const ctx = useContext(DemoContext);
  if (!ctx) throw new Error("useDemo must be used within DemoProvider");
  return ctx;
}
