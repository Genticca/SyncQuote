#!/usr/bin/env bash
set -euo pipefail

# ============================================================================
# SyncQuote 3개 이미지 빌드 & GHCR 푸시  ── [로컬 개발 전용]
#   ① syncquote-was         (Spring, context: 루트)
#   ② syncquote-front       (Vue+Vite, context: SyncQuote_Front)
#   ③ syncquote-collector   (Go, context: SyncQuote_Infra/crypto-collector)
#
# ※ 이 스크립트는 세 repo가 한 폴더에 중첩된 "지금 로컬"에서만 동작한다.
#   실제 prod 빌드는 각 repo의 GitHub Actions가 담당한다:
#     SyncQuote/.github/workflows/deploy-prod.yml        → syncquote-was
#     SyncQuote_Front/.github/workflows/deploy-front.yml → syncquote-front
#     SyncQuote_Infra/.github/workflows/deploy-collector.yml → syncquote-collector
#
# 사전:
#   echo "$CR_PAT" | docker login ghcr.io -u Genticca --password-stdin
# 사용:
#   REGISTRY=ghcr.io/genticca TAG=dev ./build-and-push.sh
#   (TAG 미지정 시 git short SHA 사용)
# ============================================================================

REGISTRY="${REGISTRY:-ghcr.io/genticca}"        # GHCR 네임스페이스(소문자)
TAG="${TAG:-$(git rev-parse --short HEAD 2>/dev/null || echo dev)}"
ROOT="$(cd "$(dirname "$0")" && pwd)"

build_push() {
  local name="$1" context="$2"
  local image="${REGISTRY}/${name}:${TAG}"
  echo ">>> build ${image}   (context: ${context})"
  docker build -t "${image}" "${context}"
  docker push "${image}"
  echo ">>> pushed ${image}"
  echo
}

build_push syncquote-was        "${ROOT}"
build_push syncquote-front      "${ROOT}/SyncQuote_Front"
build_push syncquote-collector  "${ROOT}/SyncQuote_Infra/crypto-collector"

cat <<EOF
완료. 매니페스트 이미지 적용 예시:
  kubectl set image deploy/api-server          api-server=${REGISTRY}/syncquote-was:${TAG}
  kubectl set image deploy/front-server        front=${REGISTRY}/syncquote-front:${TAG}
  kubectl set image deploy/collector-deployment collector=${REGISTRY}/syncquote-collector:${TAG}
  kubectl set image deploy/archiver-deployment  archiver=${REGISTRY}/syncquote-collector:${TAG}
EOF
