#!/bin/bash

# 로컬 환경에서 테스트를 실행하는 스크립트

echo "=================================="
echo "YFinance-KT Local Test Runner"
echo "=================================="
echo ""

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 단계별 실행
echo -e "${BLUE}[1/5]${NC} Gradle wrapper 준비 중..."
if [ ! -f "./gradlew" ]; then
    echo -e "${YELLOW}Warning:${NC} gradlew not found. Using system gradle"
    GRADLE_CMD="gradle"
else
    chmod +x ./gradlew
    GRADLE_CMD="./gradlew"
fi

echo -e "${BLUE}[2/5]${NC} 의존성 다운로드 중..."
$GRADLE_CMD dependencies --configuration testRuntimeClasspath > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} 의존성 다운로드 완료"
else
    echo -e "${YELLOW}⚠${NC} 의존성 다운로드 실패 (오프라인 모드로 진행)"
fi

echo -e "${BLUE}[3/5]${NC} 코드 컴파일 중..."
$GRADLE_CMD compileKotlin compileTestKotlin --console=plain
if [ $? -ne 0 ]; then
    echo -e "${RED}✗${NC} 컴파일 실패"
    exit 1
fi
echo -e "${GREEN}✓${NC} 컴파일 완료"

echo -e "${BLUE}[4/5]${NC} 테스트 실행 중..."
echo ""

$GRADLE_CMD test --console=plain --info 2>&1 | tee test-output.log

TEST_RESULT=$?

echo ""
echo -e "${BLUE}[5/5]${NC} 결과 분석 중..."
echo ""

if [ $TEST_RESULT -eq 0 ]; then
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}✓ 모든 테스트 통과!${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

    # 테스트 요약 표시
    if [ -f "build/test-results/test/index.html" ]; then
        echo ""
        echo "📊 상세 리포트: build/reports/tests/test/index.html"
    fi
else
    echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${RED}✗ 일부 테스트 실패${NC}"
    echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

    echo ""
    echo -e "${YELLOW}실패한 테스트 확인:${NC}"
    grep -A 5 "FAILED" test-output.log | head -30 || echo "로그에서 실패 정보를 찾을 수 없습니다."

    echo ""
    echo "📋 전체 로그: test-output.log"
    echo "📊 상세 리포트: build/reports/tests/test/index.html"
fi

echo ""
exit $TEST_RESULT
