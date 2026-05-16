# 📦 PromptVault

اپ اندروید برای ذخیره‌سازی و مدیریت پرامپت‌های هوش مصنوعی — کاملاً آفلاین.

---

## ✨ امکانات

- ✅ ذخیره پرامپت با عنوان، محتوا، دسته‌بندی، تگ و مدل AI
- ✅ جستجوی آنی در همه پرامپت‌ها
- ✅ فیلتر بر اساس دسته‌بندی و موردعلاقه‌ها
- ✅ کپی سریع پرامپت با یک ضربه
- ✅ همگام‌سازی با فایل `prompts.json` از پوشه Downloads
- ✅ وارد کردن فایل JSON از هر مکانی
- ✅ کاملاً آفلاین — بدون اینترنت، بدون سرور
- ✅ ذخیره‌سازی محلی با SQLite

---

## 🚀 راه‌اندازی و کامپایل

### روش ۱: GitHub Actions (توصیه‌شده)

1. این ریپوزیتوری را Fork یا Clone کنید
2. به GitHub بروید → **Actions** → **Build PromptVault APK**
3. روی **Run workflow** کلیک کنید
4. بعد از اتمام build، در بخش **Artifacts** فایل APK را دانلود کنید

> هر بار که کد را push کنید، APK به صورت خودکار ساخته می‌شه.

### روش ۲: کامپایل محلی

```bash
# نیاز: Java 17, Android SDK
./gradlew assembleDebug
# خروجی: app/build/outputs/apk/debug/app-debug.apk
```

---

## 📋 فرمت فایل JSON برای همگام‌سازی

فایل را با نام `prompts.json` در پوشه **Downloads** دستگاه قرار دهید:

```json
{
  "version": "1.0",
  "prompts": [
    {
      "id": "unique-id-001",
      "title": "عنوان پرامپت",
      "content": "متن کامل پرامپت...",
      "category": "کدنویسی",
      "tags": "code, python",
      "aiModel": "ChatGPT",
      "createdAt": 1700000000000,
      "updatedAt": 1700000000000,
      "isFavorite": false
    }
  ]
}
```

**نکات مهم:**
- `id` باید یونیک باشد (برای جلوگیری از ورود تکراری)
- اگر `id` تکراری باشد، پرامپت بروزرسانی می‌شه (بر اساس `updatedAt`)
- فرمت ساده‌تر (آرایه مستقیم) هم پشتیبانی می‌شه
- فیلد `content` الزامی است؛ بقیه اختیاری‌اند

### همگام‌سازی از منو:
- **🔄 همگام با Downloads** — مستقیماً از `Downloads/prompts.json` می‌خواند
- **📂 انتخاب فایل JSON** — file picker برای انتخاب دستی فایل

---

## 📁 ساختار پروژه

```
PromptVault/
├── app/src/main/
│   ├── java/com/promptvault/
│   │   ├── MainActivity.java          # صفحه اصلی
│   │   ├── AddEditPromptActivity.java # افزودن/ویرایش
│   │   ├── ViewPromptActivity.java    # نمایش کامل
│   │   ├── Prompt.java                # مدل داده
│   │   ├── PromptDatabase.java        # SQLite
│   │   ├── PromptAdapter.java         # RecyclerView
│   │   └── JsonSyncManager.java       # همگام‌سازی JSON
│   └── res/                           # منابع UI
├── .github/workflows/build.yml        # GitHub Actions
└── sample_prompts.json                # نمونه فایل JSON
```

---

## 🔄 ورژن‌بندی خودکار

برای ساخت release با tag:
```bash
git tag v1.1
git push origin v1.1
```
GitHub Actions یک Release با APK ضمیمه می‌سازد.
