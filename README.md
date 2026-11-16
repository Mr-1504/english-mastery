# English Mastery (TOEIC Practice App)

Ứng dụng Android (Java) luyện thi TOEIC. Repo gốc là wrapper; project Gradle thực tế nằm tại `PracticeToeic/practicetoeicapp` (module `:app`, package `com.example.practicetoeicapp`).

## Tính năng
- **Tài khoản**: đăng ký/đăng nhập (Firebase Auth), quên mật khẩu, xem/cập nhật thông tin (họ tên, SĐT), **đổi ảnh đại diện** (upload lên Supabase Storage, lưu URL vào Firebase RTDB), đăng xuất.
- **Học tập/Luyện tập**: từ vựng, trắc nghiệm, điền khuyết, sắp xếp câu, luyện nghe; xem lịch sử/kết quả, bảng xếp hạng.
- **Hồ sơ & giao diện**: điểm, vai trò, chuyển chế độ sáng/tối, hiển thị **phiên bản (versionName/versionCode) và ngày build**.
- **Quản trị (role == 0)**: quản lý bộ học tập và nội dung (từ vựng, trắc nghiệm, sắp xếp câu, luyện nghe, điền khuyết, ...).

## Công nghệ & kiến trúc dữ liệu
- **Auth/Người dùng**: Firebase Auth + Realtime Database `User/{uid}` (`hoTen`, `point`, `email`, `sdt`, `role`, `avatarUrl`). `DatabaseAccess.iduser` là UID hiện tại.
- **SQLite** `HocNgonNgu.db` (copy từ `app/src/main/assets/databases/`, qua `sqliteassethelper`): cache local bảng `User` (schema asset hiện không có cột `avatarUrl`, URL avatar đọc từ RTDB). Câu hỏi là legacy trong quá trình migrate.
- **Nội dung câu hỏi**: đang migrate sang **Firestore** qua `data/QuestionRepository` (`Source.SERVER`, id số dạng chuỗi). Collections: `boCauHoi`, `tracnghiem`, `dienKhuyet`, `sapXepCau`, `luyenNghe`, `tuVung`, `admins`, `meta/counters`.
- **Media**: **Supabase Storage** (bucket `media`). `luyenNghe` dùng `hinhAnhUrl`, `tuVung` dùng `anhUrl`, avatar user tại `avatars/{uid}/...`. Upload qua Edge Function `/functions/v1/media-upload-url` — **không** nhúng service-role key trong app.
- App chạy **online-only** (`notify/MyApplication` tắt Firestore persistence). `local.properties` nạp `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_MEDIA_BUCKET` (mặc định `media`) vào `BuildConfig`.

## Cấu trúc repo
- `PracticeToeic/practicetoeicapp` — project Gradle (module `:app`).
- `supabase/functions/media-upload-url` — Edge Function cấp signed upload URL (admin cho media nội dung; user đăng nhập cho `avatars/{uid}/` của chính mình).
- `firestore.rules`, `firebase.json` — rules/cấu hình Firestore.
- `tools/migrate` — script Node migrate SQLite → Firestore (`npm install && npm run migrate`, dùng `DRY_RUN=true` trước; copy `.env.example` → `.env`).
- `nghe/`, `tuvung/` — media nguồn cho migrate (ở root, không trong `app/src/main/assets`).
- `docs/plan-toeic-content.md` — kế hoạch model nội dung (`questionSets` / `questionGroups` / `questions`).
- `.github/workflows/` — CI/CD (xem bên dưới).

## Yêu cầu phát triển
- JDK 17, Android SDK (compile/target 34, minSdk 27). `ANDROID_HOME` có thể chưa set; `local.properties` phải có `sdk.dir`.
- `app/google-services.json` (Firebase, bắt buộc cho Google Services plugin) — file bị gitignore, cần tự cung cấp cục bộ.
- `PracticeToeic/practicetoeicapp/local.properties` (gitignore):
  ```properties
  sdk.dir=/path/to/Android/Sdk
  SUPABASE_URL=...
  SUPABASE_ANON_KEY=...
  SUPABASE_MEDIA_BUCKET=media
  ```
- Lưu ý: `settings.gradle` dùng `FAIL_ON_PROJECT_REPOS`; `Database.initDatabase()` chỉ copy DB asset khi file đích chưa tồn tại (clear app data nếu thay đổi asset DB).

## Build
Chạy từ `PracticeToeic/practicetoeicapp`:
```bash
./gradlew :app:assembleDebug
./gradlew :app:compileDebugJavaWithJavac
./gradlew :app:lintDebug
```
Hiện chưa có test suite.

## CI/CD (GitHub Actions)
- **`release-pipeline.yml`** (trigger: PR **merged** vào nhánh `release`): dùng `commit-and-tag-version` (Conventional Commits) để bump `versionName` + tăng `versionCode` trong `app/build.gradle`, cập nhật `CHANGELOG.md`, tạo tag `vX.Y.Z`, push commit/tag lên `release` rồi merge thẳng `release` → `main`.
- **`build-main.yml`** (trigger: push `main` / tag `v*`): khôi phục `google-services.json` & `local.properties` từ secrets, build `:app:assembleDebug`, upload artifact APK, phân phối Firebase App Distribution (nếu cấu hình), và tạo GitHub Release kèm APK + ghi chú.

### Secrets (Actions)
- `GOOGLE_SERVICES_JSON_B64` (bắt buộc, base64 của `google-services.json`)
- `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_MEDIA_BUCKET`
- `FIREBASE_APP_ID`, `FIREBASE_SERVICE_ACCOUNT` (JSON), `FIREBASE_TESTER_GROUPS` (Firebase App Distribution)
- (Tuỳ chọn) `RELEASE_TOKEN` nếu branch protection chặn push/merge trực tiếp

Quy ước: commit/PR title theo **Conventional Commits** (`feat`, `fix`, …) để tự bump version đúng.

## Deploy dịch vụ liên quan
```bash
# Firestore rules
firebase deploy --only firestore:rules

# Supabase Edge Function (cập nhật khi đổi logic upload/avatar)
supabase functions deploy media-upload-url --no-verify-jwt
```
