import { RiskBadge } from "@/src/components/risk-badge";
import { formatMoney, SAMPLE_ACCOUNTS, SAMPLE_TRANSACTIONS } from "@/src/lib/mock-data";
import { cn } from "@/lib/utils";

const FEATURED = SAMPLE_TRANSACTIONS.find((t) => t.id === "txn_01") ?? SAMPLE_TRANSACTIONS[0];
const NEIGHBORS = SAMPLE_TRANSACTIONS.filter((t) => t.id !== FEATURED.id).slice(0, 3);
const BANK = SAMPLE_ACCOUNTS.find((a) => a.id === FEATURED.accountId)?.bank ?? "American Express";

/** Static product UI fragment for the landing — real rows + detail, not a mock illustration. */
export function ProductVignette() {
  return (
    <div
      className="landing-vignette overflow-hidden rounded-[var(--radius-lg)] border border-border bg-background text-foreground"
      role="img"
      aria-label={`Example Noscam view: ${FEATURED.merchant} flagged high risk at score ${FEATURED.riskScore}`}
    >
      <div className="flex items-center justify-between border-b border-border bg-sidebar px-3 py-2">
        <span className="text-sm font-semibold tracking-tight">Noscam</span>
        <span className="text-xs text-muted-foreground">Transactions</span>
      </div>

      <div className="grid md:grid-cols-[minmax(0,1.15fr)_minmax(12rem,0.85fr)]">
        <ul className="flex flex-col border-border md:border-r" aria-hidden>
          {[FEATURED, ...NEIGHBORS].map((txn, index) => {
            const isSelected = txn.id === FEATURED.id;
            return (
              <li
                key={txn.id}
                className={cn(
                  "flex items-center gap-3 border-b border-border px-3 py-2.5 last:border-b-0",
                  isSelected ? "bg-muted/80" : "bg-background",
                )}
              >
                <span
                  className={cn(
                    "mt-0.5 size-1.5 shrink-0 rounded-full",
                    txn.riskLevel === "high" && "bg-destructive",
                    txn.riskLevel === "medium" && "bg-foreground/40",
                    txn.riskLevel === "low" && "bg-foreground/20",
                  )}
                />
                <span className="min-w-0 flex-1">
                  <span className="block truncate text-sm font-medium">{txn.merchant}</span>
                  <span className="mt-0.5 block truncate text-xs text-muted-foreground">
                    {SAMPLE_ACCOUNTS.find((a) => a.id === txn.accountId)?.bank ?? "Bank"} ·{" "}
                    {txn.date}
                  </span>
                </span>
                <span className="shrink-0 text-right">
                  <span className="block text-sm tabular-nums">
                    {formatMoney(txn.amount, txn.currency)}
                  </span>
                  <span className="mt-0.5 flex justify-end">
                    <RiskBadge level={txn.riskLevel} score={txn.riskScore} />
                  </span>
                </span>
                {index === 0 ? <span className="sr-only">Selected</span> : null}
              </li>
            );
          })}
        </ul>

        <aside className="flex flex-col gap-4 bg-background px-3 py-4" aria-hidden>
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <p className="text-xs font-medium text-muted-foreground">Risk detail</p>
              <p className="mt-1 truncate text-sm font-semibold tracking-tight">
                {FEATURED.merchant}
              </p>
            </div>
            <RiskBadge level={FEATURED.riskLevel} score={FEATURED.riskScore} />
          </div>

          <dl className="grid grid-cols-[auto_1fr] gap-x-3 gap-y-2 text-sm">
            <dt className="text-muted-foreground">Amount</dt>
            <dd className="text-right tabular-nums">
              {formatMoney(FEATURED.amount, FEATURED.currency)}
            </dd>
            <dt className="text-muted-foreground">Date</dt>
            <dd className="text-right">{FEATURED.date}</dd>
            <dt className="text-muted-foreground">Account</dt>
            <dd className="truncate text-right">{BANK}</dd>
          </dl>

          <div className="border-t border-border pt-4">
            <p className="text-sm font-medium">Why this score</p>
            <p className="mt-1 text-sm leading-relaxed text-pretty text-muted-foreground">
              {FEATURED.riskReason}
            </p>
          </div>

          <p className="text-sm leading-relaxed text-pretty text-destructive">
            Treat this as suspicious until you confirm it another way. Don’t reply or send more
            money.
          </p>
        </aside>
      </div>
    </div>
  );
}
