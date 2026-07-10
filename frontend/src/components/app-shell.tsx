import { Link, NavLink, Outlet, useLocation } from "react-router";
import { LandmarkIcon, ListIcon, MenuIcon, XIcon } from "lucide-react";
import { useEffect, useId, useState } from "react";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

const NAV = [
  { to: "/dashboard", label: "Transactions", icon: ListIcon, end: true },
  { to: "/dashboard/connect", label: "Connect", icon: LandmarkIcon, end: false },
] as const;

export function AppShell() {
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const navId = useId();

  useEffect(() => {
    setMobileOpen(false);
  }, [location.pathname]);

  return (
    <div className="flex min-h-dvh bg-background text-foreground">
      <a
        href="#main"
        className="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 focus:z-50 focus:rounded-[var(--radius-sm)] focus:bg-primary focus:px-2.5 focus:py-1.5 focus:text-xs focus:text-primary-foreground"
      >
        Skip to content
      </a>

      <aside className="hidden w-56 shrink-0 flex-col border-r border-sidebar-border bg-sidebar md:flex">
        <div className="flex h-11 items-center px-4">
          <BrandMark />
        </div>
        <nav aria-label="Primary" className="flex flex-1 flex-col gap-0.5 px-2 pb-3">
          {NAV.map((item) => (
            <NavItem key={item.to} {...item} />
          ))}
        </nav>
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
        </header>

        <main id="main" className="min-h-0 flex-1 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
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
