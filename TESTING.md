# Testing Guide

이 문서는 YFinance-KT 라이브러리의 테스트 실행 방법을 설명합니다.

## 🧪 테스트 개요

총 **29개의 포괄적 테스트**가 구현되어 있습니다:

### 테스트 카테고리

1. **기본 기능** (7개)
   - 히스토리 데이터 조회
   - 티커 정보 조회
   - 배당금 & 스플릿
   - 캘린더 이벤트
   - 뉴스 조회

2. **재무제표** (6개)
   - 손익계산서 (연간/분기)
   - 대차대조표
   - 현금흐름표

3. **애널리스트 & 보유 정보** (4개)
   - 추천 등급
   - 주요 보유자
   - 기관 보유자
   - 실적 히스토리

4. **실적 데이터** (3개)
   - 실적 히스토리
   - 전체 실적 데이터
   - 실적 캘린더

5. **옵션 & 파생상품** (3개)
   - 옵션 만기일 조회
   - 옵션 체인 조회
   - 옵션 계약 헬퍼 메서드

6. **추가 데이터** (6개)
   - Fast Info
   - Sustainability/ESG
   - Capital Gains
   - Shares Outstanding
   - 코퍼레이트 액션
   - 옵션 헬퍼 메서드

## 🚀 테스트 실행 방법

### 방법 1: 간단한 스크립트 사용

```bash
# 모든 테스트 실행
./run-tests.sh

# 특정 테스트만 실행
./run-tests.sh "TickerTest.test get options expiration dates for AAPL"
```

### 방법 2: Gradle 직접 사용

```bash
# 모든 테스트 실행
./gradlew test

# Clean build 후 테스트
./gradlew clean test

# 특정 테스트 클래스만 실행
./gradlew test --tests TickerTest

# 특정 테스트 메서드만 실행
./gradlew test --tests "TickerTest.test get historical data for AAPL"
./gradlew test --tests "TickerTest.test get options expiration dates for AAPL"

# 상세 출력과 함께 실행
./gradlew test --info

# 테스트 결과만 보기
./gradlew test --console=plain
```

### 방법 3: IDE에서 실행

**IntelliJ IDEA / Android Studio:**
1. `src/test/kotlin/io/github/yfinance/TickerTest.kt` 파일 열기
2. 클래스 또는 개별 테스트 왼쪽의 녹색 실행 버튼 클릭
3. 또는 `Ctrl+Shift+F10` (Windows/Linux) / `Cmd+Shift+R` (Mac)

## 📊 테스트 결과 확인

### HTML 리포트 보기

테스트 실행 후 자동으로 생성되는 HTML 리포트:

```bash
# 브라우저에서 열기
open build/reports/tests/test/index.html  # Mac
xdg-open build/reports/tests/test/index.html  # Linux
start build/reports/tests/test/index.html  # Windows
```

### XML 결과 파일

JUnit XML 형식의 결과:
```
build/test-results/test/TEST-*.xml
```

## 🔧 테스트 설정

### 환경 요구사항

- **Java**: 17 이상
- **Kotlin**: 2.0.21
- **Gradle**: 8.0 이상
- **네트워크**: Yahoo Finance API 접근 필요 (query2.finance.yahoo.com)

### 테스트 타임아웃

각 테스트는 30초 타임아웃이 설정되어 있습니다. 네트워크가 느린 경우 다음과 같이 늘릴 수 있습니다:

```kotlin
// build.gradle.kts에 추가
tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    // 타임아웃 설정
    systemProperty("junit.jupiter.execution.timeout.default", "60s")
}
```

## 🌐 네트워크 테스트

이 테스트들은 **실제 Yahoo Finance API를 호출**합니다:

```bash
# Yahoo Finance API 연결 테스트
curl -I https://query2.finance.yahoo.com

# 예상 출력:
# HTTP/1.1 200 OK
```

### 네트워크 문제 해결

만약 네트워크 오류가 발생하면:

1. **방화벽 확인**: query2.finance.yahoo.com 접근 허용
2. **프록시 설정**:
   ```bash
   export GRADLE_OPTS="-Dhttps.proxyHost=proxy.company.com -Dhttps.proxyPort=8080"
   ```
3. **DNS 확인**:
   ```bash
   nslookup query2.finance.yahoo.com
   ```

## 🔄 CI/CD - GitHub Actions

프로젝트에는 GitHub Actions가 설정되어 있어 자동으로 테스트가 실행됩니다:

### 트리거 조건

- `main` 브랜치에 push
- `claude/**` 브랜치에 push
- Pull Request 생성/업데이트
- 수동 실행 (workflow_dispatch)

### GitHub에서 결과 확인

1. GitHub 저장소로 이동
2. **Actions** 탭 클릭
3. 최근 워크플로우 실행 확인
4. 테스트 결과 및 아티팩트 다운로드

### 로컬에서 GitHub Actions 테스트

[act](https://github.com/nektos/act)를 사용하면 로컬에서 GitHub Actions를 실행할 수 있습니다:

```bash
# act 설치
brew install act  # Mac
# 또는 다른 방법: https://github.com/nektos/act#installation

# 워크플로우 실행
act push
```

## 📝 테스트 작성 가이드

새로운 기능을 추가할 때 테스트 작성:

```kotlin
@Test
fun `test new feature for AAPL`() = runBlocking {
    val ticker = Ticker("AAPL")
    val result = ticker.newFeature()

    assertTrue(result.isSuccess(), "Expected successful result")

    when (result) {
        is YFinanceResult.Success -> {
            val data = result.data
            assertNotNull(data)
            // 추가 검증...
        }
        is YFinanceResult.Error -> {
            throw AssertionError("Unexpected error: ${result.message}")
        }
    }
}
```

## 🐛 테스트 디버깅

### 로그 활성화

```kotlin
val ticker = Ticker("AAPL", enableLogging = true)
```

### 상세 Gradle 출력

```bash
./gradlew test --debug > test-debug.log 2>&1
```

### 특정 테스트만 디버그

```bash
./gradlew test --tests "TickerTest.test*" --debug-jvm
```

그 다음 IDE에서 Remote JVM Debug 설정 (포트 5005)

## 📈 테스트 커버리지

커버리지 리포트 생성 (JaCoCo):

```kotlin
// build.gradle.kts에 추가
plugins {
    jacoco
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
```

실행:
```bash
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## 🎯 특정 API 테스트 예제

### 옵션 데이터 테스트
```bash
./gradlew test --tests "*option*"
```

### 재무제표 테스트
```bash
./gradlew test --tests "*income statement*"
./gradlew test --tests "*balance sheet*"
./gradlew test --tests "*cash flow*"
```

### ESG 데이터 테스트
```bash
./gradlew test --tests "*sustainability*"
```

### Fast Info 테스트
```bash
./gradlew test --tests "*fast info*"
```

## ⚡ 성능 테스트

병렬 테스트 실행으로 속도 향상:

```kotlin
// build.gradle.kts
tasks.test {
    maxParallelForks = Runtime.runtime.availableProcessors() / 2
}
```

## 📚 추가 리소스

- [JUnit 5 문서](https://junit.org/junit5/docs/current/user-guide/)
- [Kotlin 코루틴 테스팅](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)
- [Ktor Client 테스팅](https://ktor.io/docs/client-testing.html)
