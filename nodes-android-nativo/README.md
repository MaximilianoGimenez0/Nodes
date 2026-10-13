# 📱 NODES Android Nativo — Mobile Client & Hardware Services

> Cliente móvil de alto rendimiento desarrollado en **Kotlin 2.2** y **Jetpack Compose** (BOM 2024.09.00), diseñado para **Android SDK 36** (UpsideDownCake+). Implementa arquitectura UDF reactiva, sincronización en tiempo real con Cloud Firestore, gestión local no volátil con Preferences DataStore, alarmas de hardware de precisión exacta con `AlarmManager`, resiliencia de arranque mediante `BootReceiver` y recepción en background de notificaciones push FCM con deep linking.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android SDK](https://img.shields.io/badge/Android_SDK-36_(API_24+)--3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com/about/versions/15)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-BOM_2024.09.00-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material_3-1.4.0-7986CB?style=flat&logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![Navigation Compose](https://img.shields.io/badge/Navigation_Compose-2.9.5-00897B?style=flat&logo=android&logoColor=white)](https://developer.android.com/guide/navigation)
[![Firebase BoM](https://img.shields.io/badge/Firebase_BoM-34.3.0-FFCA28?style=flat&logo=firebase&logoColor=black)](https://firebase.google.com/)
[![Preferences DataStore](https://img.shields.io/badge/DataStore-1.0.0-3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com/topic/libraries/architecture/datastore)
[![Coil Compose](https://img.shields.io/badge/Coil_Compose-2.7.0-689F38?style=flat&logo=coil&logoColor=white)](https://coil-kt.github.io/coil/)
[![AGP](https://img.shields.io/badge/AGP-9.2.1-02569B?style=flat&logo=gradle&logoColor=white)](https://developer.android.com/build)

[← Volver al Ecosistema General](../README.md) · [Portal Web (React 19 SPA)](../nodes-front-web) · [Microservicios Serverless (Cloud Functions v2)](../nodes-functions)

---

## Visión General & Propósito del Módulo

**nodes-android-nativo** es la aplicación cliente principal del ecosistema NODES. Proporciona una experiencia móvil táctil, responsiva y cinemática optimizada para renderizar a 60 FPS sin bloqueos en el hilo principal (`Main thread`).

### Capacidades Centrales
1. **Espacios de Trabajo Colaborativos ("Nodos"):** Creación y unión instantánea a salas mediante códigos alfanuméricos cortos únicos de 6 caracteres.
2. **Entradas Multimodales Polimórficas:** Manejo tipado de mensajes de texto, listas interactivas de tareas (`CHECK_LIST`), notas con imágenes comprimidas (`IMAGE`) y recordatorios temporizados (`REMINDER`).
3. **Motor de Alarmas de Hardware:** Planificación precisa en `AlarmManager` con `setExactAndAllowWhileIdle` en modo `RTC_WAKEUP`, inmune al Doze Mode y restaurable tras el reinicio del teléfono.
4. **Lienzo Gráfico Cinemático de Partículas:** Motor físico en `Canvas` que simula la red interconectada con cálculo de distancias euclidianas y degradado alfa a 60 FPS sincronizado vía `withFrameNanos`.
5. **Gobernanza y Moderación UGC:** Bloqueo reactivo de usuarios no deseados, reportes de contenido abusivo y aceptación formal de EULA.
6. **Accesibilidad & Personalización:** Selector de fuentes con reescalado dinámico, temas Claro/Oscuro/Sistema y soporte nativo trilingüe (`es`, `en`, `pt`) mediante `LocaleConfig`.

---

## Arquitectura de Software & Patrones de Diseño

La aplicación está estructurada bajo el patrón **Feature-Driven Architecture** con **Flujo Unidireccional de Datos (UDF)**:

```
[UI Component / Composable]
       │ ▲
Event  │ │ State (StateFlow / collectAsStateWithLifecycle)
       ▼ │
[ViewModel (Auth, Nodes, Settings)]
       │ ▲
       │ │ Coroutines / Result Flows
       ▼ │
[Repository Layer (NodeRepository, UserRepository)]
       │ ▲
       ├─► [Local Storage: Preferences DataStore (SettingsManager)]
       └─► [Remote Cloud: Cloud Firestore, Storage, Auth, Messaging]
```

### Componentes de la Arquitectura
* **Unidirectional Data Flow (UDF):** Los eventos generados en los componentes gráficos descienden al `ViewModel`, que actualiza inmutablemente instancias de `StateFlow` expuestas a la UI.
* **Lifecycle-Aware Collection:** La vista recolecta estados mediante `collectAsStateWithLifecycle()`, pausando suscripciones a Firestore automáticamente cuando la aplicación entra en segundo plano para ahorrar datos móviles y batería.
* **Separation of Concerns:** Desacoplamiento estricto entre lógica de presentación Compose, abstracciones de acceso a datos (`Repository`) y servicios de sistema operativo (`AlarmManager`, `BroadcastReceivers`).

---

## Anatomía del Proyecto & Directorios

```text
nodes-android-nativo/
├── build.gradle.kts                           # Configuración Gradle raíz del proyecto Android
├── settings.gradle.kts                        # Inclusión de submódulos y catálogo de dependencias
├── local.properties                           # Parámetros locales (sdk.dir, TERMS_URL, PASSWORD_RESET_URL)
├── gradle/
│   └── libs.versions.toml                     # Catálogo de versiones centralizado (Version Catalog)
└── app/
    ├── build.gradle.kts                       # Configuración de compilación (SDK 36, R8, BoM, plugins)
    ├── proguard-rules.pro                     # Reglas de ofuscación y reglas @Keep de modelos
    └── src/
        ├── androidTest/                       # Pruebas instrumentadas de interfaz en dispositivo
        ├── test/                              # Pruebas unitarias de JVM local
        └── main/
            ├── AndroidManifest.xml            # Declaración de componentes, receptores, servicios y permisos
            ├── res/
            │   ├── values/strings.xml         # Recursos de texto traducibles (español por defecto)
            │   ├── values-en/strings.xml      # Traducciones al inglés
            │   ├── values-pt/strings.xml      # Traducciones al portugués
            │   └── xml/                       # Configuración de backup, data extraction y file_paths
            └── java/com/maxigimenez/nodes/
                ├── app/
                │   ├── MainActivity.kt        # Activity única de la aplicación con inyección de tema
                │   └── NodesApplication.kt    # Application class que monitorea foreground y lifecycle
                ├── core/
                │   ├── navigation/
                │   │   ├── AppNavigation.kt   # Enrutador principal con control de sesión
                │   │   ├── ContentNavigation.kt # Grafo interno autenticado con Scaffold y BottomBar
                │   │   └── Routes.kt          # Definición tipada de rutas y destinos
                │   ├── ui/
                │   │   ├── components/
                │   │   │   └── Components.kt  # Componentes reutilizables, Shimmer y Canvas de partículas
                │   │   └── theme/
                │   │       ├── Animations.kt  # Curvas Bézier y transiciones cinéticas
                │   │       ├── Color.kt       # Paleta cromática Material 3 (Violet/Blue/Sky)
                │   │       ├── Theme.kt       # Controlador de Dynamic Color y esquemas claro/oscuro
                │   │       └── Type.kt        # Jerarquía tipográfica adaptable
                │   └── util/
                │       ├── AuthValidator.kt   # Validaciones de contraseñas y correos
                │       ├── ImageUtils.kt      # Compresión y reescalado de mapas de bits (JPEG)
                │       ├── LocaleHelper.kt    # Manejo dinámico de localización en Android 13+
                │       ├── NotificationHelper.kt # Canales de notificación de audio y alta prioridad
                │       └── ToastHelper.kt     # Utilidades de retroalimentación en pantalla
                ├── data/
                │   ├── local/
                │   │   └── SettingsManager.kt # Persistencia no volátil con DataStore
                │   ├── model/
                │   │   └── Models.kt          # Modelos (@Keep, Node, NodeEntry, ChecklistItem, etc.)
                │   └── repository/
                │       ├── NodeRepository.kt  # Abstracción de acceso a Firestore para Workspaces
                │       └── UserRepository.kt  # Abstracción de acceso para perfiles de usuario
                ├── features/
                │   ├── auth/                  # Pantallas de Login, Registro y ViewModels
                │   ├── home/                  # Dashboard principal y bienvenida
                │   ├── nodes/
                │   │   ├── components/        # Burbujas de chat, modales y visores de imagen
                │   │   ├── detail/            # Configuración, cambio de imagen y lista de miembros
                │   │   ├── entries/           # ChatScreen, AlarmScheduler, ReminderReceiver, BootReceiver
                │   │   └── list/              # Listado de espacios, creación y unión
                │   └── settings/              # Preferencias de usuario, idioma, escala y temas
                └── services/
                    └── FirebaseMessagingService.kt # Manejador de notificaciones push de fondo (FCM)
```

---

## Componentes y Subsistemas Críticos

### 1. Motor de Alarmas de Hardware & Resiliencia de Reinicio
* **Planificación de Alarma Exacta:** Cuando se genera una entrada de tipo `REMINDER`, `AlarmScheduler.schedule` valida el permiso `SCHEDULE_EXACT_ALARM` (o `USE_EXACT_ALARM` en Android 13+).
* **Alarma en Modo Doze:** Dispara mediante `AlarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, triggerAtMillis, pendingIntent)` garantizando que el procesador se despierte incluso en ahorro de energía severo.
* **Restauración en Arranque (`BootReceiver`):** Intercepta `ACTION_BOOT_COMPLETED` y `QUICKBOOT_POWERON`. Inicializa una corrutina en `Dispatchers.IO`, recupera los recordatorios activos pendientes de la colección Firestore del usuario y los vuelve a programar en el sistema operativo automáticamente.

```kotlin
// Reprogramación automática tras reinicio físico en BootReceiver.kt
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val userId = Firebase.auth.currentUser?.uid ?: return
            CoroutineScope(Dispatchers.IO).launch {
                // Recupera entradas REMINDER pendientes de Firestore y reprograma
            }
        }
    }
}
```

### 2. Motor Gráfico Canvas de Nodos (60 FPS)
* Ubicado en `Components.kt`, dibuja partículas flotantes interconectadas.
* **Sincronización:** Emplea `withFrameNanos` en un `LaunchedEffect` desacoplado del árbol principal de recomposiciones, calculando distancias euclidianas $d(p_i, p_j)$ y proyectando aristas cuya opacidad se atenúa con la lejanía.
* **Bypass de Vistas:** Cero instanciación de objetos en tiempo de renderizado; todo ocurre directamente en el búfer de trazado de Compose.

### 3. Recepción Push en Background & Single-Top Deep Linking
* `FirebaseMessagingService` procesa cargas útiles de FCM recibidas en segundo plano.
* Al presionar la notificación, la aplicación es invocada pasando `pendingWorkspaceId` a `AuthViewModel`.
* `ContentNavigation.kt` ejecuta la transición al chat correspondiente aplicando `launchSingleTop = true` y limpiando inmediatamente el identificador para prevenir bucles de recomposición en el backstack.

### 4. Persistencia Segura & DataStore
* `SettingsManager` encapsula `PreferencesDataStore` para almacenar:
  * Tema visual (`Sistema`, `Claro`, `Oscuro`).
  * Escala de fuente (Multiplicador tipográfico relativo).
  * Idioma preferido.
  * Estado de permisos de notificación.

---

## Puesta en Marcha & Compilación

### Requisitos Previos
* **JDK:** Java Development Kit 17 o superior.
* **Android Studio:** Versión Ladybug (2024.2.1) o superior.
* **Android SDK:** Platform 36 instalada, Build-Tools compatibles.
* **Dispositivo / Emulador:** Android 7.0 (API 24) o superior con Google Play Services activos.

---

### Configuración de Credenciales Locales

1. **Firebase Configuration:** Descarga el archivo oficial `google-services.json` desde Firebase Console y ubícalo en:
   ```text
   nodes-android-nativo/app/google-services.json
   ```

2. **Propiedades Locales (`local.properties`):** Asegúrate de que el archivo `local.properties` contenga la ruta a tu SDK y las URLs requeridas por la aplicación:
   ```properties
   sdk.dir=C\:\\Users\\tu_usuario\\AppData\\Local\\Android\\Sdk
   TERMS_URL=https://nodes-web.maxigimenez.com.ar/eula
   PASSWORD_RESET_URL=https://nodes-web.maxigimenez.com.ar/passwordReset
   ```

---

### Comandos de Compilación & Ejecución

```bash
# 1. Navegar al directorio del proyecto Android
cd nodes-android-nativo

# 2. Compilar e instalar APK de desarrollo en dispositivo conectado
./gradlew installDebug

# 3. Ejecutar pruebas unitarias de JVM local
./gradlew testDebugUnitTest

# 4. Generar APK optimizado para release (minificado con R8)
./gradlew assembleRelease

# 5. Generar paquete de distribución para Google Play Store (.aab)
./gradlew bundleRelease
```

---

## Optimización de Producción (R8 / ProGuard)

En `app/build.gradle.kts` se encuentran habilitadas las directivas de optimización:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

Las clases del paquete `com.maxigimenez.nodes.data.model` están marcadas con `@Keep` para evitar que R8 elimine constructores sin argumentos requeridos por la deserialización reflexiva de Cloud Firestore.
