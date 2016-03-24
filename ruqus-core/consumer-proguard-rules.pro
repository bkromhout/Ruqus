-keep class **$$Ruqus** { *; }
-keep class com.bkromhout.ruqus.** { *; }
-keepclasseswithmembers class * {
    @com.bkromhout.ruqus.* <fields>;
}
-keepclasseswithmembers class * {
    @com.bkromhout.ruqus.* <methods>;
}
# TODO Keep Parcelable CREATOR objects (or, make sure we don't need to)