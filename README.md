# Bluetooth Microphone (Android / Kotlin)

یک پروژه اندروید با زبان Kotlin که صدای میکروفون گوشی را به‌صورت زنده به اسپیکر بلوتوثی **A2DP متصل** می‌فرستد.

## راه‌اندازی

1. پوشه پروژه را در **Android Studio** باز کنید (Open).
2. **JDK 17** و Android SDK Platform 35 را نصب کنید. اینترنت برای دریافت Gradle و Android Gradle Plugin نیاز است.
3. منتظر **Gradle Sync** بمانید. در صورت نیاز، Android Studio می‌تواند Gradle را از تنظیمات wrapper دریافت کند.
4. اسپیکر را در تنظیمات بلوتوث گوشی Pair و Connect کنید و مطمئن شوید خروجی پخش Media روی همان دستگاه است.
5. برنامه را روی **گوشی واقعی** نصب و اجرا کنید؛ مجوز Microphone را بدهید و «شروع انتقال صدا» را بزنید.
6. برای پایان «توقف انتقال صدا» را بزنید.

## ساخت APK

از مسیر **Build > Build Bundle(s) / APK(s) > Build APK(s)** در Android Studio استفاده کنید. خروجی Debug معمولاً در `app/build/outputs/apk/debug/app-debug.apk` قرار دارد.

یا روی macOS/Linux با `./gradlew assembleDebug` و روی Windows با `gradlew.bat assembleDebug` فایل را بسازید. اسکریپت‌های gradlew همراه پروژه برای دریافت توزیع Gradle در اجرای نخست طراحی شده‌اند (فایل jar رسمی wrapper به‌دلیل دسترسی نداشتن محیط تولید به دانلود بیرونی در ZIP نیست).

## نکات و محدودیت‌ها

- روش اپ بر پایه **AudioRecord -> AudioTrack** است؛ ارتباط خام صوتی با اسپیکر برقرار نمی‌کند و از مسیر Media Audio اندروید استفاده می‌کند.
- تنها اسپیکر بلوتوثی **A2DP که قبلاً به گوشی وصل شده** پشتیبانی می‌شود. برنامه خودِ عملیات Pair را انجام نمی‌دهد.
- در بلوتوث A2DP ممکن است تأخیر محسوس وجود داشته باشد. برای اجرای زنده بدون تأخیر مناسب نیست.
- `setPreferredDevice()` درخواست مسیر خروجی است؛ برخی گوشی‌ها ممکن است آن را نادیده بگیرند. روی گوشی واقعی بررسی کنید که صدا از خود گوشی پخش نشود.
- فعالیت در پس‌زمینه پشتیبانی نمی‌شود: با خروج از صفحه میکروفون متوقف می‌شود.
- گوشی را از اسپیکر دور کنید تا فیدبک یا سوت ایجاد نشود.
- **هنوز APK کامپایل یا روی دستگاه واقعی تست نشده است.**

## ساختار

- `app/src/main/java/com/example/bluetoothmicrophone/MainActivity.kt` رابط و درخواست دسترسی
- `app/src/main/java/com/example/bluetoothmicrophone/AudioStreamer.kt` انتقال صدا
- `app/src/main/res/layout/activity_main.xml` رابط کاربری
- `app/src/main/AndroidManifest.xml` مجوزها
- `gradle/wrapper/gradle-wrapper.properties` نسخه Gradle

## تکنولوژی

Kotlin 2.2.10 / Android Gradle Plugin 8.10.1 / Gradle 8.11.1 / minSdk 26 / targetSdk 35.
