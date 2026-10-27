# قوانین ProGuard برای تقویم هوشمند

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Kotlin coroutines
-dontwarn kotlinx.coroutines.**

# Compose (بیشتر قواعد لازم به‌صورت پیش‌فرض در AAR ها هست)
-dontwarn androidx.compose.**

# جلوگیری از تغییر نام کلاس‌های داده که با JSONObject کار می‌کنند
-keepclassmembers class ir.hooshmandcalendar.app.data.db.EventEntity {
    public <fields>;
}
