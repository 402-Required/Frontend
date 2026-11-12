const KEY = "loopcut.demo.v1";
export const today = () => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
};
export const initialState = () => ({
  version: 1,
  profile: { name: "구독 관리자", email: "" },
  session: false,
  budget: 0,
  subscriptions: [],
  history: [],
  methods: [],
  notifications: {
    enabled: true,
    days: [3, 0],
    time: "09:00",
    push: true,
    email: false,
  },
  recent: [],
});
// Replace this adapter with API calls; components do not access localStorage directly.
export const demoRepository = {
  load() {
    try {
      const raw = JSON.parse(localStorage.getItem(KEY));
      const base = initialState();
      if (!raw || raw.version !== 1) return base;
      return {
        ...base,
        ...raw,
        profile: { ...base.profile, ...raw.profile },
        notifications: { ...base.notifications, ...raw.notifications },
        subscriptions: Array.isArray(raw.subscriptions)
          ? raw.subscriptions
          : [],
        history: Array.isArray(raw.history) ? raw.history : [],
        methods: Array.isArray(raw.methods) ? raw.methods : [],
      };
    } catch {
      return initialState();
    }
  },
  save(state) {
    localStorage.setItem(KEY, JSON.stringify(state));
  },
};
export const money = (n) => `${Math.round(n).toLocaleString("ko-KR")}원`;
export const monthly = (s) => (s.cycle === "year" ? s.price / 12 : s.price);
export function occurrences(s, year, month) {
  const start = new Date(`${s.startDate}T00:00:00`);
  if (!Number.isFinite(start.getTime())) return [];
  if (s.cycle === "year" && start.getMonth() !== month) return [];
  const date = new Date(
    year,
    month,
    Math.min(start.getDate(), new Date(year, month + 1, 0).getDate()),
  );
  const end = s.endedAt ? new Date(`${s.endedAt}T23:59:59`) : null;
  return date >= start && (!end || date <= end) ? [date] : [];
}
export function nextPayment(s, now = new Date()) {
  const floor = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const start = new Date(`${s.startDate}T00:00:00`);
  const anchor = start > floor ? start : floor;
  for (let i = 0; i < 13; i++) {
    const month = new Date(anchor.getFullYear(), anchor.getMonth() + i, 1);
    const date = occurrences(s, month.getFullYear(), month.getMonth())[0];
    if (date && date >= floor) return date;
  }
  return null;
}
export function daysUntil(date) {
  if (!date) return Infinity;
  const now = new Date();
  return Math.round(
    (Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()) -
      Date.UTC(now.getFullYear(), now.getMonth(), now.getDate())) /
      86400000,
  );
}
export const dateLabel = (date) =>
  date
    ? `${date.getMonth() + 1}.${String(date.getDate()).padStart(2, "0")}`
    : "예정 없음";
export function exportCsv(state) {
  const cell = (v) =>
    '"' +
    String(v ?? "")
      .replaceAll('"', '""')
      .replace(/^[=+@-]/, "'$&") +
    '"';
  const rows = [
    ["서비스", "카테고리", "결제 금액", "결제 주기", "첫 결제일", "상태"],
    ...state.subscriptions.map((s) => [
      s.name,
      s.category,
      s.price,
      s.cycle === "year" ? "매년" : "매월",
      s.startDate,
      "이용 중",
    ]),
    ...state.history.map((s) => [
      s.name,
      s.category,
      s.price,
      s.cycle === "year" ? "매년" : "매월",
      s.startDate,
      "해지",
    ]),
  ];
  const url = URL.createObjectURL(
    new Blob(
      ["\uFEFF" + rows.map((row) => row.map(cell).join(",")).join("\r\n")],
      { type: "text/csv;charset=utf-8;" },
    ),
  );
  const a = document.createElement("a");
  a.href = url;
  a.download = `LoopCut-${today()}.csv`;
  a.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
