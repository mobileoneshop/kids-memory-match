# Proguard rules for Kids Memory Match

# Keep model classes used for parsing JSON assets
-keep class com.one.memorymatch.data.model.** { *; }

# Keep Compose Runtime & annotations
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# AndroidX DataStore Preferences Protobuf
-dontwarn sun.misc.Unsafe
-keep class androidx.datastore.preferences.protobuf.** { *; }
