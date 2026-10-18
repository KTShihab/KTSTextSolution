# KTS Text Solution Proguard / R8 Rules

# Jsoup HTML parsing
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# AndroidX DataStore & Kotlin
-dontwarn kotlin.reflect.**
-keepclassmembers class **$WhenMappings { *; }
