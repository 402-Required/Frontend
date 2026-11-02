import {
  Bell,
  House,
  Grid2x2,
  ChartColumn,
  UserRound,
  Plus,
} from 'lucide-react'
import './App.css'

function showDevelopmentNotice() {
  window.alert('개발 중입니다')
}

const menus = [
  { name: '홈', Icon: House },
  { name: '내 구독', Icon: Grid2x2 },
  { name: '분석', Icon: ChartColumn },
  { name: '마이', Icon: UserRound },
]

function App() {
  return (
    <div className="app-shell">
      <div className="phone-screen">
        <div className="status-bar">
          <span className="time">9:41</span>
          <div className="status-icons" aria-hidden="true">
            <span className="signal" />
            <span className="wifi" />
            <span className="battery" />
          </div>
        </div>

        <header className="top-header">
          <h1>LoopCut</h1>
          <button
            className="icon-button"
            type="button"
            aria-label="알림"
            onClick={showDevelopmentNotice}
          >
            <Bell size={20} strokeWidth={2} />
          </button>
        </header>

        <main className="home-main">
          <section className="hero-text">
            <p>이번 달 구독을 한눈에</p>
            <h2>결제 전에 확인해요.</h2>
          </section>

          <section className="summary-card">
            <div className="summary-row">
              <span className="summary-label">이번 달 예상 결제</span>
              <span className="pill">0개 구독</span>
            </div>

            <div className="summary-price">0원</div>

            <div className="summary-row small-row">
              <span>월 예산 미설정</span>
              <span>등록된 구독 0개</span>
            </div>

            <div className="progress-track" aria-hidden="true">
              <div className="progress-fill" style={{ width: '0%' }} />
            </div>

            <p className="summary-note">
              구독을 등록하면 이번 달 예상 결제액을 확인할 수 있어요.
            </p>
          </section>

          <section className="upcoming-section">
            <div className="section-title-row">
              <h3>다가오는 결제</h3>
              <span>7일 이내 0건 · 0원</span>
            </div>

            <div className="subscription-list">
              <div className="empty-state">
                <Grid2x2
                  size={30}
                  strokeWidth={1.4}
                  aria-hidden="true"
                />
                <strong>등록된 구독이 없어요</strong>
                <p>구독을 등록하면 결제 예정 내역이 여기에 표시돼요.</p>
              </div>
            </div>
          </section>

          <div className="add-button-wrap">
            <button
              type="button"
              className="add-button"
              onClick={showDevelopmentNotice}
            >
              <Plus size={15} strokeWidth={2.4} aria-hidden="true" />
              새 구독 등록
            </button>
          </div>
        </main>

        <nav className="bottom-nav" aria-label="하단 메뉴">
          {menus.map(({ name, Icon }) => {
            const isHome = name === '홈'

            return (
              <button
                key={name}
                className={`nav-item${isHome ? ' active' : ''}`}
                type="button"
                aria-current={isHome ? 'page' : undefined}
                onClick={showDevelopmentNotice}
              >
                <div className={`nav-icon${isHome ? ' active-bg' : ''}`}>
                  <Icon
                    size={18}
                    strokeWidth={2}
                    aria-hidden="true"
                  />
                </div>
                <span>{name}</span>
              </button>
            )
          })}
        </nav>

        <div className="home-indicator" aria-hidden="true">
          <div />
        </div>
      </div>
    </div>
  )
}

export default App