# 📱 NODES Android Nativo — Mobile Client & Hardware Services

> Aplicación móvil nativa desarrollada con **Kotlin 2.2** y **Jetpack Compose** (BOM 2024.09.00) para **Android SDK 36**. Cuenta con arquitectura unidireccional reactiva (UDF), sincronización en tiempo real con Cloud Firestore, guardado de preferencias con Preferences DataStore, recordatorios con alarmas exactas del sistema, restauración automática al reiniciar el teléfono y notificaciones push con apertura directa de salas.

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

**nodes-android-nativo** es la aplicación cliente principal del ecosistema NODES. Proporciona una experiencia móvil táctil responsive.

### Capacidades Centrales
1. **Creación y Unión a Nodos:** Podés crear tus propios espacios de trabajo colaborativos o unirte a nodos existentes de forma sencilla mediante un código único de 6 caracteres.
2. **Entradas y Contenido:** Creá y compartí distintos tipos de entradas según lo que necesites: texto, imágenes, to-do lists y reminders.
3. **Notificaciones Push y Mensajería:** Mensajería con notificaciones push para mantenerte al día con las conversaciones y novedades en cada nodo.
4. **Recordatorios y Alarmas Confiables:** Notificaciones programadas que suenan a la hora exacta, incluso con el teléfono en reposo o ahorro de energía, y se restablecen automáticamente si se reinicia el dispositivo.
5. **Accesibilidad y Personalización:** Opciones de accesibilidad para ajustar el tamaño de texto, soporte para tema claro y tema oscuro (o sincronizado con el sistema) y disponibilidad en varios idiomas.
6. **Cumplimiento con Directivas de Google Play:** Funciones de seguridad y moderación que incluyen bloqueo de usuarios, reporte de contenido abusivo y aceptación de términos de uso para contenido generado por la comunidad.

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
* **Flujo Unidireccional de Datos (UDF):** Las interacciones del usuario en la pantalla se envían al `ViewModel`, que actualiza el estado (`StateFlow`) para que la interfaz se redibuje de forma ordenada y predecible.
* **Consumo Consciente del Ciclo de Vida:** La interfaz observa los estados con `collectAsStateWithLifecycle()`, pausando las lecturas de Firestore cuando la app pasa a segundo plano para ahorrar datos y batería.
* **Separación de Responsabilidades:** Organización modular entre las pantallas en Jetpack Compose, los repositorios de datos (`Repository`) y los servicios del sistema Android (alarmas y notificaciones).

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

## Componentes y Funcionalidades Destacadas

### 1. Sistema de Recordatorios y Restauración tras Reinicio
* **Alarmas Exactas:** Al crear un recordatorio, se programa una alarma precisa en el sistema Android validando los permisos correspondientes (`SCHEDULE_EXACT_ALARM` o `USE_EXACT_ALARM`).
* **Funcionamiento en Ahorro de Batería:** Se utiliza `setExactAndAllowWhileIdle` en modo `RTC_WAKEUP`, asegurando que la alarma suene puntualmente aunque el teléfono esté en reposo (Doze Mode).
* **Restauración al Reiniciar (`BootReceiver`):** Si el dispositivo se reinicia, el receptor `BootReceiver` detecta el encendido, consulta en segundo plano los recordatorios pendientes en Firestore y vuelve a programar las alarmas automáticamente.

```kotlin
// Reprogramación automática tras reinicio en BootReceiver.kt
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val userId = Firebase.auth.currentUser?.uid ?: return
            CoroutineScope(Dispatchers.IO).launch {
                // Recupera recordatorios pendientes de Firestore y los reprograma
            }
        }
    }
}
```

### 2. Fondo Dinámico con Partículas en Canvas
* Ubicado en `Components.kt`, genera un efecto visual interactivo de nodos interconectados.
* **Animación Fluida:** Se sincroniza cuadro a cuadro a 60 FPS mediante `withFrameNanos`, calculando la cercanía entre puntos para dibujar líneas que se desvanecen con la distancia.
* **Rendimiento:** Dibuja directamente sobre el lienzo de Compose sin crear objetos innecesarios en memoria durante la animación.

### 3. Notificaciones Push y Navegación Directa al Chat
* `FirebaseMessagingService` recibe y procesa las notificaciones de Firebase en segundo plano.
* Al tocar la notificación, la app se abre y envía el identificador de la sala (`pendingWorkspaceId`) a la pantalla correspondiente.
* La navegación hacia el espacio se realiza con `launchSingleTop = true`, evitando duplicar pantallas en el historial.

### 4. Ajustes y Preferencias Locales (DataStore)
* `SettingsManager` utiliza `PreferencesDataStore` para guardar la configuración del usuario en el dispositivo:
  * Tema visual (Claro, Oscuro o seguir al Sistema).
  * Tamaño de texto ajustable.
  * Idioma de la aplicación (español, inglés o portugués).
  * Estado del permiso de notificaciones.

---

## Puesta en Marcha & Compilación

### Requisitos Previos
* **JDK:** Java Development Kit 17 o superior.
* **Android Studio:** Versión Ladybug (2024.2.1) o superior.
* **Android SDK:** Platform 36 instalada, Build-Tools compatibles.
* **Dispositivo / Emulador:** Android 7.0 (API 24) o superior con Google Play Services activos.

---

### Configuración de Credenciales Locales

1. **Firebase Configuration:** Descargá el archivo oficial `google-services.json` desde Firebase Console y ubicalo en:
   ```text
   nodes-android-nativo/app/google-services.json
   ```

2. **Propiedades Locales (`local.properties`):** Asegurate de que el archivo `local.properties` contenga la ruta a tu SDK y las URLs requeridas por la aplicación:
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
