import { BrowserRouter, Navigate, Route, Routes } from "react-router";

import { AppShell } from "@/src/components/app-shell";
import { DemoProvider } from "@/src/lib/demo-state";
import { ConnectPage } from "@/src/pages/connect-page";
import { LandingPage } from "@/src/pages/landing-page";
import { TransactionsPage } from "@/src/pages/transactions-page";

export function App() {
  return (
    <DemoProvider>
      <BrowserRouter>
        <Routes>
          <Route index element={<LandingPage />} />
          <Route path="dashboard" element={<AppShell />}>
            <Route index element={<TransactionsPage />} />
            <Route path="connect" element={<ConnectPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </DemoProvider>
  );
}
