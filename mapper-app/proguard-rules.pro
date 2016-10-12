# Support Library
-keep class android.support.annotation.** { *; }
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
-keep class android.support.design.** { *; }
-keep class android.support.v7.widget.LinearLayoutCompat.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }
-keep public class com.google.android.gms.R* { *; }
-dontwarn android.support.**
-dontwarn com.google.android.**

# Enumerations. Keep the special static methods that are required in enumeration classes
-keepclassmembers enum  * {
	public static **[] values();
	public static ** valueOf(java.lang.String);
}