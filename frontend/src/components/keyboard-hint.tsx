import { useEffect, useState } from "react";

import { Kbd } from "@/src/components/kbd";
import { hasSeenKeyboardHint, markKeyboardHintSeen } from "@/src/lib/keyboard";
import { cn } from "@/lib/utils";

export function KeyboardHint({ visible, onDismiss }: { visible: boolean; onDismiss: () => void }) {
  const [rendered, setRendered] = useState(visible);

  useEffect(() => {
    if (visible) {
      setRendered(true);
      markKeyboardHintSeen();
      const timer = window.setTimeout(() => onDismiss(), 5200);
      return () => window.clearTimeout(timer);
    }

    const hide = window.setTimeout(() => setRendered(false), 180);
    return () => window.clearTimeout(hide);
  }, [visible, onDismiss]);

  if (!rendered) return null;

  return (
    <div
      role="status"
      aria-live="polite"
      className={cn(
        "pointer-events-none fixed inset-x-0 bottom-4 z-50 flex justify-center px-4",
        "motion-safe:transition-[opacity,transform] motion-safe:duration-200 motion-safe:ease-[cubic-bezier(0.16,1,0.3,1)]",
        visible ? "opacity-100 motion-safe:translate-y-0" : "opacity-0 motion-safe:translate-y-1",
      )}
    >
      <p className="inline-flex items-center gap-2 rounded-[var(--radius-md)] border border-border bg-background px-3 py-1.5 text-xs text-muted-foreground shadow-none">
        <span className="text-foreground">Keyboard ready</span>
        <span aria-hidden className="text-border">
          ·
        </span>
        <span className="inline-flex items-center gap-1">
          Press <Kbd>?</Kbd> for shortcuts
        </span>
      </p>
    </div>
  );
}

export function shouldShowKeyboardHint() {
  return !hasSeenKeyboardHint();
}
