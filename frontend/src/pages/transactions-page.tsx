import {
  useCallback,
  useEffect,
  useId,
  useMemo,
  useRef,
  useState,
  type KeyboardEvent as ReactKeyboardEvent,
} from "react";
import { Link } from "react-router";

import { Kbd } from "@/src/components/kbd";
import { RiskBadge } from "@/src/components/risk-badge";
import { isModKey, isShortcutBlocked } from "@/src/lib/keyboard";
import { useDemo } from "@/src/lib/demo-state";
import { formatMoney, type RiskLevel, type Transaction } from "@/src/lib/mock-data";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

type Filter = "all" | RiskLevel;

const FILTERS: { id: Filter; label: string }[] = [
  { id: "all", label: "All" },
  { id: "high", label: "High" },
  { id: "medium", label: "Medium" },
  { id: "low", label: "Low" },
];

export function TransactionsPage() {
  const { accounts, transactions } = useDemo();
  const [filter, setFilter] = useState<Filter>("all");
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [activeIndex, setActiveIndex] = useState(0);
  const listRef = useRef<HTMLUListElement>(null);
  const filterRefs = useRef<Array<HTMLButtonElement | null>>([]);
  const rowRefs = useRef<Array<HTMLButtonElement | null>>([]);
  const filtersId = useId();

  const accountById = useMemo(() => new Map(accounts.map((a) => [a.id, a])), [accounts]);

  const sorted = useMemo(() => {
    const list =
      filter === "all" ? transactions : transactions.filter((t) => t.riskLevel === filter);
    return [...list].sort((a, b) => b.riskScore - a.riskScore);
  }, [transactions, filter]);

  const selected = useMemo(() => {
    if (selectedId) {
      const match = sorted.find((t) => t.id === selectedId);
      if (match) return match;
    }
    return sorted[0] ?? null;
  }, [sorted, selectedId]);

  useEffect(() => {
    if (sorted.length === 0) {
      setActiveIndex(0);
      return;
    }
    const selectedIndex = selected ? sorted.findIndex((t) => t.id === selected.id) : 0;
    setActiveIndex(selectedIndex >= 0 ? selectedIndex : 0);
  }, [sorted, selected]);

  const focusRow = useCallback((index: number) => {
    const clamped = Math.max(0, Math.min(index, rowRefs.current.length - 1));
    setActiveIndex(clamped);
    const row = rowRefs.current[clamped];
    row?.focus();
    row?.scrollIntoView({ block: "nearest" });
  }, []);

  const moveSelection = useCallback(
    (index: number) => {
      if (sorted.length === 0) return;
      const clamped = Math.max(0, Math.min(index, sorted.length - 1));
      setSelectedId(sorted[clamped].id);
      focusRow(clamped);
    },
    [sorted, focusRow],
  );

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (isShortcutBlocked(event.target)) return;
      if (isModKey(event) || event.shiftKey) return;

      if (event.key === "/") {
        event.preventDefault();
        const current = FILTERS.findIndex((item) => item.id === filter);
        filterRefs.current[current >= 0 ? current : 0]?.focus();
        return;
      }

      if (sorted.length === 0) return;

      if (event.key === "j") {
        event.preventDefault();
        moveSelection(activeIndex + 1);
        return;
      }

      if (event.key === "k") {
        event.preventDefault();
        moveSelection(activeIndex - 1);
      }
    };

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [filter, sorted.length, activeIndex, moveSelection]);

  const onListKeyDown = (event: ReactKeyboardEvent<HTMLUListElement>) => {
    if (sorted.length === 0) return;
    if (isModKey(event)) return;

    switch (event.key) {
      case "ArrowDown":
        event.preventDefault();
        moveSelection(activeIndex + 1);
        break;
      case "ArrowUp":
        event.preventDefault();
        moveSelection(activeIndex - 1);
        break;
      case "Home":
        event.preventDefault();
        moveSelection(0);
        break;
      case "End":
        event.preventDefault();
        moveSelection(sorted.length - 1);
        break;
      case "Enter":
      case " ":
        event.preventDefault();
        if (sorted[activeIndex]) setSelectedId(sorted[activeIndex].id);
        break;
      default:
        break;
    }
  };

  const onFilterKeyDown = (event: ReactKeyboardEvent<HTMLDivElement>, index: number) => {
    if (isModKey(event)) return;

    let next = index;
    if (event.key === "ArrowRight" || event.key === "ArrowDown") {
      event.preventDefault();
      next = (index + 1) % FILTERS.length;
    } else if (event.key === "ArrowLeft" || event.key === "ArrowUp") {
      event.preventDefault();
      next = (index - 1 + FILTERS.length) % FILTERS.length;
    } else if (event.key === "Home") {
      event.preventDefault();
      next = 0;
    } else if (event.key === "End") {
      event.preventDefault();
      next = FILTERS.length - 1;
    } else {
      return;
    }

    setFilter(FILTERS[next].id);
    setSelectedId(null);
    filterRefs.current[next]?.focus();
  };

  if (accounts.length === 0) {
    return (
      <div className="flex flex-col gap-3 px-4 py-4 sm:px-5">
        <header className="flex flex-col gap-1">
          <h1 className="text-sm font-semibold tracking-tight text-foreground">Transactions</h1>
          <p className="max-w-prose text-sm leading-relaxed text-muted-foreground">
            Every imported charge gets a risk score.
          </p>
        </header>
        <div className="rounded-[var(--radius-sm)] border border-dashed border-border px-4 py-6">
          <h2 className="text-sm font-medium text-foreground">No transactions yet</h2>
          <p className="mt-1 max-w-prose text-sm leading-relaxed text-muted-foreground">
            Link a bank with Plaid or import a CSV first.
          </p>
          <Button size="sm" className="mt-3" render={<Link to="/dashboard/connect" />}>
            Go to Connect
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex h-full min-h-[calc(100dvh-2.75rem)] flex-col md:min-h-dvh lg:flex-row">
      <div className="flex min-w-0 flex-1 flex-col border-border lg:border-r">
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-border px-4 py-2 sm:px-5">
          <div className="flex items-baseline gap-2">
            <h1 className="text-sm font-semibold tracking-tight text-foreground">Transactions</h1>
            <span className="text-xs tabular-nums text-muted-foreground">{sorted.length}</span>
          </div>
          <div className="flex items-center gap-2">
            <div
              role="radiogroup"
              aria-label="Filter by risk"
              id={filtersId}
              className="flex items-center gap-0.5"
              onKeyDown={(event) => {
                const index = FILTERS.findIndex((item) => item.id === filter);
                onFilterKeyDown(event, index >= 0 ? index : 0);
              }}
            >
              {FILTERS.map((item, index) => {
                const checked = filter === item.id;
                return (
                  <button
                    key={item.id}
                    ref={(node) => {
                      filterRefs.current[index] = node;
                    }}
                    type="button"
                    role="radio"
                    aria-checked={checked}
                    tabIndex={checked ? 0 : -1}
                    onClick={() => {
                      setFilter(item.id);
                      setSelectedId(null);
                    }}
                    className={cn(
                      "h-6 rounded-[var(--radius-sm)] px-2 text-xs font-medium transition-colors outline-none focus-visible:ring-2 focus-visible:ring-ring/40",
                      checked
                        ? "bg-muted text-foreground"
                        : "text-muted-foreground hover:bg-muted/70 hover:text-foreground",
                    )}
                  >
                    {item.label}
                  </button>
                );
              })}
            </div>
            <span className="hidden items-center gap-1 text-xs text-muted-foreground sm:inline-flex">
              <Kbd>/</Kbd>
            </span>
          </div>
        </div>

        {sorted.length === 0 ? (
          <p className="px-4 py-6 text-sm text-muted-foreground sm:px-5">
            No transactions match this filter.
          </p>
        ) : (
          <ul
            ref={listRef}
            className="flex flex-col"
            role="listbox"
            aria-label="Transactions"
            onKeyDown={onListKeyDown}
          >
            {sorted.map((txn, index) => {
              const isSelected = selected?.id === txn.id;
              const isActive = index === activeIndex;
              return (
                <li key={txn.id} role="presentation">
                  <button
                    id={`txn-option-${txn.id}`}
                    ref={(node) => {
                      rowRefs.current[index] = node;
                    }}
                    type="button"
                    role="option"
                    aria-selected={isSelected}
                    tabIndex={isActive ? 0 : -1}
                    onClick={() => {
                      setSelectedId(txn.id);
                      setActiveIndex(index);
                    }}
                    onFocus={() => setActiveIndex(index)}
                    className={cn(
                      "group flex w-full items-center gap-3 border-b border-border px-4 py-2 text-left transition-colors outline-none sm:px-5",
                      "focus-visible:bg-muted/80 focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-ring/40",
                      isSelected ? "bg-muted/80" : "hover:bg-muted/50",
                    )}
                  >
                    <span
                      className={cn(
                        "mt-0.5 size-1.5 shrink-0 rounded-full",
                        txn.riskLevel === "high" && "bg-destructive",
                        txn.riskLevel === "medium" && "bg-foreground/40",
                        txn.riskLevel === "low" && "bg-foreground/20",
                      )}
                      aria-hidden
                    />
                    <span className="min-w-0 flex-1">
                      <span className="block truncate text-sm font-medium text-foreground">
                        {txn.merchant}
                      </span>
                      <span className="mt-0.5 block truncate text-xs text-muted-foreground">
                        {accountById.get(txn.accountId)?.bank ?? "Unknown"} · {txn.date}
                      </span>
                    </span>
                    <span className="shrink-0 text-right">
                      <span className="block text-sm tabular-nums text-foreground">
                        {formatMoney(txn.amount, txn.currency)}
                      </span>
                      <span className="mt-0.5 flex justify-end">
                        <RiskBadge level={txn.riskLevel} score={txn.riskScore} />
                      </span>
                    </span>
                  </button>
                </li>
              );
            })}
          </ul>
        )}
      </div>

      {selected ? (
        <TransactionDetail txn={selected} bank={accountById.get(selected.accountId)?.bank} />
      ) : null}
    </div>
  );
}

function TransactionDetail({ txn, bank }: { txn: Transaction; bank?: string }) {
  return (
    <aside
      aria-labelledby="txn-detail-heading"
      className="w-full shrink-0 border-t border-border bg-background lg:w-80 lg:border-t-0"
    >
      <div className="sticky top-0 flex flex-col gap-4 px-4 py-4 sm:px-5">
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0">
            <p className="text-xs font-medium text-muted-foreground">Risk detail</p>
            <h2
              id="txn-detail-heading"
              className="mt-1 truncate text-sm font-semibold tracking-tight text-foreground"
            >
              {txn.merchant}
            </h2>
          </div>
          <RiskBadge level={txn.riskLevel} score={txn.riskScore} />
        </div>

        <dl className="grid grid-cols-[auto_1fr] gap-x-3 gap-y-2 text-sm">
          <dt className="text-muted-foreground">Amount</dt>
          <dd className="text-right tabular-nums text-foreground">
            {formatMoney(txn.amount, txn.currency)}
          </dd>
          <dt className="text-muted-foreground">Date</dt>
          <dd className="text-right text-foreground">{txn.date}</dd>
          <dt className="text-muted-foreground">Account</dt>
          <dd className="truncate text-right text-foreground">{bank ?? "Unknown"}</dd>
          <dt className="text-muted-foreground">Source</dt>
          <dd className="text-right text-foreground">
            {txn.sourceType === "PLAID" ? "Plaid" : "CSV"}
          </dd>
        </dl>

        <div className="border-t border-border pt-4">
          <p className="text-sm font-medium text-foreground">Why this score</p>
          <p className="mt-1 text-sm leading-relaxed text-pretty text-muted-foreground">
            {txn.riskReason}
          </p>
        </div>

        {txn.riskLevel === "high" ? (
          <p className="text-sm leading-relaxed text-pretty text-destructive">
            Treat this as suspicious until you confirm it another way. Don’t reply or send more
            money.
          </p>
        ) : null}

        <p className="text-xs text-muted-foreground">
          <span className="inline-flex items-center gap-1">
            <Kbd>j</Kbd>
            <Kbd>k</Kbd>
          </span>{" "}
          to move · <Kbd>?</Kbd> for all shortcuts
        </p>
      </div>
    </aside>
  );
}
