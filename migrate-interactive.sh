#!/bin/bash

# ============================================
# 인터랙티브 도메인 구조 마이그레이션 스크립트
# ============================================

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ROOT="src/main/java/com/aid/train/backend"

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_step() { echo -e "${BLUE}[STEP]${NC} $1"; }

# 사용자 확인 함수
confirm() {
    local message=$1
    echo -e "${YELLOW}$message (y/n):${NC} "
    read -r response
    [[ "$response" =~ ^[Yy]$ ]]
}

# 백업 생성
create_backup() {
    if confirm "백업을 생성하시겠습니까?"; then
        log_info "프로젝트 백업 생성 중..."
        BACKUP_DIR="backup_$(date +%Y%m%d_%H%M%S)"
        cp -r "$PROJECT_ROOT" "$BACKUP_DIR"
        log_info "백업 완료: $BACKUP_DIR"
        echo ""
    else
        log_warn "백업을 건너뜁니다. (권장하지 않음)"
        echo ""
    fi
}

# Step 1: Scenario 도메인 마이그레이션
step1_scenario() {
    log_step "Step 1: Scenario 도메인 마이그레이션"
    echo "- Controller 이동: controller/scenario → domain/scenario/controller"
    echo "- Service 이동: service → domain/scenario/service"
    echo "- Repository 이동: repository/scenario → domain/scenario/repository"
    echo ""
    
    if ! confirm "Scenario 도메인을 마이그레이션하시겠습니까?"; then
        log_warn "Step 1 건너뜀"
        return
    fi
    
    # 디렉토리 생성
    mkdir -p "$PROJECT_ROOT/domain/scenario/controller"
    mkdir -p "$PROJECT_ROOT/domain/scenario/service"
    mkdir -p "$PROJECT_ROOT/domain/scenario/repository"
    
    # Controller 이동
    if [ -f "$PROJECT_ROOT/controller/scenario/ScenarioController.java" ]; then
        mv "$PROJECT_ROOT/controller/scenario/ScenarioController.java" \
           "$PROJECT_ROOT/domain/scenario/controller/"
        sed -i.bak 's/package com.aid.train.backend.controller.scenario;/package com.aid.train.backend.domain.scenario.controller;/' \
            "$PROJECT_ROOT/domain/scenario/controller/ScenarioController.java"
        rm "$PROJECT_ROOT/domain/scenario/controller/ScenarioController.java.bak"
        log_info "Controller 이동 완료"
    fi
    
    # Service 이동
    if [ -f "$PROJECT_ROOT/service/ScenarioService.java" ]; then
        mv "$PROJECT_ROOT/service/ScenarioService.java" \
           "$PROJECT_ROOT/domain/scenario/service/"
        sed -i.bak 's/package com.aid.train.backend.service;/package com.aid.train.backend.domain.scenario.service;/' \
            "$PROJECT_ROOT/domain/scenario/service/ScenarioService.java"
        rm "$PROJECT_ROOT/domain/scenario/service/ScenarioService.java.bak"
        log_info "Service 이동 완료"
    fi
    
    # Repository 이동
    if [ -d "$PROJECT_ROOT/repository/scenario" ]; then
        for file in "$PROJECT_ROOT/repository/scenario/"*.java; do
            [ -f "$file" ] || continue
            filename=$(basename "$file")
            mv "$file" "$PROJECT_ROOT/domain/scenario/repository/"
            sed -i.bak 's/package com.aid.train.backend.repository.scenario;/package com.aid.train.backend.domain.scenario.repository;/' \
                "$PROJECT_ROOT/domain/scenario/repository/$filename"
            rm "$PROJECT_ROOT/domain/scenario/repository/$filename.bak"
        done
        log_info "Repository 이동 완료"
    fi
    
    log_info "✅ Step 1 완료"
    echo ""
}

# Step 2: User 도메인 마이그레이션
step2_user() {
    log_step "Step 2: User 도메인 마이그레이션"
    echo "- Repository 이동: repository/user → domain/user/repository"
    echo ""
    
    if ! confirm "User 도메인 Repository를 마이그레이션하시겠습니까?"; then
        log_warn "Step 2 건너뜀"
        return
    fi
    
    mkdir -p "$PROJECT_ROOT/domain/user/repository"
    mkdir -p "$PROJECT_ROOT/domain/user/service"
    mkdir -p "$PROJECT_ROOT/domain/user/controller"
    mkdir -p "$PROJECT_ROOT/domain/user/dto/request"
    mkdir -p "$PROJECT_ROOT/domain/user/dto/response"
    
    if [ -d "$PROJECT_ROOT/repository/user" ]; then
        for file in "$PROJECT_ROOT/repository/user/"*.java; do
            [ -f "$file" ] || continue
            filename=$(basename "$file")
            mv "$file" "$PROJECT_ROOT/domain/user/repository/"
            sed -i.bak 's/package com.aid.train.backend.repository.user;/package com.aid.train.backend.domain.user.repository;/' \
                "$PROJECT_ROOT/domain/user/repository/$filename"
            rm "$PROJECT_ROOT/domain/user/repository/$filename.bak"
        done
        log_info "Repository 이동 완료"
    fi
    
    log_info "✅ Step 2 완료"
    log_warn "Service와 Controller는 수동으로 생성 필요"
    echo ""
}

# Step 3: Session 도메인 마이그레이션
step3_session() {
    log_step "Step 3: Session 도메인 마이그레이션"
    echo "- Repository 이동: repository/session → domain/session/repository"
    echo ""
    
    if ! confirm "Session 도메인 Repository를 마이그레이션하시겠습니까?"; then
        log_warn "Step 3 건너뜀"
        return
    fi
    
    mkdir -p "$PROJECT_ROOT/domain/session/repository"
    mkdir -p "$PROJECT_ROOT/domain/session/service"
    mkdir -p "$PROJECT_ROOT/domain/session/controller"
    mkdir -p "$PROJECT_ROOT/domain/session/dto/request"
    mkdir -p "$PROJECT_ROOT/domain/session/dto/response"
    
    if [ -d "$PROJECT_ROOT/repository/session" ]; then
        for file in "$PROJECT_ROOT/repository/session/"*.java; do
            [ -f "$file" ] || continue
            filename=$(basename "$file")
            mv "$file" "$PROJECT_ROOT/domain/session/repository/"
            sed -i.bak 's/package com.aid.train.backend.repository.session;/package com.aid.train.backend.domain.session.repository;/' \
                "$PROJECT_ROOT/domain/session/repository/$filename"
            rm "$PROJECT_ROOT/domain/session/repository/$filename.bak"
        done
        log_info "Repository 이동 완료"
    fi
    
    log_info "✅ Step 3 완료"
    log_warn "Service와 Controller는 수동으로 생성 필요"
    echo ""
}

# Step 4: Global 모듈 재구성
step4_global() {
    log_step "Step 4: Global 모듈 재구성"
    echo "- response: global/common/response → global/response"
    echo "- security: global/jwt → global/security/jwt"
    echo "- exception, util 디렉토리 생성"
    echo ""
    
    if ! confirm "Global 모듈을 재구성하시겠습니까?"; then
        log_warn "Step 4 건너뜀"
        return
    fi
    
    # response 재구성
    if [ -d "$PROJECT_ROOT/global/common/response" ]; then
        mkdir -p "$PROJECT_ROOT/global/response"
        for file in "$PROJECT_ROOT/global/common/response/"*.java; do
            [ -f "$file" ] || continue
            filename=$(basename "$file")
            mv "$file" "$PROJECT_ROOT/global/response/"
            sed -i.bak 's/package com.aid.train.backend.global.common.response;/package com.aid.train.backend.global.response;/' \
                "$PROJECT_ROOT/global/response/$filename"
            rm "$PROJECT_ROOT/global/response/$filename.bak"
        done
        rm -rf "$PROJECT_ROOT/global/common"
        log_info "response 재구성 완료"
    fi
    
    # security/jwt 재구성
    if [ -d "$PROJECT_ROOT/global/jwt" ]; then
        mkdir -p "$PROJECT_ROOT/global/security/jwt"
        for file in "$PROJECT_ROOT/global/jwt/"*.java; do
            [ -f "$file" ] || continue
            filename=$(basename "$file")
            mv "$file" "$PROJECT_ROOT/global/security/jwt/"
            sed -i.bak 's/package com.aid.train.backend.global.jwt;/package com.aid.train.backend.global.security.jwt;/' \
                "$PROJECT_ROOT/global/security/jwt/$filename"
            rm "$PROJECT_ROOT/global/security/jwt/$filename.bak"
        done
        rm -rf "$PROJECT_ROOT/global/jwt"
        log_info "security 재구성 완료"
    fi
    
    # 디렉토리 생성
    mkdir -p "$PROJECT_ROOT/global/exception/custom"
    mkdir -p "$PROJECT_ROOT/global/util"
    log_info "exception, util 디렉토리 생성 완료"
    
    log_info "✅ Step 4 완료"
    echo ""
}

# Step 5: 빈 디렉토리 정리
step5_cleanup() {
    log_step "Step 5: 빈 디렉토리 정리"
    echo "- controller, service, repository 최상위 디렉토리 삭제"
    echo ""
    
    if ! confirm "빈 디렉토리를 정리하시겠습니까?"; then
        log_warn "Step 5 건너뜀"
        return
    fi
    
    [ -d "$PROJECT_ROOT/controller" ] && rm -rf "$PROJECT_ROOT/controller" && log_info "controller/ 삭제"
    [ -d "$PROJECT_ROOT/service" ] && rm -rf "$PROJECT_ROOT/service" && log_info "service/ 삭제"
    [ -d "$PROJECT_ROOT/repository" ] && rm -rf "$PROJECT_ROOT/repository" && log_info "repository/ 삭제"
    
    log_info "✅ Step 5 완료"
    echo ""
}

# 최종 리포트
final_report() {
    echo ""
    echo "=========================================="
    log_info "🎉 마이그레이션 완료!"
    echo "=========================================="
    echo ""
    echo "✅ 완료된 작업:"
    echo "  - Scenario 도메인 완전 마이그레이션"
    echo "  - User/Session 도메인 Repository 이동"
    echo "  - Global 모듈 재구성"
    echo "  - 패키지 선언부 자동 수정"
    echo ""
    echo "⚠️  다음 작업 필요:"
    echo "  1. IDE에서 프로젝트 열기"
    echo "  2. Import 최적화 (IntelliJ: Ctrl+Alt+O)"
    echo "  3. ./gradlew clean build 실행"
    echo "  4. User/Session Service, Controller 생성"
    echo ""
}

# 메인 함수
main() {
    echo ""
    echo "=========================================="
    log_info "도메인 구조 마이그레이션 스크립트"
    echo "=========================================="
    echo ""
    
    create_backup
    step1_scenario
    step2_user
    step3_session
    step4_global
    step5_cleanup
    final_report
}

main
