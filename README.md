# LoopCut Frontend (Android)

Kotlin + Jetpack Compose로 만든 **네이티브 안드로이드 앱**입니다. WebView나 웹 서버를 사용하지 않습니다. 백엔드 없이 실행되며 구독·결제수단·설정은 기기의 앱 전용 저장소에 저장됩니다.

## 테스트 방법

처음 테스트한다면 **[실행·테스트 가이드](docs/TESTING.md)**를 순서대로 따라 하세요.

1. Android Studio로 프로젝트 루트를 열고 JDK 17 / SDK 35를 준비합니다.
2. API 35 에뮬레이터를 켜고 `app`을 실행합니다.
3. 로그인 화면에서 **데모 둘러보기**를 누릅니다. 또는 이메일 `test@example.com`, 비밀번호 `test1234`로 진입합니다. 실제 계정 인증은 없습니다.
4. 가이드의 **수동 테스트 체크리스트**에 따라 구독 등록·수정·이력, 설정, 파일 내보내기를 확인합니다.
5. 자동 테스트는 아래 명령으로 실행합니다. 기기 테스트는 **초기 샘플 구독 6개** 상태가 필요하므로 별도 테스트 에뮬레이터를 권장합니다.

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

정상 결과는 `BUILD SUCCESSFUL`, 단위 테스트 **7개** / 기기 테스트 **5개** 실패 0건입니다. 보고서 위치, 초기화 방법, macOS/Linux 명령, 문제 해결은 [테스트 가이드](docs/TESTING.md)에 있습니다.

## 실행

1. Android Studio에서 이 프로젝트의 루트 폴더를 엽니다.
2. Gradle JDK를 **17**으로 설정하고 SDK 35를 설치합니다.
3. Gradle Sync 후 Android 8.0(API 26) 이상 기기 또는 에뮬레이터에서 `app`을 실행합니다.
4. 첫 화면의 **데모 둘러보기**를 누르면 샘플 구독 6개를 확인할 수 있습니다.

명령줄:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

검증 결과와 실제 앱 캡처는 [검증 기록](qa/VALIDATION.md)에 있습니다. Java 17 + 한글 Windows 경로에서는 Gradle 데몬의 `file.encoding`을 임의로 UTF-8로 강제하지 마세요.

APK 경로: `app/build/outputs/apk/debug/app-debug.apk`

JDK 17과 Android SDK 35는 각 개발 환경에 설치합니다. Android Studio가 생성하는 `local.properties`는 개인 SDK 경로이므로 Git에서 제외합니다.

## 구현 내용

- Figma의 로그인, 회원가입, 내 구독, 지난 이력, 등록, 서비스 검색, 카테고리 시트, 상세, 마이, 알림 설정, 결제수단, 내보내기, 도움말
- 구독 검색·카테고리 필터·정렬, 등록·수정·삭제 후 이력 보관·다시 구독
- 네이티브 날짜/시간 선택창, 시스템 뒤로 가기, 키보드 및 시스템 바 영역 대응
- 결제수단 추가·수정, 예산·프로필 편집, 알림 설정 저장
- Android 저장 위치 선택창을 통한 **CSV / 실제 XLSX / PDF** 내보내기
- Noto Sans KR 폰트와 Figma SVG를 앱에 포함, 런타임 외부 에셋 다운로드 없음
- 원본에 없는 홈·분석 화면은 같은 디자인 토큰으로 보완

## 데모 범위

- 인증은 화면 검증 및 데모 진입만 제공합니다. 실제 계정 생성·비밀번호 저장·소셜 인증은 하지 않습니다.
- 앱에 인터넷 권한이 없으며 API 요청을 하지 않습니다.
- 알림 설정은 저장되지만 실제 알림은 발송하지 않습니다.
- 상세의 최근 결제 내역은 **예시**입니다. 실제 결제 내역 내보내기는 비활성화했습니다.
- 구독 삭제는 관리 목록에서 이력으로 옮기는 작업입니다. 외부 서비스가 실제 해지되지는 않습니다.
- Figma의 D-day 재현을 위해 샘플 기준일을 **2026-10-03**으로 고정했습니다. 실제 서비스에서는 서버/사용자 시간대로 교체해야 합니다.
- 앱 삭제 또는 데이터 초기화 시 로컬 정보가 삭제됩니다.

부족한 화면·정책·API 작업은 [추가 작업 목록](docs/REMAINING.md), 백엔드 연결 구조는 [연동 안내](docs/BACKEND.md)를 참고하세요.

## 구조

```text
app/src/main/java/kr/ac/tuk/loopcut/
  Models.kt                 데이터 모델, 샘플 데이터, 결제일 계산
  LocalRepository.kt        저장소 인터페이스와 로컬 구현
  LoopCutViewModel.kt       상태 관리와 저장 처리
  LoopCutApp.kt             화면 이동 및 공통 다이얼로그
  UiKit.kt                  테마, 폰트, SVG, 공통 컴포넌트
  AuthScreens.kt            로그인·회원가입
  SubscriptionScreens.kt    목록·등록·검색·상세·이력
  SettingsScreens.kt        마이·알림·결제수단·도움말·홈·분석
  ExportScreen.kt           저장 위치 선택 및 CSV/XLSX/PDF 생성
```

## 저장소 구성 안내

소스, 테스트, Gradle Wrapper, 앱 에셋과 폰트 라이선스, 개발 문서만 포함합니다. 빌드 결과물(APK), 캐시, 로그, 개인 SDK/JDK, Figma 원시 추출 자료와 이전 웹 시제품은 제외했습니다.

[화면 미리보기](qa/contact-sheet.png) · [검증 기록](qa/VALIDATION.md)
