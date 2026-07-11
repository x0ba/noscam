import type { ComponentProps } from "react";

import { cn } from "@/lib/utils";

export function Kbd({ className, ...props }: ComponentProps<"kbd">) {
  return (
    <kbd
      data-slot="kbd"
      className={cn(
        "inline-flex h-5 min-w-5 items-center justify-center rounded-[var(--radius-sm)] border border-border bg-muted px-1 font-sans text-xs font-medium leading-none text-muted-foreground tabular-nums",
        className,
      )}
      {...props}
    />
  );
}

export function KbdGroup({
  keys,
  className,
  join = "then",
}: {
  keys: readonly string[];
  className?: string;
  join?: "then" | "or";
}) {
  return (
    <span className={cn("inline-flex items-center gap-0.5", className)}>
      {keys.map((key, index) => (
        <span key={`${key}-${index}`} className="inline-flex items-center gap-0.5">
          {index > 0 ? (
            join === "then" ? (
              <span className="px-0.5 text-xs text-muted-foreground/70" aria-hidden>
                then
              </span>
            ) : (
              <span className="px-0.5 text-xs text-muted-foreground/70" aria-hidden>
                /
              </span>
            )
          ) : null}
          <Kbd>{key}</Kbd>
        </span>
      ))}
    </span>
  );
}
