import { useId, useRef } from "react";
import { FileUpIcon, LandmarkIcon, LoaderCircleIcon, UnplugIcon } from "lucide-react";

import { Kbd } from "@/src/components/kbd";
import { useDemo } from "@/src/lib/demo-state";
import { accountTypeLabel, formatMoney, type ConnectedAccount } from "@/src/lib/mock-data";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";

export function ConnectPage() {
  const {
    accounts,
    connectPhase,
    connectError,
    connectPlaid,
    importCsv,
    disconnectAccount,
    resetDemo,
  } = useDemo();
  const fileInputId = useId();
  const fileRef = useRef<HTMLInputElement>(null);
  const busy = connectPhase === "connecting" || connectPhase === "importing";

  return (
    <div className="flex flex-col">
      <div className="flex flex-wrap items-end justify-between gap-2 border-b border-border px-4 py-2 sm:px-5">
        <div className="flex flex-col gap-0.5">
          <h1 className="text-sm font-semibold tracking-tight text-foreground">Connect</h1>
          <p className="text-xs text-muted-foreground">
            Plaid or CSV — then every charge gets a risk score.
          </p>
        </div>
        <p className="hidden items-center gap-1 text-xs text-muted-foreground sm:inline-flex">
          <Kbd>g</Kbd>
          <span className="text-muted-foreground/70">then</span>
          <Kbd>t</Kbd>
          <span>Transactions</span>
        </p>
      </div>

      <div className="flex flex-col gap-8 px-4 py-4 sm:px-5">
        <section aria-labelledby="add-sources" className="flex flex-col gap-3">
          <h2 id="add-sources" className="text-xs font-medium text-muted-foreground">
            Add a source
          </h2>

          <div className="overflow-hidden rounded-[var(--radius-sm)] border border-border">
            <div className="flex flex-col gap-3 px-3.5 py-3 sm:flex-row sm:items-center sm:justify-between sm:gap-6">
              <div className="min-w-0">
                <p className="text-sm font-medium text-foreground">Bank via Plaid</p>
                <p className="mt-0.5 text-xs leading-relaxed text-muted-foreground">
                  Link checking, savings, or a card. New charges sync and get scored.
                </p>
              </div>
              <Button
                type="button"
                size="sm"
                disabled={busy}
                onClick={connectPlaid}
                className="shrink-0 self-start sm:self-auto"
              >
                {connectPhase === "connecting" ? (
                  <>
                    <LoaderCircleIcon className="animate-spin" data-icon="inline-start" />
                    Connecting…
                  </>
                ) : (
                  <>
                    <LandmarkIcon data-icon="inline-start" />
                    Connect
                  </>
                )}
              </Button>
            </div>

            <div className="h-px bg-border" />

            <div className="flex flex-col gap-3 px-3.5 py-3 sm:flex-row sm:items-center sm:justify-between sm:gap-6">
              <div className="min-w-0">
                <p className="text-sm font-medium text-foreground">Statement CSV</p>
                <p className="mt-0.5 text-xs leading-relaxed text-muted-foreground">
                  Upload an export when you can’t link live.
                </p>
              </div>
              <div className="shrink-0 self-start sm:self-auto">
                <Label htmlFor={fileInputId} className="sr-only">
                  CSV file
                </Label>
                <input
                  ref={fileRef}
                  id={fileInputId}
                  type="file"
                  accept=".csv,text/csv"
                  className="sr-only"
                  disabled={busy}
                  onChange={(event) => {
                    const file = event.target.files?.[0];
                    if (file) importCsv(file.name);
                    event.target.value = "";
                  }}
                />
                <Button
                  type="button"
                  size="sm"
                  variant="outline"
                  disabled={busy}
                  onClick={() => fileRef.current?.click()}
                >
                  {connectPhase === "importing" ? (
                    <>
                      <LoaderCircleIcon className="animate-spin" data-icon="inline-start" />
                      Importing…
                    </>
                  ) : (
                    <>
                      <FileUpIcon data-icon="inline-start" />
                      Choose CSV
                    </>
                  )}
                </Button>
              </div>
            </div>
          </div>

          {connectError ? (
            <p role="alert" className="text-sm text-destructive">
              {connectError}
            </p>
          ) : null}
        </section>

        <section aria-labelledby="connected" className="flex flex-col gap-3">
          <div className="flex items-baseline justify-between gap-3">
            <h2 id="connected" className="text-xs font-medium text-muted-foreground">
              Connected sources
            </h2>
            {accounts.length > 0 ? (
              <span className="text-xs tabular-nums text-muted-foreground">{accounts.length}</span>
            ) : null}
          </div>

          {accounts.length === 0 ? (
            <div className="rounded-[var(--radius-sm)] border border-dashed border-border px-4 py-6">
              <h3 className="text-sm font-medium text-foreground">Nothing connected yet</h3>
              <p className="mt-1 max-w-prose text-sm leading-relaxed text-muted-foreground">
                Connect a bank or import a CSV to start scoring transactions.
              </p>
              <Button type="button" variant="ghost" size="sm" className="mt-3" onClick={resetDemo}>
                Restore sample data
              </Button>
            </div>
          ) : (
            <ul className="overflow-hidden rounded-[var(--radius-sm)] border border-border">
              {accounts.map((account, index) => (
                <AccountRow
                  key={account.id}
                  account={account}
                  onDisconnect={() => disconnectAccount(account.id)}
                  disabled={busy}
                  bordered={index > 0}
                />
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}

function AccountRow({
  account,
  onDisconnect,
  disabled,
  bordered,
}: {
  account: ConnectedAccount;
  onDisconnect: () => void;
  disabled: boolean;
  bordered: boolean;
}) {
  return (
    <li
      className={`flex flex-wrap items-center justify-between gap-3 px-3.5 py-2.5 ${bordered ? "border-t border-border" : ""}`}
    >
      <div className="flex min-w-0 flex-col gap-0.5">
        <div className="flex flex-wrap items-center gap-1.5">
          <p className="text-sm font-medium text-foreground">{account.bank}</p>
          <Badge variant="outline">{account.sourceType === "PLAID" ? "Plaid" : "CSV"}</Badge>
        </div>
        <p className="text-xs text-muted-foreground">
          {account.accountName} · {accountTypeLabel(account.accountType)} ·{" "}
          <span className="tabular-nums">{formatMoney(account.balance, account.currency)}</span>
        </p>
      </div>
      <Button type="button" variant="ghost" size="xs" disabled={disabled} onClick={onDisconnect}>
        <UnplugIcon data-icon="inline-start" />
        Disconnect
      </Button>
    </li>
  );
}
