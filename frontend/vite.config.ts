import { defineConfig } from "vite-plus";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
  staged: {
    "*": "vp check --fix",
  },
  fmt: {
    ignorePatterns: ["components/ui/**"],
  },
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": new URL(".", import.meta.url).pathname,
    },
  },
  lint: {
    ignorePatterns: ["components/ui/**"],
    jsPlugins: [{ name: "vite-plus", specifier: "vite-plus/oxlint-plugin" }],
    rules: { "vite-plus/prefer-vite-plus-imports": "error" },
    options: { typeAware: true, typeCheck: true },
  },
});
