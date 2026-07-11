const TYPING_TAGS = new Set(["INPUT", "TEXTAREA", "SELECT"]);

export function isTypingTarget(target: EventTarget | null) {
  if (!(target instanceof HTMLElement)) return false;
  if (target.isContentEditable) return true;
  if (TYPING_TAGS.has(target.tagName)) return true;
  return target.closest("[contenteditable='true']") != null;
}

export function isModKey(event: { metaKey: boolean; ctrlKey: boolean; altKey: boolean }) {
  return event.metaKey || event.ctrlKey || event.altKey;
}

export function isShortcutBlocked(target: EventTarget | null) {
  if (isTypingTarget(target)) return true;
  if (typeof document !== "undefined" && document.querySelector("dialog[open]")) {
    return true;
  }
  return false;
}

const KEYBOARD_HINT_KEY = "noscam.keyboard-hint-seen";

export function hasSeenKeyboardHint() {
  try {
    return sessionStorage.getItem(KEYBOARD_HINT_KEY) === "1";
  } catch {
    return true;
  }
}

export function markKeyboardHintSeen() {
  try {
    sessionStorage.setItem(KEYBOARD_HINT_KEY, "1");
  } catch {
    /* ignore */
  }
}

export const SHORTCUT_GROUPS = [
  {
    title: "Anywhere",
    shortcuts: [
      { keys: ["?"], join: "or" as const, label: "Open keyboard shortcuts" },
      { keys: ["g", "t"], join: "then" as const, label: "Go to Transactions" },
      { keys: ["g", "c"], join: "then" as const, label: "Go to Connect" },
      { keys: ["Esc"], join: "or" as const, label: "Close menu or dialog" },
    ],
  },
  {
    title: "Transactions",
    shortcuts: [
      { keys: ["↑", "↓", "j", "k"], join: "or" as const, label: "Move through the list" },
      { keys: ["Home", "End"], join: "or" as const, label: "First or last transaction" },
      { keys: ["/"], join: "or" as const, label: "Focus risk filters" },
      { keys: ["←", "→"], join: "or" as const, label: "Change filter when focused" },
    ],
  },
] as const;
