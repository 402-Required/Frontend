# LoopCut 테스트 가이드

이 문서는 백엔드 없이 안드로이드 앱을 실행하고 기능을 확인하는 절차입니다. 웹 서버나 npm은 필요하지 않습니다.

## 1. 준비

- Git, Android Studio, JDK 17
- Android SDK Platform 35 및 SDK Platform-Tools
- Android 8.0(API 26) 이상 기기 또는 에뮬레이터. 기존 검증 환경은 API 35입니다.
- 처음 Gradle 의존성을 내려받을 때는 인터넷이 필요합니다. 설치된 앱 자체는 오프라인으로 동작합니다.

```shell
git clone https://github.com/402-Required/Frontend.git
cd Frontend
```

이미 프로젝트가 있다면 그 폴더를 사용하세요. Android Studio에서 **Open**으로 `settings.gradle.kts`가 있는 루트 폴더를 엽니다. `app` 폴더만 열지 마세요.

## 2. Android Studio에서 실행

1. Settings의 **Build, Execution, Deployment > Build Tools > Gradle**에서 Gradle JDK를 **17**로 지정합니다. 메뉴 위치는 Android Studio 버전에 따라 다를 수 있습니다.
2. SDK Manager에서 **Android SDK Platform 35**, **Android SDK Platform-Tools**, **Android Emulator**를 설치합니다.
3. Gradle Sync가 완료될 때까지 기다립니다.
4. Device Manager에서 가상 기기를 만들고 API 35 시스템 이미지를 선택한 뒤 시작합니다. 이미 켜진 에뮬레이터가 있으면 그대로 사용합니다.
5. 실행 구성으로 `app`, 실행 대상으로 해당 에뮬레이터를 선택하고 **Run ▶**을 누릅니다.
6. LoopCut 로그인 화면이 표시되면 **데모 둘러보기**를 누릅니다. 화면 아래에 있으면 스크롤하세요.

실제 휴대폰을 사용하려면 개발자 옵션에서 USB 디버깅을 켜고 USB로 연결한 뒤, 휴대폰에 표시되는 디버깅 허용 요청을 직접 승인합니다. Android Studio의 실행 대상 목록에서 휴대폰을 선택합니다.

### 테스트용 로그인

| 항목 | 입력 예시 |
|---|---|
| 이메일 | `test@example.com` |
| 비밀번호 | `test1234` |

현재는 이메일 **형식**과 비밀번호가 **공백이 아닌지**만 검사합니다. 실제 계정 인증은 없습니다. 위 계정을 미리 가입할 필요도 없습니다. `데모 둘러보기`는 입력 없이 진입합니다.

회원가입 화면은 이름, 이메일 형식, 영문·숫자를 포함한 8자 이상 비밀번호, 비밀번호 확인, 필수 약관 동의를 검사합니다. 실제 계정을 만들거나 비밀번호를 저장하지 않습니다.

## 3. 수동 테스트 체크리스트

새로 설치한 상태에서 순서대로 진행하세요. 이전에 수정한 데이터가 있으면 구독 수와 금액은 달라질 수 있습니다.

| 확인할 기능 | 테스트 방법 | 정상 결과 |
|---|---|---|
| 초기 화면 | 데모 둘러보기로 진입 | 내 구독에 샘플 구독 6개 표시 |
| 로그인 검증 | `abc`를 이메일로 입력하거나 비밀번호를 비우고 로그인 | 오류 안내 표시, 진입하지 않음 |
| 목록 검색 | 내 구독에서 `Canva` 입력 | Canva가 표시되고 Netflix 등 다른 항목은 숨겨짐. 검색어를 지우면 복원 |
| 필터·정렬 | 카테고리 선택 후 정렬 기준 변경 | 해당 카테고리만 표시되고 선택한 기준대로 정렬 |
| 상세 보기 | 목록에서 구독 선택 | 서비스명, 금액, 결제 주기 등 선택한 구독 정보 표시 |
| 등록 입력 검증 | 구독 추가 진입 후 빈 상태 확인 | 등록하기 비활성화 |
| 서비스 검색 | 추가 화면에서 서비스 검색에 `넷플` 입력 후 Netflix 선택 | Netflix가 검색되고 등록 화면에 금액 `17000` 반영 |
| 구독 등록 | 서비스, 양수 금액, 첫 결제일, 카테고리, 결제수단 입력 후 등록 | 목록에 새 항목 추가, 총 개수·금액 반영 |
| 구독 수정 | 상세 > 구독 수정에서 금액을 변경하고 수정하기 | 목록 및 상세에 변경 금액 반영 |
| 이력 보관 | 상세 > 관리 목록에서 삭제 > 삭제하고 이력에 보관 | 활성 목록에서 빠지고 지난 이력에 표시. 실제 서비스 해지 아님 |
| 다시 구독 | 지난 이력에서 다시 구독 선택 후 등록 과정 완료 | 활성 목록에 다시 표시 |
| 프로필·예산 | 마이에서 프로필 또는 예산 수정 | 변경 내용이 화면에 반영 |
| 결제수단 | 마이 > 결제수단에서 추가·수정 | 저장한 결제수단이 목록에 반영 |
| 알림 설정 | 마이 > 결제 알림에서 요일 간격·시간 등을 바꾸고 저장 | 화면을 나갔다 돌아와도 설정 유지. 실제 알림 발송은 하지 않음 |
| 파일 내보내기 | 마이 > 데이터 내보내기에서 CSV, Excel, PDF를 각각 선택하고 파일로 저장하기 | Android 저장 창에서 위치를 지정하면 파일 생성 |
| 내보낸 파일 확인 | 저장한 CSV는 텍스트/스프레드시트 앱, XLSX는 스프레드시트 앱, PDF는 PDF 뷰어로 열기 | 파일이 열리고 선택한 구독 정보 확인 가능. 뷰어가 없으면 PC로 옮겨 확인 |
| 저장 유지 | 변경 사항을 저장한 뒤 앱을 완전히 종료하고 다시 실행하여 데모 진입 | 등록·수정한 구독과 설정 유지. 로그인 화면부터 시작해도 데이터는 유지 |
| 화면 이동 | 홈·내 구독·분석·마이 이동, Android 뒤로 가기, 입력 중 키보드 닫기 | 화면 전환 중 종료되지 않고 주요 버튼이 가려지지 않음 |
| 도움말 | 마이 > 도움말에서 항목 선택 | 해당 안내 내용 확인 가능 |

시각 비교는 [화면 모음](../qa/contact-sheet.png)을 참고하세요. 시스템 상태바 시각 등은 기기에 따라 달라집니다.

## 4. 자동 테스트

프로젝트 루트의 터미널에서 실행합니다. 명령줄에서는 `JAVA_HOME`이 JDK 17을 가리켜야 합니다. Android Studio의 Gradle JDK 설정과 터미널의 Java 설정은 별개일 수 있습니다.

### Windows PowerShell

```powershell
# 빌드: 기기 없이 실행 가능
.\gradlew.bat :app:assembleDebug

# 단위 테스트: 기기 없이 실행 가능
.\gradlew.bat :app:testDebugUnitTest

# 기기 테스트: 에뮬레이터 또는 USB 디버깅 기기가 켜져 있어야 함
.\gradlew.bat :app:connectedDebugAndroidTest
```

### macOS / Linux

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

`Permission denied`가 나오면 `chmod +x gradlew`를 실행합니다.

### 기기 테스트 전 초기 상태 준비

**현재 UI 자동 테스트는 샘플 구독이 6개인 상태를 전제로 합니다.** 수동으로 구독을 추가·삭제한 기기에서는 실패할 수 있습니다. 자동 테스트용 새 에뮬레이터를 사용하거나, 테스트 데이터만 있는 기기에서 LoopCut 앱 데이터를 초기화한 후 실행하세요.

초기화하면 해당 기기의 구독·설정이 삭제됩니다. 보존할 데이터가 있는 기기에서는 실행하지 마세요. 다음 `adb` 명령은 SDK의 `platform-tools`가 PATH에 있는 경우 사용할 수 있습니다.

```shell
adb devices
adb shell pm clear kr.ac.tuk.loopcut
```

여러 기기가 연결되어 있다면 테스트할 기기만 남기거나, 초기화 대상은 `adb -s <기기ID> shell pm clear kr.ac.tuk.loopcut`으로 지정합니다. `pm clear`는 앱이 설치된 상태에서 실행합니다. 처음 설치하는 새 에뮬레이터라면 초기화가 필요 없습니다.

### 정상 결과와 보고서

| 종류 | 현재 테스트 수 | 확인 범위 | 보고서 |
|---|---:|---|---|
| 단위 테스트 | 7 | 월말·윤년·첫 결제일·당일 계산, 연간 금액 환산, CSV 이스케이프, XLSX 구조 | `app/build/reports/tests/testDebugUnitTest/index.html` |
| 기기 테스트 | 5 | 화면 이동·검색·입력 상태, 로컬 저장/재읽기·이력 보관, PDF/XLSX 파일 형식 | `app/build/reports/androidTests/connected/debug/index.html` |

명령 끝에 `BUILD SUCCESSFUL`이 표시되고 보고서의 실패 건수가 0이면 통과입니다. `UP-TO-DATE`라면 이전 결과를 재사용한 것입니다. 다시 실행해야 할 때는 해당 명령 끝에 `--rerun-tasks`를 붙입니다.

기기 테스트는 모든 사용자 흐름을 자동으로 확인하지는 않습니다. 실제 회원가입 완료, 시스템 저장 창에서 파일 저장·외부 뷰어 열기 등은 위 수동 체크리스트로 확인하세요. 기존 검증 결과는 [검증 기록](../qa/VALIDATION.md)에 있으며, 지금 실행한 결과는 로컬 HTML 보고서에서 확인합니다.

## 5. APK로 직접 설치

```powershell
.\gradlew.bat :app:assembleDebug
```

생성 파일: `app/build/outputs/apk/debug/app-debug.apk`

APK를 Android 휴대폰에 옮겨 설치하거나 연결된 테스트 기기에 아래 명령으로 설치합니다. 이 저장소에는 APK를 포함하지 않습니다.

```shell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

`-r`은 기존 앱을 업데이트하며 일반적으로 로컬 데이터를 유지합니다. 설치 후 앱 목록에서 **LoopCut**을 실행하세요. 디버그 APK이며 Play Store 배포용이 아닙니다.

## 6. 자주 발생하는 문제

| 증상 | 확인할 내용 |
|---|---|
| SDK location not found | Android Studio에서 프로젝트를 열고 SDK 경로를 설정. 생성된 `local.properties`는 Git에 올리지 않음 |
| JAVA_HOME 오류 또는 Java 버전 오류 | `java -version`과 `gradlew.bat --version`으로 JDK 17 사용 여부 확인 |
| SDK 35를 찾지 못함 | SDK Manager에서 Android SDK Platform 35 설치 후 Gradle Sync |
| 의존성 다운로드 실패 | 인터넷 연결과 Gradle 오프라인 모드 확인 후 다시 Sync |
| No connected devices | 에뮬레이터 부팅 완료 여부 또는 USB 디버깅 승인 확인. `adb devices`에서 상태가 `device`여야 함 |
| 자동 테스트에서 전체 6개를 찾지 못함 | 이전 수동 테스트 데이터가 남았는지 확인. 위 초기 상태 준비 절차 사용 |
| 한글 경로에서 테스트 ClassNotFoundException | Gradle 데몬에 `-Dfile.encoding=UTF-8`을 추가하지 않음. 설정을 수정했다면 Gradle 데몬 종료 후 재실행 |
| INSTALL_FAILED_UPDATE_INCOMPATIBLE | 같은 앱 ID의 다른 서명 APK가 설치됐을 수 있음. 별도 테스트 기기 사용 또는 데이터 보존 필요 여부 확인 후 기존 앱 제거·재설치 |

## 7. 현재 실패로 처리하지 않는 데모 제한

- 로그인은 실제 인증이 아닙니다. 소셜 로그인·비밀번호 재설정은 안내만 표시합니다.
- 알림 설정은 저장만 하며 푸시·이메일·예약 알림을 보내지 않습니다.
- 실제 결제 내역은 연동하지 않았고 상세 내역은 예시입니다. 결제 내역 내보내기는 비활성화 상태입니다.
- D-day 계산용 샘플 기준일은 **2026-10-03**으로 고정되어 있습니다.
- 관리 목록에서 삭제해도 실제 외부 서비스가 해지되지 않습니다.
- 홈·분석은 제공된 디자인에 없어서 보완한 화면입니다.

서버 연결 및 미구현 항목은 [추가 작업 목록](REMAINING.md), [백엔드 연결 안내](BACKEND.md)를 참고하세요.

## 8. 문제를 발견했을 때

GitHub 이슈에 아래 내용을 남기면 재현하기 쉽습니다. 실제 비밀번호나 개인정보는 넣지 마세요.

```text
기기 / Android 버전:
사용한 커밋:
진입 경로와 재현 순서:
입력한 테스트 값:
기대한 결과:
실제 결과:
스크린샷 또는 오류 로그:
새 설치 상태에서도 재현되는지:
```
