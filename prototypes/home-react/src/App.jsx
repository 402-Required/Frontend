import { createContext, useContext, useEffect, useRef, useState } from "react";
import {
  ArrowLeft,
  ArrowRight,
  Bell,
  ChartColumn,
  ChevronRight,
  CircleHelp,
  CreditCard,
  Download,
  Grid2x2,
  History,
  House,
  Plus,
  Repeat2,
  Search,
  ShieldCheck,
  UserRound,
  X,
  Check,
  LogOut,
} from "lucide-react";
import { catalog, categories, logoPaths, matchesService } from "./catalog";
import {
  demoRepository,
  money,
  monthly,
  nextPayment,
  daysUntil,
  dateLabel,
  today,
  occurrences,
  exportCsv,
} from "./data";
import "./App.css";
const tabs = [
  ["home", "홈", House],
  ["subscriptions", "내 구독", Grid2x2],
  ["analytics", "분석", ChartColumn],
  ["my", "마이", UserRound],
];
function Logo({ service }) {
  const [failed, setFailed] = useState(false);
  const item = catalog.find((c) => c.id === service.serviceId) || service;
  return (
    <span
      className={`logo ${item.id === "netflix" ? "dark" : ""}`}
      style={{ background: item.color || "#e0f3ed" }}
    >
      {logoPaths[item.id] && !failed ? (
        <img
          src={logoPaths[item.id]}
          alt=""
          loading="lazy"
          decoding="async"
          onError={() => setFailed(true)}
        />
      ) : (
        item.initial || item.name?.slice(0, 1) || "+"
      )}
    </span>
  );
}
function Empty({
  title = "등록된 구독이 없어요",
  text = "새 구독을 등록하고 결제 전에 확인해요.",
  action,
}) {
  return (
    <div className="empty">
      <Grid2x2 size={30} strokeWidth={1.4} />
      <strong>{title}</strong>
      <p>{text}</p>
      {action}
    </div>
  );
}
function Toggle({ checked, onChange, label }) {
  return (
    <button
      type="button"
      className={`toggle ${checked ? "on" : ""}`}
      role="switch"
      aria-checked={checked}
      aria-label={label}
      onClick={() => onChange(!checked)}
    >
      <span />
    </button>
  );
}
function Field({ label, children }) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
    </label>
  );
}
const AppContext = createContext(null);
function App() {
  const [data, setData] = useState(() => demoRepository.load());
  const [route, setRoute] = useState({ page: "home" });
  const [, setStack] = useState([]);
  const [toast, setToast] = useState("");
  const [storageError, setStorageError] = useState("");
  const timer = useRef();
  const [dialog, setDialog] = useState(null);
  useEffect(() => {
    try {
      demoRepository.save(data);
    } catch {
      queueMicrotask(() =>
        setStorageError(
          "이 브라우저에 저장하지 못했어요. 저장 공간을 확인해 주세요.",
        ),
      );
    }
  }, [data]);
  useEffect(() => () => clearTimeout(timer.current), []);
  const notify = (message) => {
    setToast(message);
    clearTimeout(timer.current);
    timer.current = setTimeout(() => setToast(""), 3000);
  };
  const update = (fn) =>
    setData((old) => (typeof fn === "function" ? fn(old) : { ...old, ...fn }));
  const go = (page, extra = {}) => {
    setStack((s) => [...s, route]);
    setRoute({ page, ...extra });
    window.scrollTo(0, 0);
  };
  const back = () => {
    setStack((s) => {
      setRoute(s.at(-1) || { page: "home" });
      return s.slice(0, -1);
    });
    window.scrollTo(0, 0);
  };
  const tab = (page) => {
    setStack([]);
    setRoute({ page });
    window.scrollTo(0, 0);
  };
  const active = data.subscriptions;
  const total = active.reduce((n, s) => n + monthly(s), 0);
  const [now] = useState(() => new Date());
  const billed = active.reduce(
    (n, s) =>
      n + occurrences(s, now.getFullYear(), now.getMonth()).length * s.price,
    0,
  );
  const upcoming = active
    .filter((s) => daysUntil(nextPayment(s)) <= 7)
    .sort((a, b) => nextPayment(a) - nextPayment(b));
  const root = tabs.some((t) => t[0] === route.page);
  const titles = {
    home: "LoopCut",
    subscriptions: "내 구독",
    analytics: "분석",
    my: "마이",
    find: "서비스 찾기",
    add: "구독 추가 등록",
    edit: "구독 수정",
    detail: "구독 상세",
    history: "지난 구독 이력",
    notifications: "결제 알림",
    methods: "결제수단",
    profile: "프로필 수정",
    budget: "월 예산",
    help: "도움말",
    login: "로그인",
    signup: "회원가입",
  };
  const subscription = active.find((s) => s.id === route.id);
  return (
    <AppContext.Provider
      value={{
        data,
        route,
        update,
        go,
        back,
        tab,
        notify,
        active,
        total,
        now,
        billed,
        subscription,
        setDialog,
      }}
    >
      <div className="app-shell">
        <div className="phone-screen">
          <header className="top-header">
            {!root && (
              <button aria-label="뒤로" className="icon-button" onClick={back}>
                <ArrowLeft size={22} />
              </button>
            )}
            <h1>{titles[route.page]}</h1>
            <div className="header-actions">
              {route.page === "home" && (
                <button
                  className="icon-button"
                  aria-label="결제 알림 설정"
                  onClick={() => go("notifications")}
                >
                  <Bell size={21} />
                </button>
              )}
              {route.page === "subscriptions" && (
                <>
                  <button
                    className="history-link"
                    onClick={() => go("history")}
                  >
                    <History size={14} />
                    지난 이력
                  </button>
                  <button
                    className="round"
                    aria-label="구독 추가"
                    onClick={() => go("find")}
                  >
                    <Plus size={23} />
                  </button>
                </>
              )}
              {route.page === "my" && (
                <button
                  className="icon-button"
                  aria-label="도움말"
                  onClick={() => go("help")}
                >
                  <CircleHelp size={21} />
                </button>
              )}
            </div>
          </header>
          <main key={route.page}>
            {storageError && (
              <p className="error" role="alert">
                {storageError}
              </p>
            )}
            {route.page === "home" && (
              <>
                <div className="hero-text">
                  <p>{data.profile.name}님, 이번 달 구독은</p>
                  <h2>결제 전에 확인해요.</h2>
                </div>
                <Summary />
                <div className="between section-heading">
                  <h2>다가오는 결제</h2>
                  <small className="green">
                    7일 이내 {upcoming.length}건 ·{" "}
                    {money(upcoming.reduce((n, s) => n + s.price, 0))}
                  </small>
                </div>
                <div className="list">
                  {upcoming.map((s) => (
                    <Row key={s.id} s={s} />
                  ))}
                  {!upcoming.length && (
                    <Empty
                      title={
                        active.length ? "7일 이내 결제가 없어요" : undefined
                      }
                      text={
                        active.length
                          ? "다가올 결제 일정이 생기면 여기에 표시돼요."
                          : undefined
                      }
                    />
                  )}
                </div>
                {upcoming.some((s) => daysUntil(nextPayment(s)) <= 1) && (
                  <p className="notice yellow">
                    <Bell size={16} />
                    오늘 또는 내일 결제되는 구독이 있어요.
                  </p>
                )}
                <div className="center spaced">
                  <button className="primary" onClick={() => go("find")}>
                    <Plus size={15} />새 구독 등록
                  </button>
                </div>
              </>
            )}
            {route.page === "subscriptions" && <Subscriptions />}
            {route.page === "find" && <Finder />}
            {["add", "edit"].includes(route.page) && <SubscriptionForm />}
            {route.page === "detail" && <Detail />}
            {route.page === "history" && <HistoryPage />}
            {route.page === "analytics" && <Analytics />}
            {route.page === "my" && (
              <>
                <button
                  className="panel profile setting full"
                  onClick={() => go("profile")}
                >
                  <span className="logo">{data.profile.name.slice(0, 1)}</span>
                  <span>
                    <strong>{data.profile.name}님</strong>
                    <small>구독 관리 {active.length}개 · 프로필 수정</small>
                  </span>
                  <ChevronRight size={16} />
                </button>
                <Summary budgetOnly />
                <div className="panel spaced">
                  {[
                    [
                      Bell,
                      "결제 알림",
                      "notifications",
                      data.notifications.enabled ? "켜짐" : "꺼짐",
                    ],
                    [
                      CreditCard,
                      "결제수단",
                      "methods",
                      `${data.methods.length}개`,
                    ],
                    [Download, "데이터 내보내기", "csv", "CSV"],
                    [CircleHelp, "도움말", "help", ""],
                  ].map(([Icon, label, page, value]) => (
                    <button
                      key={page}
                      className="menu-row"
                      onClick={() =>
                        page === "csv"
                          ? (exportCsv(data), notify("CSV 파일을 내보냈어요"))
                          : go(page)
                      }
                    >
                      <Icon size={21} />
                      <span>{label}</span>
                      <small>{value}</small>
                      <ChevronRight size={17} />
                    </button>
                  ))}
                </div>
                <div className="privacy">
                  <ShieldCheck size={17} />
                  <strong>내 구독 정보는 안전하게</strong>
                  <p>필요한 정보만 입력하고 직접 관리할 수 있어요.</p>
                  <small>루프컷 v1.0 · 프론트엔드 데모</small>
                </div>
                <button
                  className="muted full spaced"
                  onClick={() =>
                    data.session
                      ? (update({ session: false }), notify("로그아웃했어요"))
                      : go("login")
                  }
                >
                  <LogOut size={16} />
                  {data.session ? "로그아웃" : "로그인 · 회원가입 체험"}
                </button>
              </>
            )}
            {route.page === "notifications" && <Notifications />}
            {route.page === "methods" && <Methods />}
            {route.page === "profile" && <Profile />}
            {route.page === "budget" && <Budget />}
            {route.page === "help" && <Help />}
            {["login", "signup"].includes(route.page) && <Auth />}
          </main>
          {root && (
            <nav className="bottom-nav" aria-label="하단 메뉴">
              {tabs.map(([key, label, Icon]) => (
                <button
                  key={key}
                  aria-current={route.page === key ? "page" : undefined}
                  className={route.page === key ? "active" : ""}
                  onClick={() => tab(key)}
                >
                  <span>
                    <Icon size={20} strokeWidth={1.8} />
                  </span>
                  {label}
                </button>
              ))}
            </nav>
          )}
          {toast && (
            <div className="toast" role="status">
              {toast}
            </div>
          )}
          {dialog && (
            <div className="overlay">
              <section
                role="dialog"
                aria-modal="true"
                aria-labelledby="dialog-title"
                className="dialog"
              >
                <h2 id="dialog-title">{dialog.title}</h2>
                <p>{dialog.text}</p>
                <div className="columns">
                  <button
                    autoFocus
                    className="secondary"
                    onClick={() => setDialog(null)}
                  >
                    {dialog.action ? "취소" : "닫기"}
                  </button>
                  {dialog.action && (
                    <button
                      className="primary"
                      onClick={() => {
                        dialog.action();
                        setDialog(null);
                      }}
                    >
                      확인
                    </button>
                  )}
                </div>
              </section>
            </div>
          )}
        </div>
      </div>
    </AppContext.Provider>
  );
}
function Row({ s }) {
  const { go } = useContext(AppContext);
  const date = nextPayment(s);
  const days = daysUntil(date);
  return (
    <button className="subscription" onClick={() => go("detail", { id: s.id })}>
      <Logo service={s} />
      <div className="service-info">
        <strong>{s.name}</strong>
        <div>
          <small>{s.category}</small>
          <span className={`badge ${s.unused ? "danger" : ""}`}>
            {s.unused ? "미사용 확인 필요" : "이용 중"}
          </span>
        </div>
      </div>
      <div className="service-price">
        <strong>
          {money(s.price)}
          <small>/{s.cycle === "year" ? "년" : "월"}</small>
        </strong>
        <small className={days <= 3 ? "warning" : ""}>
          {dateLabel(date)} ·{" "}
          {days === 0
            ? "오늘"
            : days === Infinity
              ? "예정 없음"
              : days <= 3
                ? `D-${days}`
                : `${days}일 뒤`}
        </small>
      </div>
    </button>
  );
}
function Summary({ budgetOnly = false }) {
  const { data, go, active, total, now, billed } = useContext(AppContext);
  const ratio = data.budget ? total / data.budget : 0;
  return (
    <section className="summary">
      <div className="between">
        <span>
          {budgetOnly
            ? `월 예산 · ${ratio > 1 ? "초과" : ratio >= 0.8 ? "주의" : "여유"}`
            : `${now.getMonth() + 1}월 예상 결제`}
        </span>
        {budgetOnly ? (
          <button onClick={() => go("budget")}>수정</button>
        ) : (
          <span className="badge">{active.length}개 구독</span>
        )}
      </div>
      <strong className="amount">
        {money(budgetOnly ? data.budget : billed)}
      </strong>
      <div className="between fine">
        <span>
          {budgetOnly
            ? "월 환산 비용 기준"
            : data.budget
              ? `월 예산 ${money(data.budget)}`
              : "월 예산 미설정"}
        </span>
        <span>{data.budget ? `${Math.round(ratio * 100)}% 사용` : ""}</span>
      </div>
      <div className="track">
        <div style={{ width: `${Math.min(ratio * 100, 100)}%` }} />
      </div>
      <p>
        {data.budget ? (
          total > data.budget ? (
            `${money(total - data.budget)} 초과했어요`
          ) : (
            `${money(data.budget - total)} 더 쓸 수 있어요`
          )
        ) : (
          <button onClick={() => go("budget")}>
            월 예산을 설정해 보세요 <ChevronRight size={12} />
          </button>
        )}
      </p>
    </section>
  );
}
function Subscriptions() {
  const { go, active } = useContext(AppContext);
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("전체");
  const [sort, setSort] = useState("date");
  const list = active
    .filter(
      (s) =>
        matchesService(s, query) &&
        (category === "전체" || s.category === category),
    )
    .sort((a, b) =>
      sort === "price"
        ? monthly(b) - monthly(a)
        : sort === "name"
          ? a.name.localeCompare(b.name, "ko")
          : (nextPayment(a)?.getTime() || Infinity) -
            (nextPayment(b)?.getTime() || Infinity),
    );
  return (
    <>
      <div className="between section-heading">
        <h2>전체 {active.length}개</h2>
        <select
          aria-label="구독 정렬"
          value={sort}
          onChange={(e) => setSort(e.target.value)}
        >
          <option value="date">결제일순</option>
          <option value="price">높은 금액순</option>
          <option value="name">이름순</option>
        </select>
      </div>
      <label className="search">
        <Search size={18} />
        <input
          placeholder="서비스 이름으로 검색"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        {query && (
          <button aria-label="검색 지우기" onClick={() => setQuery("")}>
            <X size={16} />
          </button>
        )}
      </label>
      <div className="chips" aria-label="카테고리">
        {["전체", ...categories].map((c) => (
          <button
            key={c}
            className={category === c ? "selected" : ""}
            onClick={() => setCategory(c)}
          >
            {c}
          </button>
        ))}
      </div>
      <div className="list">
        {list.map((s) => (
          <Row key={s.id} s={s} />
        ))}
        {!list.length && (
          <Empty
            title={active.length ? "검색 결과가 없어요" : undefined}
            text={
              active.length
                ? "다른 이름이나 카테고리를 선택해 주세요."
                : undefined
            }
          />
        )}
      </div>
      <button className="primary full spaced" onClick={() => go("find")}>
        <Plus size={16} />새 구독 등록
      </button>
    </>
  );
}
function Finder() {
  const { data, update, go } = useContext(AppContext);
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("전체");
  const list = catalog.filter(
    (s) =>
      matchesService(s, query) &&
      (category === "전체" || s.category === category),
  );
  const select = (s) => {
    update((d) => ({
      ...d,
      recent: [s.id, ...d.recent.filter((id) => id !== s.id)].slice(0, 5),
    }));
    go("add", { service: s });
  };
  return (
    <>
      <label className="search outlined">
        <Search size={18} />
        <input
          autoFocus
          placeholder="서비스 이름을 검색하세요"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        {query && (
          <button aria-label="검색 지우기" onClick={() => setQuery("")}>
            <X size={16} />
          </button>
        )}
      </label>
      {!!data.recent.length && !query && (
        <>
          <div className="between section-heading">
            <h2>최근 검색</h2>
            <button className="muted" onClick={() => update({ recent: [] })}>
              전체 삭제
            </button>
          </div>
          <div className="chips wrap">
            {data.recent
              .map((id) => catalog.find((s) => s.id === id))
              .filter(Boolean)
              .map((s) => (
                <button key={s.id} onClick={() => select(s)}>
                  {s.name}
                  <ArrowRight size={12} />
                </button>
              ))}
          </div>
        </>
      )}
      <h2 className="spaced">카테고리로 찾기</h2>
      <div className="chips wrap">
        {["전체", ...categories].map((c) => (
          <button
            className={category === c ? "selected" : ""}
            key={c}
            onClick={() => setCategory(c)}
          >
            {c}
          </button>
        ))}
      </div>
      <h2 className="spaced">
        {query
          ? `검색 결과 ${list.length}개`
          : category === "전체"
            ? `전체 서비스 ${catalog.length}개`
            : category}
      </h2>
      <div className="panel catalog">
        {list.map((s) => (
          <button key={s.id} className="catalog-row" onClick={() => select(s)}>
            <Logo service={s} />
            <span>
              <strong>{s.name}</strong>
              <small>{s.category} · 요금 직접 수정 가능</small>
            </span>
            <span className="round pale">
              <Plus size={17} />
            </span>
          </button>
        ))}
        {!list.length && <p className="muted">일치하는 서비스가 없어요.</p>}
      </div>
      <button
        className="custom-service"
        onClick={() =>
          go("add", {
            service: {
              name: query,
              category: category === "전체" ? "기타" : category,
            },
          })
        }
      >
        <Plus size={20} />
        <span>
          {query
            ? `“${query}” 직접 등록하기`
            : "찾는 서비스가 없다면 직접 입력하기"}
        </span>
        <ChevronRight size={16} />
      </button>
      <p className="caption">
        서비스를 선택한 뒤 이용 중인 요금제의 실제 결제 금액을 입력하세요.
      </p>
    </>
  );
}
function SubscriptionForm() {
  const { data, route, update, go, tab, notify, subscription } =
    useContext(AppContext);
  const editing = route.page === "edit";
  const original = editing ? subscription : route.restore || null;
  const item = original || route.service || {};
  const [form, setForm] = useState({
    name: item.name || "",
    serviceId: item.serviceId || item.id || "",
    price: item.price ?? "",
    cycle: item.cycle || "month",
    startDate: original && !editing ? today() : item.startDate || today(),
    category: item.category || "기타",
    methodId: item.methodId || "",
    remind: item.remind ?? true,
    unused: item.unused || false,
  });
  const [sheet, setSheet] = useState(false);
  const change = (key, value) => setForm((f) => ({ ...f, [key]: value }));
  const save = (e) => {
    e.preventDefault();
    if (
      !form.name.trim() ||
      !Number.isFinite(Number(form.price)) ||
      Number(form.price) < 1 ||
      !form.startDate
    )
      return;
    const entry = {
      ...form,
      name: form.name.trim(),
      price: Number(form.price),
      id: editing ? subscription.id : crypto.randomUUID(),
      createdAt: editing ? subscription.createdAt : today(),
    };
    update((d) => ({
      ...d,
      subscriptions: editing
        ? d.subscriptions.map((s) => (s.id === entry.id ? entry : s))
        : [...d.subscriptions, entry],
    }));
    tab("subscriptions");
    notify(editing ? "구독을 수정했어요" : "새 구독을 등록했어요");
  };
  return (
    <form onSubmit={save} className="form">
      <div className="panel service-select">
        <Logo service={{ ...form, initial: form.name.slice(0, 1) }} />
        <Field label="서비스 이름">
          <input
            required
            maxLength={60}
            placeholder="어떤 서비스를 등록할까요?"
            value={form.name}
            onChange={(e) => change("name", e.target.value)}
          />
        </Field>
        <button
          type="button"
          aria-label="서비스 검색"
          onClick={() => go("find")}
        >
          <Search size={20} />
        </button>
      </div>
      <Field label="결제 금액">
        <input
          required
          type="number"
          min="1"
          max="100000000"
          step="1"
          placeholder="금액을 입력하세요"
          value={form.price}
          onChange={(e) => change("price", e.target.value)}
        />
      </Field>
      <Field label="결제 주기">
        <div className="segmented">
          {[
            ["month", "매월"],
            ["year", "매년"],
          ].map(([key, label]) => (
            <button
              type="button"
              key={key}
              className={form.cycle === key ? "chosen" : ""}
              onClick={() => change("cycle", key)}
            >
              {form.cycle === key && <Check size={14} />} {label}
            </button>
          ))}
        </div>
      </Field>
      <Field label="첫 결제일">
        <input
          required
          type="date"
          value={form.startDate}
          onChange={(e) => change("startDate", e.target.value)}
        />
      </Field>
      <div className="columns">
        <Field label="카테고리">
          <button
            className="input"
            type="button"
            onClick={() => setSheet(true)}
          >
            <Grid2x2 size={16} />
            {form.category}
            <ChevronRight size={14} />
          </button>
        </Field>
        <Field label="결제수단">
          <select
            value={form.methodId}
            onChange={(e) => change("methodId", e.target.value)}
          >
            <option value="">선택 안 함</option>
            {data.methods.map((m) => (
              <option key={m.id} value={m.id}>
                {m.name} · {m.last4}
              </option>
            ))}
          </select>
        </Field>
      </div>
      <div className="panel setting">
        <Bell size={20} />
        <span>
          <strong>결제 알림</strong>
          <small>전체 알림 설정을 따라요</small>
        </span>
        <Toggle
          checked={form.remind}
          label="구독 결제 알림"
          onChange={(v) => change("remind", v)}
        />
      </div>
      <div className="panel setting">
        <span>
          <strong>미사용 확인 필요</strong>
          <small>다시 이용할지 검토할 구독이에요</small>
        </span>
        <Toggle
          checked={form.unused}
          label="미사용 확인 필요"
          onChange={(v) => change("unused", v)}
        />
      </div>
      <p className="caption">등록한 정보는 언제든 수정할 수 있어요.</p>
      <button className="primary full form-submit">
        {editing ? "수정하기" : "등록하기"}
      </button>
      {sheet && (
        <div className="overlay" onClick={() => setSheet(false)}>
          <section
            role="dialog"
            aria-modal="true"
            aria-label="카테고리 선택"
            className="sheet"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="between">
              <h2>카테고리 선택</h2>
              <button
                type="button"
                aria-label="닫기"
                onClick={() => setSheet(false)}
              >
                <X />
              </button>
            </div>
            <p className="caption">구독 성격에 맞는 분류를 골라주세요</p>
            <div className="category-grid">
              {categories.map((c) => (
                <button
                  type="button"
                  key={c}
                  className={form.category === c ? "chosen" : ""}
                  onClick={() => change("category", c)}
                >
                  {c}
                  {form.category === c && <Check size={16} />}
                </button>
              ))}
            </div>
            <button
              type="button"
              className="primary full"
              onClick={() => setSheet(false)}
            >
              선택 완료
            </button>
          </section>
        </div>
      )}
    </form>
  );
}
function Detail() {
  const { data, update, go, tab, notify, now, subscription, setDialog } =
    useContext(AppContext);
  if (!subscription)
    return (
      <Empty
        title="구독을 찾을 수 없어요"
        action={
          <button onClick={() => tab("subscriptions")}>내 구독으로</button>
        }
      />
    );
  const s = subscription;
  const date = nextPayment(s);
  const method = data.methods.find((m) => m.id === s.methodId);
  const payments = [];
  for (let i = 0; i < 24; i++) {
    const m = new Date(now.getFullYear(), now.getMonth() - i, 1);
    payments.push(
      ...occurrences(s, m.getFullYear(), m.getMonth()).filter((d) => d <= now),
    );
  }
  return (
    <>
      <div className="detail-hero">
        <Logo service={s} />
        <h2>{s.name}</h2>
        <small>
          {s.category} · <span className="green">이용 중</span>
        </small>
      </div>
      <section className="summary">
        <div className="between">
          <span>다음 결제 {dateLabel(date)}</span>
          <span className="badge yellow">
            {daysUntil(date) === 0 ? "오늘" : `D-${daysUntil(date)}`}
          </span>
        </div>
        <strong className="amount">{money(s.price)}</strong>
      </section>
      <dl className="panel details">
        <div>
          <dt>결제 주기</dt>
          <dd>
            {s.cycle === "year" ? "매년" : "매월"}{" "}
            {s.cycle === "year" ? `${Number(s.startDate.slice(5, 7))}월 ` : ""}
            {Number(s.startDate.slice(8))}일
          </dd>
        </div>
        <div>
          <dt>결제수단</dt>
          <dd>{method ? `${method.name} · ${method.last4}` : "등록 안 함"}</dd>
        </div>
        <div>
          <dt>결제 알림</dt>
          <dd>
            <button onClick={() => go("notifications")}>
              {s.remind && data.notifications.enabled ? "켜짐" : "꺼짐"}{" "}
              <ChevronRight size={12} />
            </button>
          </dd>
        </div>
        <div>
          <dt>등록일</dt>
          <dd>{s.createdAt}</dd>
        </div>
      </dl>
      <div className="between section-heading">
        <h2>최근 결제 내역</h2>
        <small className="green">{payments.length}건</small>
      </div>
      <div className="panel">
        {payments.slice(0, 6).map((d) => (
          <div key={d.toISOString()} className="payment-row">
            <span>{d.toLocaleDateString("ko-KR")}</span>
            <strong>{money(s.price)}</strong>
          </div>
        ))}
        {!payments.length && (
          <p className="caption">아직 지난 결제일이 없어요.</p>
        )}
      </div>
      <p className="caption">
        등록한 일정으로 계산한 데모 내역이며 실제 결제 기록과 다를 수 있어요.
      </p>
      <button
        className="danger-text full spaced"
        onClick={() =>
          setDialog({
            title: "구독 관리를 종료할까요?",
            text: "지난 구독 이력에 보관되며 다시 등록할 수 있어요. 실제 서비스 해지는 해당 서비스에서 진행해 주세요.",
            action: () => {
              update((d) => ({
                ...d,
                subscriptions: d.subscriptions.filter((x) => x.id !== s.id),
                history: [
                  { ...s, endedAt: today(), historyId: crypto.randomUUID() },
                  ...d.history,
                ],
              }));
              tab("subscriptions");
              notify("지난 구독 이력에 보관했어요");
            },
          })
        }
      >
        관리 목록에서 삭제
      </button>
      <button
        className="primary full spaced"
        onClick={() => go("edit", { id: s.id })}
      >
        구독 수정
      </button>
    </>
  );
}
function HistoryPage() {
  const { data, go } = useContext(AppContext);
  return (
    <>
      <section className="summary">
        <div className="between">
          <span>지금까지 정리한 구독</span>
          <span>해지로 아끼는 금액</span>
        </div>
        <div className="between">
          <strong className="amount">{data.history.length}개</strong>
          <strong>
            월 {money(data.history.reduce((n, s) => n + monthly(s), 0))}
          </strong>
        </div>
      </section>
      <h2 className="spaced">전체 {data.history.length}개</h2>
      <div className="list">
        {data.history.map((s) => (
          <article className="panel" key={s.historyId}>
            <div className="setting">
              <Logo service={s} />
              <span>
                <strong>
                  {s.name} <span className="badge danger">해지</span>
                </strong>
                <small>
                  {s.startDate} ~ {s.endedAt}
                </small>
              </span>
            </div>
            <div className="between divider">
              <small>
                {money(s.price)}/{s.cycle === "year" ? "년" : "월"}
              </small>
              <button
                className="green"
                onClick={() => go("add", { restore: s })}
              >
                다시 구독 <Repeat2 size={13} />
              </button>
            </div>
          </article>
        ))}
        {!data.history.length && (
          <Empty
            title="지난 구독 이력이 없어요"
            text="관리 목록에서 삭제한 구독이 여기에 보관돼요."
          />
        )}
      </div>
    </>
  );
}
function Analytics() {
  const { data, active } = useContext(AppContext);
  const [month, setMonth] = useState(today().slice(0, 7));
  const [year, mi] = month.split("-").map(Number);
  const all = [...active, ...data.history];
  const cost = (y, m) =>
    all.reduce((n, s) => n + occurrences(s, y, m).length * s.price, 0);
  const bars = Array.from({ length: 4 }, (_, i) => {
    const d = new Date(year, mi - 4 + i, 1);
    return {
      label: `${d.getMonth() + 1}월`,
      value: cost(d.getFullYear(), d.getMonth()),
    };
  });
  const sum = cost(year, mi - 1);
  const byCategory = categories
    .map((c, i) => ({
      name: c,
      value: all
        .filter((s) => s.category === c)
        .reduce((n, s) => n + occurrences(s, year, mi - 1).length * s.price, 0),
      color: [
        "#00796b",
        "#7b61ca",
        "#54a5c4",
        "#a2dbce",
        "#e9b958",
        "#df8e9d",
        "#a3b270",
        "#c8976c",
        "#8cabc0",
        "#a5a5a5",
      ][i],
    }))
    .filter((c) => c.value > 0);
  const gradient = byCategory
    .map((c, index) => {
      const start =
        (byCategory.slice(0, index).reduce((n, item) => n + item.value, 0) /
          sum) *
        100;
      return `${c.color} ${start}% ${start + (c.value / sum) * 100}%`;
    })
    .join(",");
  return (
    <>
      <Field label="분석 월">
        <input
          type="month"
          required
          value={month}
          onChange={(e) => e.target.value && setMonth(e.target.value)}
        />
      </Field>
      <p className="muted spaced">이번 달 구독 지출</p>
      <strong className="big-total">{money(sum)}</strong>
      <section className="panel spaced">
        <h2>최근 4개월 지출</h2>
        <div
          className="bar-chart"
          role="img"
          aria-label={bars
            .map((b) => `${b.label} ${money(b.value)}`)
            .join(", ")}
        >
          {bars.map((b, i) => (
            <div className="bar-column" key={i}>
              <small>{money(b.value)}</small>
              <div
                className={`bar ${i === 3 ? "current" : ""}`}
                style={{
                  height: `${Math.max(3, (b.value / Math.max(...bars.map((x) => x.value), 1)) * 105)}px`,
                }}
              />
              <small>{b.label}</small>
            </div>
          ))}
        </div>
      </section>
      <section className="panel spaced">
        <h2>어디에 가장 많이 쓸까요?</h2>
        {sum ? (
          <div className="category-chart">
            <div
              className="donut"
              style={{ background: `conic-gradient(${gradient})` }}
              role="img"
              aria-label="카테고리별 지출 비율"
            >
              <div>
                <small>카테고리</small>
                <strong>{byCategory.length}개</strong>
              </div>
            </div>
            <div className="legend">
              {byCategory.map((c) => (
                <div key={c.name}>
                  <i style={{ background: c.color }} />
                  <span>
                    {c.name}
                    <small>{Math.round((c.value / sum) * 100)}%</small>
                  </span>
                  <strong>{money(c.value)}</strong>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <Empty
            title="아직 분석할 구독이 없어요"
            text="구독을 등록하면 지출 그래프가 채워져요."
          />
        )}
      </section>
      <p className="notice">
        월별 그래프는 등록된 결제 일정 기준이에요. 연간 구독은 결제 월에 전체
        금액을 반영해요.
      </p>
    </>
  );
}
function Notifications() {
  const { data, update, back, notify } = useContext(AppContext);
  const [form, setForm] = useState(data.notifications);
  const set = (key, value) => setForm((f) => ({ ...f, [key]: value }));
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        update({ notifications: form });
        notify("결제 알림 설정을 저장했어요");
        back();
      }}
    >
      <div className="panel setting">
        <span>
          <strong>결제 알림 받기</strong>
          <small>등록한 구독의 결제일을 미리 알려드려요</small>
        </span>
        <Toggle
          checked={form.enabled}
          onChange={(v) => set("enabled", v)}
          label="결제 알림 받기"
        />
      </div>
      <h2 className="spaced">알림 시점</h2>
      <div className="panel">
        {[7, 3, 1, 0].map((day) => (
          <div className="setting line" key={day}>
            <span>결제 {day ? `${day}일 전` : "당일"}</span>
            <Toggle
              label={`결제 ${day ? `${day}일 전` : "당일"}`}
              checked={form.days.includes(day)}
              onChange={(v) =>
                set(
                  "days",
                  v ? [...form.days, day] : form.days.filter((d) => d !== day),
                )
              }
            />
          </div>
        ))}
      </div>
      <h2 className="spaced">알림 시간</h2>
      <Field label="알림 받을 시간">
        <input
          type="time"
          required
          value={form.time}
          onChange={(e) => set("time", e.target.value)}
        />
      </Field>
      <h2 className="spaced">알림 방식</h2>
      <div className="panel">
        {[
          ["push", "앱 푸시"],
          ["email", "이메일"],
        ].map(([key, label]) => (
          <div className="setting line" key={key}>
            <span>{label}</span>
            <Toggle
              label={label}
              checked={form[key]}
              onChange={(v) => set(key, v)}
            />
          </div>
        ))}
      </div>
      <p className="caption">
        프론트엔드 데모에서는 설정만 저장되며 실제 푸시·이메일은 발송되지
        않아요.
      </p>
      <button className="primary full form-submit">저장하기</button>
    </form>
  );
}
function Methods() {
  const { data, update, notify, active, setDialog } = useContext(AppContext);
  const [form, setForm] = useState(null);
  const change = (key, value) => setForm((f) => ({ ...f, [key]: value }));
  return (
    <>
      <p className="caption">등록한 결제수단 {data.methods.length}개</p>
      <div className="list">
        {data.methods.map((m) => (
          <div className="panel" key={m.id}>
            <div className="setting">
              <span className="logo">
                <CreditCard size={23} />
              </span>
              <span>
                <strong>{m.name}</strong>
                <small>
                  {m.kind === "bank" ? "계좌" : "카드"} · •••• {m.last4}{" "}
                  {m.primary && <span className="badge">기본</span>}
                </small>
              </span>
            </div>
            <div className="between divider">
              <small>
                구독 {active.filter((s) => s.methodId === m.id).length}개 · 월{" "}
                {money(
                  active
                    .filter((s) => s.methodId === m.id)
                    .reduce((n, s) => n + monthly(s), 0),
                )}
              </small>
              <button className="green" onClick={() => setForm(m)}>
                수정
              </button>
            </div>
          </div>
        ))}
        {!data.methods.length && (
          <Empty
            title="등록된 결제수단이 없어요"
            text="구독별 결제수단을 기록하고 지출을 살펴보세요."
          />
        )}
      </div>
      <p className="caption">
        <ShieldCheck size={14} /> 전체 카드번호 없이 이름과 끝 4자리만 저장해요.
      </p>
      <button
        className="primary full spaced"
        onClick={() =>
          setForm({
            name: "",
            last4: "",
            kind: "card",
            primary: !data.methods.length,
          })
        }
      >
        결제수단 추가
      </button>
      {form && (
        <div className="overlay">
          <form
            className="sheet form"
            role="dialog"
            aria-modal="true"
            aria-label="결제수단 편집"
            onSubmit={(e) => {
              e.preventDefault();
              const m = { ...form, id: form.id || crypto.randomUUID() };
              update((d) => ({
                ...d,
                methods: [
                  ...d.methods
                    .filter((x) => x.id !== m.id)
                    .map((x) => (m.primary ? { ...x, primary: false } : x)),
                  m,
                ],
              }));
              setForm(null);
              notify("결제수단을 저장했어요");
            }}
          >
            <div className="between">
              <h2>결제수단 {form.id ? "수정" : "추가"}</h2>
              <button
                type="button"
                aria-label="닫기"
                onClick={() => setForm(null)}
              >
                <X />
              </button>
            </div>
            <Field label="이름">
              <input
                required
                autoFocus
                maxLength={40}
                value={form.name}
                onChange={(e) => change("name", e.target.value)}
                placeholder="예: 신한카드"
              />
            </Field>
            <Field label="종류">
              <select
                value={form.kind}
                onChange={(e) => change("kind", e.target.value)}
              >
                <option value="card">카드</option>
                <option value="bank">계좌</option>
              </select>
            </Field>
            <Field label="끝 4자리">
              <input
                required
                inputMode="numeric"
                pattern="[0-9]{4}"
                maxLength={4}
                value={form.last4}
                onChange={(e) =>
                  change("last4", e.target.value.replace(/\D/g, ""))
                }
              />
            </Field>
            <label className="check">
              <input
                type="checkbox"
                checked={form.primary}
                onChange={(e) => change("primary", e.target.checked)}
              />
              기본 결제수단
            </label>
            <button className="primary full">저장하기</button>
            {form.id && (
              <button
                type="button"
                className="danger-text"
                onClick={() =>
                  setDialog({
                    title: "결제수단을 삭제할까요?",
                    text: "연결된 구독의 결제수단이 선택 안 함으로 변경돼요.",
                    action: () => {
                      update((d) => ({
                        ...d,
                        methods: d.methods.filter((m) => m.id !== form.id),
                        subscriptions: d.subscriptions.map((s) =>
                          s.methodId === form.id ? { ...s, methodId: "" } : s,
                        ),
                      }));
                      setForm(null);
                    },
                  })
                }
              >
                삭제하기
              </button>
            )}
          </form>
        </div>
      )}
    </>
  );
}
function Profile() {
  const { data, update, back, notify } = useContext(AppContext);
  const [name, setName] = useState(data.profile.name);
  const [email, setEmail] = useState(data.profile.email);
  return (
    <form
      className="form"
      onSubmit={(e) => {
        e.preventDefault();
        update({ profile: { name: name.trim(), email } });
        notify("프로필을 저장했어요");
        back();
      }}
    >
      <Field label="이름">
        <input
          required
          maxLength={30}
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
      </Field>
      <Field label="이메일">
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
      </Field>
      <p className="caption">이 정보는 이 브라우저에만 저장돼요.</p>
      <button className="primary full">저장하기</button>
    </form>
  );
}
function Budget() {
  const { data, update, back, notify, total } = useContext(AppContext);
  const [budget, setBudget] = useState(data.budget || "");
  return (
    <form
      className="form"
      onSubmit={(e) => {
        e.preventDefault();
        update({ budget: Number(budget) });
        back();
        notify("월 예산을 저장했어요");
      }}
    >
      <p className="muted">한 달에 구독에 쓰고 싶은 금액을 정해보세요.</p>
      <Field label="월 예산 (원)">
        <input
          autoFocus
          required
          type="number"
          min="0"
          max="1000000000"
          step="1"
          placeholder="80000"
          value={budget}
          onChange={(e) => setBudget(e.target.value)}
        />
      </Field>
      <div className="chips">
        {[30000, 50000, 80000, 100000].map((n) => (
          <button type="button" key={n} onClick={() => setBudget(n)}>
            {money(n)}
          </button>
        ))}
      </div>
      <p className="notice">
        현재 월 환산 비용은 {money(total)}이에요. 예산을 0원으로 저장하면 설정을
        해제해요.
      </p>
      <button className="primary full">저장하기</button>
    </form>
  );
}
function Auth() {
  const { route, update, go, tab, notify, setDialog } = useContext(AppContext);
  const signup = route.page === "signup";
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [terms, setTerms] = useState(false);
  const [privacy, setPrivacy] = useState(false);
  const [error, setError] = useState("");
  return (
    <form
      className="form auth"
      onSubmit={(e) => {
        e.preventDefault();
        setError("");
        if (
          password.length < 8 ||
          !/[a-zA-Z]/.test(password) ||
          !/[0-9]/.test(password)
        ) {
          setError("비밀번호는 영문과 숫자를 포함해 8자 이상 입력하세요.");
          return;
        }
        if (signup && password !== confirm) {
          setError("비밀번호가 일치하지 않아요.");
          return;
        }
        if (signup && (!terms || !privacy)) {
          setError("필수 약관에 동의해 주세요.");
          return;
        }
        update((d) => ({
          ...d,
          session: true,
          profile: { name: signup ? name : d.profile.name, email },
        }));
        tab("home");
        notify("데모 로그인을 완료했어요");
      }}
    >
      {!signup && (
        <div className="brand">
          <span>
            <Repeat2 size={30} />
          </span>
          <h2>LoopCut</h2>
          <p>결제 전에 확인하는 구독 관리</p>
        </div>
      )}
      {signup && (
        <Field label="이름">
          <input
            required
            maxLength={30}
            value={name}
            onChange={(e) => setName(e.target.value)}
            autoComplete="name"
          />
        </Field>
      )}
      <Field label="이메일">
        <input
          required
          type="email"
          placeholder="이메일 주소를 입력하세요"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
      </Field>
      <Field label="비밀번호">
        <input
          required
          type="password"
          placeholder="영문·숫자 포함 8자 이상"
          autoComplete={signup ? "new-password" : "current-password"}
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
      </Field>
      {signup && (
        <>
          <Field label="비밀번호 확인">
            <input
              required
              type="password"
              autoComplete="new-password"
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
            />
          </Field>
          <div className="panel">
            <label className="check">
              <input
                type="checkbox"
                checked={terms && privacy}
                onChange={(e) => {
                  setTerms(e.target.checked);
                  setPrivacy(e.target.checked);
                }}
              />
              전체 동의하기
            </label>
            {[
              ["[필수] 서비스 이용약관", terms, setTerms],
              ["[필수] 개인정보 수집 및 이용", privacy, setPrivacy],
            ].map(([label, value, set]) => (
              <div className="between line" key={label}>
                <label className="check">
                  <input
                    type="checkbox"
                    checked={value}
                    onChange={(e) => set(e.target.checked)}
                  />
                  {label}
                </label>
                <button
                  type="button"
                  onClick={() =>
                    setDialog({
                      title: label,
                      text: "LoopCut은 브라우저에 데이터를 저장하는 프론트엔드 데모입니다. 실제 계정은 생성되지 않으며 개인정보는 서버로 전송되지 않습니다.",
                    })
                  }
                >
                  보기
                </button>
              </div>
            ))}
          </div>
        </>
      )}
      {error && (
        <p role="alert" className="danger-text">
          {error}
        </p>
      )}
      <button className="primary full">
        {signup ? "가입하기 (데모)" : "로그인 (데모)"}
      </button>
      <button
        type="button"
        className="green full"
        onClick={() => go(signup ? "login" : "signup")}
      >
        {signup ? "이미 계정이 있나요? 로그인" : "아직 계정이 없나요? 회원가입"}
      </button>
      <p className="caption">
        실제 인증이 아닌 체험용 화면이에요. 비밀번호는 저장하지 않아요.
      </p>
    </form>
  );
}
function Help() {
  return (
    <>
      <p className="muted">LoopCut, 이렇게 사용해 보세요.</p>
      {[
        [
          "구독은 어떻게 등록하나요?",
          "내 구독의 + 버튼에서 서비스를 검색하거나 직접 입력하세요. 금액, 주기와 첫 결제일을 입력하면 다음 결제일을 계산해요.",
        ],
        [
          "구독을 삭제하면 실제로 해지되나요?",
          "관리 목록에서만 삭제되며 지난 이력에 보관돼요. 실제 해지는 각 서비스에서 진행하세요.",
        ],
        [
          "월 환산 비용과 예상 결제는 무엇이 다른가요?",
          "연간 구독은 월 환산 시 12로 나눠요. 홈 예상 결제와 분석은 해당 월의 실제 결제 예정 금액을 합산해요.",
        ],
        [
          "결제 알림이 오지 않아요",
          "현재는 프론트엔드 데모로 설정만 저장해요. 실제 알림 발송은 제공하지 않아요.",
        ],
        [
          "데이터는 어디에 저장되나요?",
          "이 기기의 브라우저에 저장돼요. 브라우저 데이터를 지우면 사라지므로 마이에서 CSV로 내보낼 수 있어요.",
        ],
        [
          "서비스 로고와 금액이 실제와 다른가요?",
          "카탈로그 금액은 예시이며 변경할 수 있어요. 로고가 없거나 불러올 수 없으면 서비스 이니셜로 표시해요.",
        ],
      ].map(([q, a]) => (
        <details className="panel faq" key={q}>
          <summary>{q}</summary>
          <p>{a}</p>
        </details>
      ))}
      <p className="notice">LoopCut · 프론트엔드 데모 v1.0</p>
    </>
  );
}
export default App;
