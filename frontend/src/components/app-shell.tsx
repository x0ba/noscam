import { Link, NavLink, Outlet, useLocation, useNavigate } from "react-router";
import { CircleHelpIcon, LandmarkIcon, ListIcon, MenuIcon, XIcon } from "lucide-react";
import { useCallback, useEffect, useId, useRef, useState } from "react";

import { KeyboardHint, shouldShowKeyboardHint } from "@/src/components/keyboard-hint";
import { KeyboardShortcutsDialog } from "@/src/components/keyboard-shortcuts-dialog";
import { Kbd } from "@/src/components/kbd";
import { isModKey, isShortcutBlocked } from "@/src/lib/keyboard";
import { Button } from "@/components/ui/button";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";
import { cn } from "@/lib/utils";

const NAV = [
  { to: "/dashboard", label: "Transactions", icon: ListIcon, end: true },
  { to: "/dashboard/connect", label: "Connect", icon: LandmarkIcon, end: false },
] as const;

export function AppShell() {
  const location = useLocation();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [helpOpen, setHelpOpen] = useState(false);
  const [hintVisible, setHintVisible] = useState(false);
  const navId = useId();
  const goChordRef = useRef(false);
  const goChordTimer = useRef<number | null>(null);
  const menuPanelRef = useRef<HTMLElement>(null);
  const menuTriggerRef = useRef<HTMLButtonElement>(null);

  const dismissHint = useCallback(() => setHintVisible(false), []);

  useEffect(() => {
    setMobileOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    if (!mobileOpen) return;

    const previouslyFocused = document.activeElement as HTMLElement | null;
    const panel = menuPanelRef.current;
    const focusable = panel?.querySelectorAll<HTMLElement>(
      'a[href], button:not([disabled]), [tabindex]:not([tabindex="-1"])',
    );
    focusable?.[0]?.focus();

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        event.preventDefault();
        setMobileOpen(false);
        return;
      }

      if (event.key !== "Tab" || !panel || !focusable?.length) return;

      const items = Array.from(focusable);
      const first = items[0];
      const last = items[items.length - 1];
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    };

    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.removeEventListener("keydown", onKeyDown);
      if (previouslyFocused === menuTriggerRef.current) {
        menuTriggerRef.current?.focus();
      } else {
        previouslyFocused?.focus?.();
      }
    };
  }, [mobileOpen]);

  useEffect(() => {
    const clearGoChord = () => {
      goChordRef.current = false;
      if (goChordTimer.current != null) {
        window.clearTimeout(goChordTimer.current);
        goChordTimer.current = null;
      }
    };

    const onKeyDown = (event: KeyboardEvent) => {
      if (helpOpen) return;
      if (isShortcutBlocked(event.target)) return;
      if (event.defaultPrevented) return;

      const key = event.key;
      const isQuestion = key === "?" || (key === "/" && event.shiftKey);

      if (isQuestion && !event.metaKey && !event.ctrlKey && !event.altKey) {
        event.preventDefault();
        clearGoChord();
        setHelpOpen(true);
        return;
      }

      if (isModKey(event) || event.shiftKey) {
        clearGoChord();
        return;
      }

      if (goChordRef.current) {
        if (key === "t") {
          event.preventDefault();
          void navigate("/dashboard");
        } else if (key === "c") {
          event.preventDefault();
          void navigate("/dashboard/connect");
        }
        clearGoChord();
        return;
      }

      if (key === "g") {
        event.preventDefault();
        goChordRef.current = true;
        goChordTimer.current = window.setTimeout(clearGoChord, 1200);
      }
    };

    const onFirstKeyboard = (event: KeyboardEvent) => {
      if (event.metaKey || event.ctrlKey || event.altKey) return;
      if (event.key === "Tab" || event.key === "ArrowDown" || event.key === "ArrowUp") {
        if (shouldShowKeyboardHint()) setHintVisible(true);
        window.removeEventListener("keydown", onFirstKeyboard, true);
      }
    };

    window.addEventListener("keydown", onKeyDown);
    window.addEventListener("keydown", onFirstKeyboard, true);
    return () => {
      window.removeEventListener("keydown", onKeyDown);
      window.removeEventListener("keydown", onFirstKeyboard, true);
      clearGoChord();
    };
  }, [helpOpen, navigate]);

  return (
    <TooltipProvider>
      <div className="flex min-h-dvh bg-background text-foreground">
        <a
          href="#main"
          className="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 focus:z-50 focus:rounded-[var(--radius-sm)] focus:bg-primary focus:px-2.5 focus:py-1.5 focus:text-xs focus:text-primary-foreground"
        >
          Skip to content
        </a>

        <aside className="hidden w-56 shrink-0 flex-col border-r border-sidebar-border bg-sidebar md:flex">
          <div className="flex h-11 items-center justify-between gap-2 px-3">
            <BrandMark />
            <ShortcutsTrigger onOpen={() => setHelpOpen(true)} />
          </div>
          <nav aria-label="Primary" className="flex flex-1 flex-col gap-0.5 px-2 pb-3">
            {NAV.map((item) => (
              <NavItem key={item.to} {...item} />
            ))}
          </nav>
          <p className="px-4 pb-3 text-xs text-muted-foreground">
            Press <Kbd className="mx-0.5">?</Kbd> for shortcuts
          </p>
        </aside>

        {mobileOpen ? (
          <div className="fixed inset-0 z-40 md:hidden">
            <button
              type="button"
              className="absolute inset-0 bg-foreground/15"
              aria-label="Close menu"
              onClick={() => setMobileOpen(false)}
            />
            <aside
              ref={menuPanelRef}
              id={navId}
              className="relative flex h-full w-56 max-w-[80vw] flex-col border-r border-sidebar-border bg-sidebar"
            >
              <div className="flex h-11 items-center justify-between px-3">
                <BrandMark />
                <Button
                  variant="ghost"
                  size="icon-xs"
                  aria-label="Close menu"
                  onClick={() => setMobileOpen(false)}
                >
                  <XIcon />
                </Button>
              </div>
              <nav aria-label="Primary" className="flex flex-col gap-0.5 px-2">
                {NAV.map((item) => (
                  <NavItem key={item.to} {...item} />
                ))}
              </nav>
            </aside>
          </div>
        ) : null}

        <div className="flex min-w-0 flex-1 flex-col">
          <header className="flex h-11 items-center gap-2 border-b border-border px-3 md:hidden">
            <Button
              ref={menuTriggerRef}
              variant="ghost"
              size="icon-xs"
              aria-label="Open menu"
              aria-expanded={mobileOpen}
              aria-controls={navId}
              onClick={() => setMobileOpen(true)}
            >
              <MenuIcon />
            </Button>
            <BrandMark />
            <div className="ml-auto">
              <ShortcutsTrigger onOpen={() => setHelpOpen(true)} />
            </div>
          </header>

          <main id="main" className="min-h-0 flex-1 overflow-y-auto">
            <Outlet />
          </main>
        </div>

        <KeyboardShortcutsDialog open={helpOpen} onOpenChange={setHelpOpen} />
        <KeyboardHint visible={hintVisible} onDismiss={dismissHint} />
      </div>
    </TooltipProvider>
  );
}

function ShortcutsTrigger({ onOpen }: { onOpen: () => void }) {
  return (
    <Tooltip>
      <TooltipTrigger
        render={
          <Button
            type="button"
            variant="ghost"
            size="icon-xs"
            aria-label="Keyboard shortcuts"
            aria-keyshortcuts="Shift+/"
            onClick={onOpen}
          >
            <CircleHelpIcon />
          </Button>
        }
      />
      <TooltipContent side="bottom" className="rounded-[var(--radius-md)]">
        Shortcuts <Kbd className="border-background/20 bg-background/15 text-background">?</Kbd>
      </TooltipContent>
    </Tooltip>
  );
}

function BrandMark() {
  return (
    <Link
      to="/"
      className="text-sm font-semibold tracking-tight text-foreground outline-none focus-visible:rounded-[var(--radius-sm)] focus-visible:ring-2 focus-visible:ring-ring/40"
    >
      Noscam
    </Link>
  );
}

function NavItem({
  to,
  label,
  icon: Icon,
  end,
}: {
  to: string;
  label: string;
  icon: typeof ListIcon;
  end: boolean;
}) {
  return (
    <NavLink
      to={to}
      end={end}
      className={({ isActive }) =>
        cn(
          "flex h-7 items-center gap-2 rounded-[var(--radius-sm)] px-2 text-sm font-medium transition-colors outline-none focus-visible:ring-2 focus-visible:ring-ring/40",
          isActive
            ? "bg-sidebar-accent text-sidebar-accent-foreground"
            : "text-muted-foreground hover:bg-sidebar-accent/80 hover:text-foreground",
        )
      }
    >
      <Icon className="size-3.5 shrink-0 opacity-70" aria-hidden />
      {label}
    </NavLink>
  );
}
