import { useEffect, useRef } from "react";

import { KbdGroup } from "@/src/components/kbd";
import { SHORTCUT_GROUPS } from "@/src/lib/keyboard";
import { Button } from "@/components/ui/button";

export function KeyboardShortcutsDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}) {
  const dialogRef = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) return;

    if (open) {
      if (!dialog.open) dialog.showModal();
    } else if (dialog.open) {
      dialog.close();
    }
  }, [open]);

  return (
    <dialog
      ref={dialogRef}
      aria-labelledby="keyboard-shortcuts-title"
      className="fixed inset-0 z-50 m-auto max-h-[min(32rem,calc(100dvh-2rem))] w-[min(22rem,calc(100vw-2rem))] overflow-hidden rounded-[var(--radius-md)] border border-border bg-background p-0 text-foreground shadow-none open:flex open:flex-col backdrop:bg-foreground/20"
      onClose={() => onOpenChange(false)}
      onClick={(event) => {
        if (event.target === dialogRef.current) onOpenChange(false);
      }}
      onKeyDown={(event) => {
        if (event.key === "Escape") {
          event.stopPropagation();
        }
      }}
    >
      <div className="flex items-center justify-between border-b border-border px-4 py-2.5">
        <h2 id="keyboard-shortcuts-title" className="text-sm font-semibold tracking-tight">
          Keyboard shortcuts
        </h2>
        <Button
          type="button"
          variant="ghost"
          size="xs"
          onClick={() => onOpenChange(false)}
          aria-label="Close shortcuts"
        >
          Esc
        </Button>
      </div>

      <div className="flex flex-col gap-5 overflow-y-auto px-4 py-4">
        {SHORTCUT_GROUPS.map((group) => (
          <section key={group.title} className="flex flex-col gap-2">
            <h3 className="text-xs font-medium text-muted-foreground">{group.title}</h3>
            <ul className="flex flex-col gap-1.5">
              {group.shortcuts.map((shortcut) => (
                <li
                  key={`${group.title}-${shortcut.label}-${shortcut.keys.join("-")}`}
                  className="flex items-center justify-between gap-3"
                >
                  <span className="text-sm text-foreground">{shortcut.label}</span>
                  <KbdGroup keys={shortcut.keys} join={shortcut.join} />
                </li>
              ))}
            </ul>
          </section>
        ))}
      </div>
    </dialog>
  );
}
