# 백엔드 연결 안내

`SubscriptionRepository`의 `load()` / `save()`는 suspend 함수이고 ViewModel은 UI와 저장소를 분리합니다. 현재 `LocalRepository`만 주입되어 있습니다.

실서비스에서는 상태 전체 저장을 HTTP 한 번으로 치환하기보다 저장소를 아래 작업별 메서드로 확장하고 ViewModel의 `saveSubscription`, `archive`, `savePayment`, 설정 저장을 연결하세요.

| 작업 | 제안 API | 주요 필드 |
|---|---|---|
| 인증 | POST /auth/login, /auth/signup, /auth/refresh | email, password, token |
| 구독 목록 | GET /subscriptions?status=active | id, name, amount, cycle, firstDate, paymentId, reminder |
| 등록 | POST /subscriptions | 사용자 입력 + 서버 발급 ID |
| 수정 | PATCH /subscriptions/{id} | 변경 필드, version |
| 종료 이력 | POST /subscriptions/{id}/archive | endedAt, reason |
| 지난 이력 | GET /subscriptions?status=archived | cancelled / expired 구분 |
| 결제수단 | GET/POST /payment-methods, PATCH /payment-methods/{id} | 표시용 이름·마스킹, 필요시 PSP token |
| 설정 | GET/PATCH /me/settings | budget, reminderDays, time, timezone, channels |
| 서비스 검색 | GET /services?q= | serviceId, localizedName, categories, plans |
| 결제 내역 | GET /payments | 실제 결제 발생일·금액·통화·상태 |
| 알림 등록 | POST /me/devices | FCM token, platform, timezone |

이 목록은 **제안 계약**이며 실제 서버 구현이 아닙니다.

연결 순서:

1. 사용자/구독/결제수단 DTO 및 서버 ID, 에러 응답 구조를 합의합니다.
2. 인증 상태 및 안전한 토큰 저장소를 별도로 도입합니다. 비밀번호는 기기에 저장하지 않습니다.
3. 원격 저장소와 캐시를 구현하고 생성자 주입으로 데모 저장소와 교체합니다.
4. API 별 로딩·실패·재시도 UI와 서버 검증 오류를 연결합니다. 지금은 네트워크 요청이 없습니다.
5. `DemoData.today`를 주입 가능한 Clock으로 교체합니다. 실제 결제 예정일과 상태는 서버 기준으로 통일하세요.
6. 샘플 결제 내역을 실제 Payment 모델로 바꾸고, 내보내기의 결제 내역 체크를 활성화합니다.

현재 프로젝트에 실제 API 키, OAuth 비밀키, 카드 중간 번호·CVC·유효기간, 외부 분석 SDK는 포함하지 않았습니다.
