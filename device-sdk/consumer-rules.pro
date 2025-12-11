# Consumer ProGuard rules for MaxMind Device SDK
# These rules will be automatically applied to apps that use this library

# Keep public SDK API
-keep public class com.maxmind.device.** {
    public protected *;
}

# Keep SDK entry points
-keep class com.maxmind.device.DeviceTracker { *; }

# Keep configuration classes (used via Builder pattern)
-keep class com.maxmind.device.config.SdkConfig { *; }
-keep class com.maxmind.device.config.SdkConfig$Builder { *; }

# Kotlin serialization rules for SDK data classes
-keepattributes InnerClasses

# Keep all model classes (data classes with @Serializable)
-keep class com.maxmind.device.model.** { *; }

-keepclassmembers class com.maxmind.device.model.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class com.maxmind.device.**
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class com.maxmind.device.** {
    static **$* *;
}
-keepnames class <2>.<3>

# Note: Internal collector/helper classes (com.maxmind.device.collector.**)
# are intentionally NOT kept - they can be optimized by R8 as they're not
# part of the public API.
