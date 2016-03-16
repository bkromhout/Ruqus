-keep class **$$Ruqus** { *; }
-keep class com.bkromhout.ruqus.** { *; }
-keepclasseswithmembers class * {
    @com.bkromhout.ruqus.* <fields>;
}
-keepclasseswithmembers class * {
    @com.bkromhout.ruqus.* <methods>;
}