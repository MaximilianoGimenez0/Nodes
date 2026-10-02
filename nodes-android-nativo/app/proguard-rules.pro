# ==============================================================================
# PROGUARD / R8 RULES - NODES APP
# ==============================================================================

# --- Trazabilidad y Stack Traces (Crashlytics / Logs) ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Atributos de Reflexión y Genéricos ---
# Requerido por Firebase, Kotlin Coroutines y deserialización de genéricos (ej: List<ChecklistItem>)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# --- MODELOS DE DATOS Y ENTIDADES (Firestore / Realtime Database / DataStore) ---
# Mantiene todos los modelos de datos en el paquete exacto del proyecto
-keep class com.maxigimenez.nodes.data.model.** { *; }
-keepclassmembers class com.maxigimenez.nodes.data.model.** {
    public <init>();
    public <fields>;
    public <methods>;
}

# Preservar propiedades y métodos anotados con @PropertyName (Firebase Firestore)
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.PropertyName <fields>;
}

# --- VIEW MODELS Y LIFECYCLE ---
# Preservar constructores de ViewModels para ViewModelProvider por reflexión
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# --- GOOGLE PLAY SERVICES (AUTH) ---
-dontwarn com.google.android.gms.**

# --- CORRUTINAS (KotlinX Coroutines) ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# --- COIL (Manejo de imágenes en Compose) ---
-keep class coil.** { *; }
-dontwarn coil.**
-dontwarn okio.**

# --- ANOTACIÓN @KEEP ---
# Mantiene cualquier clase o miembro marcado con androidx.annotation.Keep
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
