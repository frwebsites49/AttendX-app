# ============================================
# ATTENDX APP - PROGUARD RULES
# ============================================

# ============================================
# ANDROIDX / MATERIAL
# ============================================

# Keep Material Design
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# ============================================
# COMPOSE
# ============================================

-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.foundation.** { *; }
-dontwarn androidx.compose.**

# Keep Composable annotations
-keep @androidx.compose.runtime.Composable class * { *; }
-keep @androidx.compose.runtime.Composable interface * { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# ============================================
# FIREBASE
# ============================================

# Keep Firebase
-keep class com.google.firebase.** { *; }
-keep interface com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep Firestore
-keep class com.google.firebase.firestore.** { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName *;
}
-keepclassmembers class * {
    @com.google.firebase.firestore.Exclude *;
}

# Keep Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# ============================================
# APP SPECIFIC CLASSES
# ============================================

# Keep all app classes
-keep class com.college.attendx.** { *; }
-keepclassmembers class com.college.attendx.** { *; }

# Keep models/data classes
-keep class com.college.attendx.models.** { *; }
-keepclassmembers class com.college.attendx.models.** {
    <init>(...);
    <fields>;
}

# Keep repositories
-keep class com.college.attendx.repositories.** { *; }

# Keep viewmodels
-keep class com.college.attendx.viewmodels.** { *; }

# Keep screens
-keep class com.college.attendx.screens.** { *; }

# Keep utils
-keep class com.college.attendx.utils.** { *; }

# Keep main classes
-keep class com.college.attendx.MainActivity { *; }
-keep class com.college.attendx.FirebaseInit { *; }

# ============================================
# KOTLIN
# ============================================

# Keep Kotlin
-keep class kotlin.** { *; }
-keep interface kotlin.** { *; }
-dontwarn kotlin.**

# Keep Kotlin reflection
-keep class kotlin.reflect.** { *; }

# Keep Kotlin metadata
-keepclassmembers class ** {
    @kotlin.Metadata *;
}

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ============================================
# ZXING QR CODE
# ============================================

-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.barcodescanner.** { *; }
-dontwarn com.google.zxing.**
-dontwarn com.journeyapps.barcodescanner.**

# ============================================
# ITEXT PDF
# ============================================

-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**
-dontwarn org.slf4j.**

# ============================================
# APACHE POI
# ============================================

-keep class org.apache.poi.** { *; }
-keep class org.apache.commons.** { *; }
-dontwarn org.apache.poi.**
-dontwarn org.apache.commons.**

# ============================================
# GOOGLE PLAY SERVICES
# ============================================

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.location.** { *; }

# ============================================
# ANDROID SDK
# ============================================

# Keep Android core
-keep class android.** { *; }
-keep class android.content.** { *; }
-keep class android.os.** { *; }
-keep class android.app.** { *; }

# Keep Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# ============================================
# REMOVE LOGS IN RELEASE
# ============================================

# Remove all debug logs
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** println(...);
}

# Remove Firebase Analytics debug logs
-assumenosideeffects class com.google.firebase.analytics.FirebaseAnalytics {
    public void logEvent(...);
}

# ============================================
# HIDE SOURCE FILES
# ============================================

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# ============================================
# OPTIMIZATION
# ============================================

-optimizationpasses 5
-allowaccessmodification
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers

# ============================================
# GENERAL ATTRIBUTES
# ============================================

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes RuntimeVisibleAnnotations
-keepattributes EnclosingMethod

# ============================================
# GENERIC RULES
# ============================================

# Keep all classes with default constructors
-keepclassmembers class * {
    public <init>(...);
}

# Keep all classes that implement Serializable
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ============================================
# PREVENT WARNINGS
# ============================================

-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.j2objc.annotations.**
-dontwarn org.checkerframework.**
-dontwarn javax.annotation.**
-dontwarn sun.misc.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.**