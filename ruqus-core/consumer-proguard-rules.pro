-keep class **$$RQV { *; }
-keep class com.bkromhout.rqv.** { *; }
-keepclasseswithmembers class * {
    @com.bkromhout.rqv.* <fields>;
}
-keepclasseswithmembers class * {
    @com.bkromhout.rqv.* <methods>;
}