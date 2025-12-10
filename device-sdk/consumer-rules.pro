# Consumer ProGuard rules for MaxMind Device SDK
# These rules are automatically included when this library is consumed by other apps

# Keep SDK public API
-keep class com.maxmind.device.** { *; }

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Ktor
-keep class io.ktor.** { *; }
