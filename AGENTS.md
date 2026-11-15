# AGENTS.md

## Project layout
- This repo root is a wrapper. The real Gradle project is `PracticeToeic/practicetoeicapp`.
- Single Android app module: `:app`; namespace/package `com.example.practicetoeicapp`; Java only.
- Launcher is `taikhoan.LoginActivity`; successful login routes `role == 0` to `admin.AdminActivity`, otherwise to `MainActivity`.

## Commands
Run from `PracticeToeic/practicetoeicapp`:
```bash
./gradlew :app:assembleDebug
./gradlew :app:compileDebugJavaWithJavac
./gradlew :app:lintDebug
```
- There is no test suite currently; do not invent test commands.
- `local.properties` supplies `sdk.dir`; `ANDROID_HOME` may be unset.
- Optional Supabase config is read from `local.properties` into `BuildConfig`: `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_MEDIA_BUCKET` (default `media`).
- `app/google-services.json` is required by the Google Services Gradle plugin.

## Data/architecture
- Auth and users: Firebase Auth + Realtime Database `User/{uid}`. `DatabaseAccess.iduser` is the process-wide current UID.
- SQLite `HocNgonNgu.db` is copied from `app/src/main/assets/databases/`; its `User` table is a local cache of RTDB users. Question tables are legacy during the current Firestore migration.
- Question content is migrating to Firestore through `data/QuestionRepository`; use `Source.SERVER` and numeric document IDs as strings.
- Firestore collections: `boCauHoi`, `tracnghiem`, `dienKhuyet`, `sapXepCau`, `luyenNghe`, `tuVung`, `admins`, `meta/counters`.
- Media is migrating to Supabase Storage. `luyenNghe` uses `hinhAnhUrl`; `tuVung` uses `anhUrl`. Upload path prefixes live in `data/MediaTypeUtil`.
- `data/SupabaseStorageRepository` calls the Edge Function `/functions/v1/media-upload-url`; never put a Supabase service-role key in the app.
- `notify/MyApplication` disables Firestore persistence; treat the app as online-only.

## Deployment/migration
- Firestore rules: `firebase deploy --only firestore:rules` (uses `firebase.json`).
- Supabase Edge Function: `supabase functions deploy media-upload-url --no-verify-jwt`; set `FIREBASE_PROJECT_ID` as a function secret.
- Migration script: `tools/migrate` (Node). Copy `.env.example` to `.env`, set service credentials, then `npm install && npm run migrate`. Use `DRY_RUN=true` first.
- The Edge Function hardcodes the `media` bucket; keep `SUPABASE_MEDIA_BUCKET=media` for the app and migration.

## Gotchas
- `settings.gradle` uses `FAIL_ON_PROJECT_REPOS`; add repositories in `settings.gradle`, not module `build.gradle`.
- `Database.initDatabase()` only copies the bundled DB when the target file is absent. Clear app data after changing the asset DB.
- Source media for migration is at repo root: `nghe/` and `tuvung/`, not inside `app/src/main/assets`.
- Question screens now use `QuestionRepository`; SQLite remains only as the local `User` cache.
- The README is minimal/stale; prefer Gradle config and source.
