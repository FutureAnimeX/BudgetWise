# BudgetWise ProGuard rules

# Keep Room entities
-keep class com.budgetwise.data.models.** { *; }

# Keep BCrypt
-keep class org.mindrot.jbcrypt.** { *; }

# Keep MPAndroidChart
-keep class com.github.mikephil.** { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
