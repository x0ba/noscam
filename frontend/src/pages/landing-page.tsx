import { Link } from "react-router";

import { ProductVignette } from "@/src/components/product-vignette";
import { Button } from "@/components/ui/button";

export function LandingPage() {
  return (
    <div className="landing min-h-dvh text-[var(--landing-ink)]">
      <a
        href="#main"
        className="sr-only focus:not-sr-only focus:absolute focus:top-3 focus:left-3 focus:z-50 focus:rounded-[var(--radius-md)] focus:bg-[var(--landing-ink)] focus:px-3 focus:py-2 focus:text-sm focus:text-[var(--landing-bg)]"
      >
        Skip to content
      </a>

      <header className="landing-header sticky top-0 z-20">
        <div className="mx-auto flex h-14 max-w-[1120px] items-center justify-between px-5 sm:px-8">
          <Link
            to="/"
            className="text-sm font-semibold tracking-tight text-[var(--landing-ink)] outline-none focus-visible:rounded-[var(--radius-sm)] focus-visible:ring-2 focus-visible:ring-[var(--landing-ink)]/30"
          >
            Noscam
          </Link>
          <Button
            size="sm"
            className="h-8 bg-[var(--landing-ink)] px-3 text-[var(--landing-bg)] hover:bg-[var(--landing-ink)]/85"
            render={<Link to="/dashboard" />}
          >
            Dashboard
          </Button>
        </div>
      </header>

      <main id="main">
        <section
          aria-labelledby="landing-hero-heading"
          className="landing-hero relative overflow-hidden px-5 pt-16 pb-0 sm:px-8 sm:pt-24"
        >
          <div className="landing-hero-glow" aria-hidden />

          <div className="relative z-[1] mx-auto max-w-[720px] text-center">
            <h1
              id="landing-hero-heading"
              className="landing-display text-balance font-semibold tracking-[-0.035em] text-[var(--landing-ink)]"
            >
              Is this a scam?
            </h1>
            <p className="mx-auto mt-5 max-w-[42ch] text-pretty text-base leading-relaxed text-[var(--landing-muted)] sm:text-lg">
              Check a charge before you reply, click a link, or send money.
            </p>
            <div className="mt-8 flex justify-center">
              <Button
                size="lg"
                className="h-11 bg-[var(--landing-ink)] px-5 text-sm text-[var(--landing-bg)] hover:bg-[var(--landing-ink)]/85"
                render={<Link to="/dashboard" />}
              >
                Open dashboard
              </Button>
            </div>
          </div>

          <div className="relative z-[1] mx-auto mt-14 max-w-[960px] sm:mt-20">
            <div className="landing-stage px-2 sm:px-4">
              <ProductVignette />
            </div>
          </div>
        </section>

        <section
          aria-labelledby="landing-points-heading"
          className="border-t border-[var(--landing-line)] px-5 py-20 sm:px-8 sm:py-28"
        >
          <h2 id="landing-points-heading" className="sr-only">
            What Noscam does
          </h2>
          <ul className="mx-auto grid max-w-[1120px] gap-10 sm:grid-cols-3 sm:gap-8">
            <Point
              title="Import charges"
              body="Connect a bank with Plaid, or upload a CSV from your bank or card."
            />
            <Point
              title="See the risk"
              body="Each transaction gets a score and a short reason you can actually read."
            />
            <Point
              title="Decide next"
              body="If something looks wrong, pause before you reply or send money."
            />
          </ul>
        </section>

        <section
          aria-labelledby="landing-how-heading"
          className="border-t border-[var(--landing-line)] px-5 py-20 sm:px-8 sm:py-28"
        >
          <div className="mx-auto max-w-[640px]">
            <h2
              id="landing-how-heading"
              className="landing-section-title text-balance text-center font-semibold tracking-[-0.03em] text-[var(--landing-ink)]"
            >
              How it works
            </h2>
            <ol className="mt-12 divide-y divide-[var(--landing-line)] border-y border-[var(--landing-line)]">
              <HowStep
                step="1"
                title="Connect an account"
                body="Link a bank with Plaid, or upload a CSV."
              />
              <HowStep
                step="2"
                title="Review the scores"
                body="Every charge gets a score from low to high, plus why."
              />
              <HowStep
                step="3"
                title="Decide what to do"
                body="If something looks off, check another way before you act."
              />
            </ol>
            <div className="mt-12 flex justify-center">
              <Button
                size="lg"
                className="h-11 bg-[var(--landing-ink)] px-5 text-sm text-[var(--landing-bg)] hover:bg-[var(--landing-ink)]/85"
                render={<Link to="/dashboard" />}
              >
                Open dashboard
              </Button>
            </div>
          </div>
        </section>
      </main>

      <footer className="border-t border-[var(--landing-line)] px-5 py-6 sm:px-8">
        <div className="mx-auto flex max-w-[1120px] flex-wrap items-center justify-between gap-4">
          <p className="text-sm font-semibold tracking-tight text-[var(--landing-ink)]">Noscam</p>
          <nav aria-label="Footer">
            <Link
              to="/dashboard"
              className="text-sm font-medium text-[var(--landing-muted)] underline-offset-4 outline-none transition-colors hover:text-[var(--landing-ink)] hover:underline focus-visible:rounded-[var(--radius-sm)] focus-visible:ring-2 focus-visible:ring-[var(--landing-ink)]/30"
            >
              Dashboard
            </Link>
          </nav>
        </div>
      </footer>
    </div>
  );
}

function Point({ title, body }: { title: string; body: string }) {
  return (
    <li className="text-center sm:text-left">
      <h3 className="text-base font-semibold tracking-tight text-[var(--landing-ink)]">{title}</h3>
      <p className="mt-2 text-pretty text-sm leading-relaxed text-[var(--landing-muted)] sm:text-base">
        {body}
      </p>
    </li>
  );
}

function HowStep({ step, title, body }: { step: string; title: string; body: string }) {
  return (
    <li className="grid grid-cols-[2rem_1fr] gap-4 py-6 sm:grid-cols-[2.5rem_minmax(10rem,12rem)_1fr] sm:gap-8">
      <span className="pt-0.5 text-sm tabular-nums text-[var(--landing-muted)]" aria-hidden>
        {step}
      </span>
      <h3 className="text-base font-semibold tracking-tight text-[var(--landing-ink)]">{title}</h3>
      <p className="col-span-2 max-w-prose text-pretty text-base leading-relaxed text-[var(--landing-muted)] sm:col-span-1">
        {body}
      </p>
    </li>
  );
}
