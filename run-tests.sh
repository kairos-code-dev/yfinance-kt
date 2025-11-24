#!/bin/bash

# YFinance-KT Test Runner
# This script runs tests with proper error handling and reporting

set -e

echo "======================================"
echo "YFinance-KT Test Runner"
echo "======================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# Check for test argument
TEST_FILTER=""
if [ ! -z "$1" ]; then
    TEST_FILTER="--tests $1"
    print_status "Running specific test: $1"
else
    print_status "Running all tests"
fi

# Clean build directory
print_status "Cleaning build directory..."
./gradlew clean --quiet

# Compile code
print_status "Compiling code..."
./gradlew compileKotlin compileTestKotlin --quiet

# Run tests
print_status "Running tests..."
echo ""

if ./gradlew test $TEST_FILTER --console=plain; then
    echo ""
    print_status "${GREEN}✓ All tests passed!${NC}"
    echo ""

    # Show test summary
    if [ -f "build/test-results/test/TEST-*.xml" ]; then
        print_status "Test Summary:"
        find build/test-results/test -name "TEST-*.xml" -exec grep -h "tests=" {} \; | head -1
    fi

    # Show test report location
    if [ -f "build/reports/tests/test/index.html" ]; then
        print_status "Detailed test report: build/reports/tests/test/index.html"
    fi
else
    echo ""
    print_error "${RED}✗ Tests failed!${NC}"
    echo ""

    # Show test report location
    if [ -f "build/reports/tests/test/index.html" ]; then
        print_warning "Check detailed test report: build/reports/tests/test/index.html"
    fi

    exit 1
fi
