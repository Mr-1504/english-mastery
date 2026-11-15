#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

if [[ -f "$ROOT_DIR/deploy.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$ROOT_DIR/deploy.env"
  set +a
fi

FIREBASE_PROJECT_ID="${FIREBASE_PROJECT_ID:-practicetoiecapp}"
SUPABASE_PROJECT_REF="${SUPABASE_PROJECT_REF:-}"
DEPLOY_FIREBASE="${DEPLOY_FIREBASE:-true}"
DEPLOY_SUPABASE="${DEPLOY_SUPABASE:-true}"
RUN_MIGRATION="${RUN_MIGRATION:-false}"

usage() {
  cat <<'EOF'
Usage: ./deploy.sh [--help]

Reads optional deploy.env from the repo root, then deploys:
  - Firestore rules to Firebase
  - FIREBASE_PROJECT_ID secret to Supabase
  - media-upload-url Edge Function to Supabase

Required:
  SUPABASE_PROJECT_REF   Supabase project ref (20 lowercase letters)

Optional:
  FIREBASE_PROJECT_ID    Default: practicetoiecapp
  SUPABASE_ACCESS_TOKEN  If omitted, the script runs `supabase login`
  DEPLOY_FIREBASE        true/false, default true
  DEPLOY_SUPABASE        true/false, default true
  RUN_MIGRATION          true/false, default false

Migration also requires tools/migrate/.env.
EOF
}

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
  usage
  exit 0
fi

require_npx() {
  if ! command -v npx >/dev/null 2>&1; then
    echo "Loi: khong tim thay npx. Hay cai Node.js 20+." >&2
    exit 1
  fi
}

deploy_firebase() {
  echo "==> Deploy Firestore rules (project: $FIREBASE_PROJECT_ID)"
  if ! npx --yes firebase-tools projects:list >/dev/null 2>&1; then
    echo "Chua dang nhap Firebase CLI, mo dang nhap..."
    npx --yes firebase-tools login
  fi
  npx --yes firebase-tools deploy \
    --project "$FIREBASE_PROJECT_ID" \
    --only firestore:rules \
    --non-interactive
}

deploy_supabase() {
  if [[ -z "$SUPABASE_PROJECT_REF" ]]; then
    echo "Loi: thieu SUPABASE_PROJECT_REF trong deploy.env hoac bien moi truong." >&2
    echo "Vi du: SUPABASE_PROJECT_REF=abcdefghijklmnopqrst" >&2
    exit 1
  fi

  if [[ -z "${SUPABASE_ACCESS_TOKEN:-}" ]]; then
    echo "Chua co SUPABASE_ACCESS_TOKEN, mo dang nhap Supabase..."
    npx --yes supabase login
  fi

  echo "==> Set Supabase secret FIREBASE_PROJECT_ID"
  npx --yes supabase secrets set \
    "FIREBASE_PROJECT_ID=$FIREBASE_PROJECT_ID" \
    --remote="$SUPABASE_PROJECT_REF" \
    --yes

  echo "==> Deploy Edge Function media-upload-url"
  local deploy_cmd=(
    npx --yes supabase functions deploy media-upload-url
    --remote="$SUPABASE_PROJECT_REF"
    --no-verify-jwt
    --use-api
    --yes
  )
  if ! "${deploy_cmd[@]}"; then
    echo "Deploy function bi timeout, thu lai lan 2..." >&2
    "${deploy_cmd[@]}"
  fi
}

run_migration() {
  local migrate_dir="$ROOT_DIR/tools/migrate"
  if [[ ! -f "$migrate_dir/.env" ]]; then
    echo "Loi: chua co tools/migrate/.env. Copy tu .env.example truoc." >&2
    exit 1
  fi
  if [[ ! -d "$migrate_dir/node_modules" ]]; then
    echo "==> Cai dependency cho migration"
    (cd "$migrate_dir" && npm install --no-audit --no-fund)
  fi
  echo "==> Chay migration"
  (cd "$migrate_dir" && npm run migrate)
}

require_npx

if [[ "$DEPLOY_FIREBASE" == "true" ]]; then
  deploy_firebase
else
  echo "==> Bo qua Firebase"
fi

if [[ "$DEPLOY_SUPABASE" == "true" ]]; then
  deploy_supabase
else
  echo "==> Bo qua Supabase"
fi

if [[ "$RUN_MIGRATION" == "true" ]]; then
  run_migration
else
  echo "==> Bo qua migration (dat RUN_MIGRATION=true de chay)"
fi

cat <<'EOF'

Hoan tat deploy.

Buoc tiep theo:
1. Them SUPABASE_URL va SUPABASE_ANON_KEY vao PracticeToeic/practicetoeicapp/local.properties.
2. Tao bucket "media" tren Supabase va bat Public.
3. Tao admin:
   - Realtime Database: User/<UID>/role = 0
   - Firestore: admins/<UID>
4. Rebuild app:
   cd PracticeToeic/practicetoeicapp
   ./gradlew :app:assembleDebug
EOF
