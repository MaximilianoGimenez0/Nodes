# Plan Maestro de Implementación: Refactorización Arquitectónica
**Proyecto:** `nodes-android-nativo`  
**Directriz Semántica:** Estandarización estricta bajo la nomenclatura **`nodes`** (descartando `workspaces`).  
**Estándar:** Modern Android Development (MAD), Jetpack Compose, Clean Architecture & Google Architecture Guidelines.

---

## 1. Contexto y Principios Rectores

Este documento establece la hoja de ruta técnica paso a paso para transformar la estructura de `nodes-android-nativo` desde su estado actual hacia una arquitectura desacoplada, escalable y modularizable.

### Principios de Arquitectura
1. **Compilación Continua (Zero-Breaking Steps):** Al finalizar cada fase, el proyecto debe mantenerse 100% funcional y compilar sin errores ejecutando `./gradlew assembleDebug`.
2. **Dirección Unidireccional de Dependencias:**
   $$\text{Presentation (UI)} \longrightarrow \text{Domain} \longleftarrow \text{Data}$$
   $$\text{Features} \longrightarrow \text{Core} \quad (\text{Core jamás importa nada de Features ni de App})$$
3. **Estandarización del Lenguaje Ubicuo (`nodes`):**
   * El concepto central del dominio se nombra **`Node`** (singular) y **`Nodes`** (plural).
   * La colección de Firestore puede mantenerse como `"workspaces"` internamente en la capa de datos si el backend así lo requiere, pero el dominio, la presentación, la navegación y los nombres de archivos utilizarán exclusivamente la terminología `Node` / `Nodes`.
4. **Segregación de Responsabilidades:**
   * Eliminación de archivos "cajón de sastre" (`Components.kt`, `Models.kt`).
   * Eliminación del "God ViewModel" (`NodesViewModel.kt` de 758 líneas) dividiéndolo por responsabilidades.
   * Extracción de componentes de sistema operativo (`BroadcastReceivers`, `Services`) fuera de los paquetes de interfaz de usuario.

---

## 2. Fase 0: Preparación y Línea Base (Baseline)

### Objetivo
Establecer un entorno de control de versiones seguro y verificar la integridad de compilación antes de iniciar cualquier modificación estructural.

### Tareas
1. **Creación de Rama Git:**
   ```powershell
   git checkout -b refactor/mad-architecture-nodes
   ```
2. **Verificación de Compilación Limpia:**
   ```powershell
   ./gradlew clean assembleDebug
   ```
3. **Mapeo de Rutas de Navegación Activas:**
   Registrar las rutas actuales (`login`, `signup`, `home`, `workspaces_graph`, `chat/{id}`, `info/{id}`, `settings`) para asegurar continuidad durante las refactorizaciones.

---

## 3. Fase 1: Desacoplamiento de Core y Eliminación de "Cajones de Sastre"

### Objetivo
Romper la fuga de abstracciones y dependencias circulares (donde `core` importa clases de `features/auth`), fragmentar los archivos monolíticos `Models.kt` y `Components.kt`, y limpiar la capa de utilidades.

### Tareas Paso a Paso

#### 1.1. Desmantelamiento de `data/model/Models.kt`
* Crear el paquete `domain/model/` para alojar los modelos puros de negocio.
* Extraer cada modelo a su propio archivo independiente:
  * `domain/model/Node.kt`: Entidad principal del nodo.
  * `domain/model/NodeEntry.kt`: Entidad de cada entrada/mensaje del nodo.
  * `domain/model/ChecklistItem.kt`: Ítems de tareas/checklists.
  * `domain/model/EntryType.kt`: Enum con los tipos de entrada (`MESSAGE`, `CHECK_LIST`, `IMAGE`, `REMINDER`, `NONE`).
  * `domain/model/UserProfile.kt`: Perfil del usuario.
  * `domain/model/AppSettings.kt`: Preferencias de la aplicación.
* **Mover UI State:** Extraer `NodesUiState` fuera de `data` y ubicarlo en `features/nodes/presentation/list/NodesUiState.kt`.
* **Eliminar:** `data/model/Models.kt`.

#### 1.2. Ruptura de Dependencia Circular `Core` $\rightarrow$ `Auth`
* Localizar la clase `ParticleNode` declarada dentro de `features/auth/LoginPage.kt`.
* Moverla a `core/designsystem/components/NodesParticleCanvas.kt` junto con el composable `NodesBackground`.
* Corregir las importaciones en `LoginPage.kt` y `HomePage.kt` para que consuman `NodesParticleCanvas` desde `core`.

#### 1.3. Fragmentación de `core/ui/components/Components.kt`
* Extraer componentes a `core/designsystem/components/`:
  * `ShimmerImageLoader.kt`
  * `CharacterLimitedTextField.kt`
  * `NodesParticleCanvas.kt` (reuniendo `NodesBackground` y `ParticleNode`)
* Extraer componentes específicos a sus respectivas features:
  * `CreateChecklistDialog` $\rightarrow$ `features/nodes/presentation/chat/components/dialogs/CreateChecklistDialog.kt`
  * `CategoryFilterRow` $\rightarrow$ `features/nodes/presentation/list/components/CategoryFilterRow.kt`
  * `CustomLoginField` $\rightarrow$ `features/auth/presentation/login/components/AuthTextField.kt`
* Extraer navegación:
  * `MainBottomBar` $\rightarrow$ `core/navigation/components/MainBottomBar.kt`
* **Eliminar:** `core/ui/components/Components.kt`.

#### 1.4. Saneamiento de `core/util/`
* Mover `AuthValidator.kt` a `features/auth/domain/validation/AuthValidator.kt`.
* En `ImageUtils.kt`:
  * Extraer la función suspendida `uploadImage` (que invoca `FirebaseStorage.getInstance()`) y crear `data/remote/storage/FirebaseStorageDataSource.kt`.
  * Renombrar `ImageUtils.kt` a `core/util/ImageCompressor.kt`, limitándolo exclusivamente al manejo de bitmaps en memoria, orientación EXIF y compresión.

### Tabla de Mapeo de Archivos - Fase 1

| Archivo Origen | Archivo Destino | Acción | Responsabilidad Asignada |
| :--- | :--- | :--- | :--- |
| `data/model/Models.kt` | `domain/model/Node.kt` | **Crear / Dividir** | Entidad de dominio Node |
| `data/model/Models.kt` | `domain/model/NodeEntry.kt` | **Crear / Dividir** | Entidad de dominio NodeEntry |
| `data/model/Models.kt` | `domain/model/ChecklistItem.kt` | **Crear / Dividir** | Sub-entidad ChecklistItem |
| `data/model/Models.kt` | `domain/model/EntryType.kt` | **Crear / Dividir** | Enum de tipos de entrada |
| `data/model/Models.kt` | `domain/model/UserProfile.kt` | **Crear / Dividir** | Entidad de dominio UserProfile |
| `data/model/Models.kt` | `domain/model/AppSettings.kt` | **Crear / Dividir** | Entidad de dominio AppSettings |
| `data/model/Models.kt` | `features/nodes/presentation/list/NodesUiState.kt` | **Mover / Aislar** | Estado inmutable de UI de lista de nodos |
| `data/model/Models.kt` | *N/A* | **Eliminar** | Archivo monolítico descartado |
| `features/auth/LoginPage.kt` (`class ParticleNode`) | `core/designsystem/components/NodesParticleCanvas.kt` | **Mover** | Modelo de partícula visual en Core |
| `core/ui/components/Components.kt` (`NodesBackground`) | `core/designsystem/components/NodesParticleCanvas.kt` | **Mover** | Canvas de animación de nodos |
| `core/ui/components/Components.kt` (`ShimmerImageLoader`) | `core/designsystem/components/ShimmerImageLoader.kt` | **Mover** | Átomo de UI de carga |
| `core/ui/components/Components.kt` (`CharacterLimited...`) | `core/designsystem/components/CharacterLimitedTextField.kt` | **Mover** | Molécula de UI para formularios |
| `core/ui/components/Components.kt` (`CreateChecklistDialog`) | `features/nodes/presentation/chat/components/dialogs/CreateChecklistDialog.kt` | **Mover** | Diálogo propio del chat de nodos |
| `core/ui/components/Components.kt` (`CategoryFilterRow`) | `features/nodes/presentation/list/components/CategoryFilterRow.kt` | **Mover** | Filtro específico de la lista de nodos |
| `core/ui/components/Components.kt` (`CustomLoginField`) | `features/auth/presentation/login/components/AuthTextField.kt` | **Mover** | Campo específico de autenticación |
| `core/ui/components/Components.kt` (`MainBottomBar`) | `core/navigation/components/MainBottomBar.kt` | **Mover** | Barra de navegación desacoplada |
| `core/ui/components/Components.kt` | *N/A* | **Eliminar** | Archivo cajón de sastre descartado |
| `core/util/AuthValidator.kt` | `features/auth/domain/validation/AuthValidator.kt` | **Mover** | Lógica de validación de negocio en Auth |
| `core/util/ImageUtils.kt` (`uploadImage`) | `data/remote/storage/FirebaseStorageDataSource.kt` | **Extraer** | Infraestructura de almacenamiento remoto |
| `core/util/ImageUtils.kt` | `core/util/ImageCompressor.kt` | **Renombrar** | Utilidad gráfica pura sin llamadas de red |

### Criterio de Aceptación y Verificación - Fase 1
* Compilación limpia mediante `./gradlew assembleDebug`.
* Ningún archivo bajo el paquete `com.maxigimenez.nodes.core` contiene importaciones hacia `com.maxigimenez.nodes.features.*`.

---

## 4. Fase 2: Extracción de Componentes de Sistema y Estandarización de Pantallas

### Objetivo
Desacoplar los componentes Android del sistema (`BroadcastReceiver`, `Service`, `AlarmManager`) de los composables de interfaz de usuario y unificar la nomenclatura de vistas bajo el estándar `Screen`.

### Tareas Paso a Paso

#### 2.1. Reubicación de Componentes de Sistema Operativo
* Crear el paquete `system/receivers/`:
  * Mover `features/nodes/entries/BootReceiver.kt` $\rightarrow$ `system/receivers/BootReceiver.kt`.
  * Extraer `ReminderReceiver` desde `features/nodes/entries/NodeReminders.kt` hacia `system/receivers/ReminderReceiver.kt`.
* Crear el paquete `core/notifications/scheduler/`:
  * Extraer `AlarmScheduler` desde `features/nodes/entries/NodeReminders.kt` hacia `core/notifications/scheduler/AlarmScheduler.kt`.
  * **Eliminar** el archivo residual `features/nodes/entries/NodeReminders.kt`.
* Crear el paquete `system/services/`:
  * Renombrar y mover `services/FirebaseMessagingService.kt` $\rightarrow$ `system/services/NodesFirebaseMessagingService.kt` para erradicar el *shadowing* sobre el SDK de Firebase.
  * Eliminar el directorio `services/`.

#### 2.2. Actualización en `AndroidManifest.xml`
Reemplazar las declaraciones antiguas por las rutas calificadas:
```xml
<!-- System Receivers -->
<receiver
    android:name=".system.receivers.BootReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
        <action android:name="android.intent.action.QUICKBOOT_POWERON" />
    </intent-filter>
</receiver>

<receiver
    android:name=".system.receivers.ReminderReceiver"
    android:enabled="true"
    android:exported="false" />

<!-- Push Notifications Service -->
<service
    android:name=".system.services.NodesFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

#### 2.3. Estandarización de Pantallas (`Page` $\rightarrow$ `Screen`) y Supresión de Wrappers
* `features/auth/LoginPage.kt` $\rightarrow$ renombrar a `features/auth/presentation/login/LoginScreen.kt`.
* `features/auth/SignUpPage.kt` $\rightarrow$ renombrar a `features/auth/presentation/signup/SignUpScreen.kt`.
* `features/home/HomePage.kt` $\rightarrow$ renombrar a `features/home/presentation/HomeScreen.kt`.
* `features/settings/SettingsPage.kt` $\rightarrow$ renombrar a `features/settings/presentation/SettingsScreen.kt`.
* **Eliminar el wrapper innecesario:** Suprimir `features/nodes/list/NodesPage.kt` (41 líneas redundantes). El punto de entrada directo pasa a ser `features/nodes/presentation/list/NodesScreen.kt`.

### Tabla de Mapeo de Archivos - Fase 2

| Archivo Origen | Archivo Destino | Acción | Responsabilidad Asignada |
| :--- | :--- | :--- | :--- |
| `features/nodes/entries/BootReceiver.kt` | `system/receivers/BootReceiver.kt` | **Mover** | Receptor de reinicio del dispositivo |
| `features/nodes/entries/NodeReminders.kt` (`ReminderReceiver`) | `system/receivers/ReminderReceiver.kt` | **Extraer** | Receptor de disparo de alarmas/recordatorios |
| `features/nodes/entries/NodeReminders.kt` (`AlarmScheduler`) | `core/notifications/scheduler/AlarmScheduler.kt` | **Extraer** | Gestor de programación en AlarmManager |
| `features/nodes/entries/NodeReminders.kt` | *N/A* | **Eliminar** | Archivo mixto suprimido |
| `services/FirebaseMessagingService.kt` | `system/services/NodesFirebaseMessagingService.kt` | **Renombrar / Mover** | Servicio FCM sin colisión de nombres |
| `features/nodes/list/NodesPage.kt` | *N/A* | **Eliminar** | Wrapper redundante eliminado |
| `features/nodes/list/NodesScreen.kt` | `features/nodes/presentation/list/NodesScreen.kt` | **Mover** | Pantalla principal de lista de nodos |
| `features/auth/LoginPage.kt` | `features/auth/presentation/login/LoginScreen.kt` | **Renombrar / Mover** | Pantalla de inicio de sesión |
| `features/auth/SignUpPage.kt` | `features/auth/presentation/signup/SignUpScreen.kt` | **Renombrar / Mover** | Pantalla de registro |
| `features/home/HomePage.kt` | `features/home/presentation/HomeScreen.kt` | **Renombrar / Mover** | Pantalla Home unificada |
| `features/settings/SettingsPage.kt` | `features/settings/presentation/SettingsScreen.kt` | **Renombrar / Mover** | Pantalla de ajustes de usuario |

### Criterio de Aceptación y Verificación - Fase 2
* Compilación limpia mediante `./gradlew assembleDebug`.
* El `AndroidManifest.xml` resuelve los nombres de clase completos sin errores de lint.
* La navegación accede directamente a `NodesScreen` sin wrappers intermediarios.

---

## 5. Fase 3: Reestructuración de la Feature `nodes` y Descomposición del "God ViewModel"

### Objetivo
Dividir el monolítico `NodesViewModel` (758 líneas), fragmentar `NodeDetail.kt` (791 líneas) y `NodeBubbles.kt` (727 líneas), estructurando la feature en submódulos cohesivos: `list`, `chat`, `detail` y `components`.

### Tareas Paso a Paso

#### 3.1. Reorganización de Submódulos y Pantallas
* **Eliminar `features/nodes/entries/`:** Este paquete ambiguo se elimina definitivamente.
* **Separar `features/nodes/detail/NodeDetail.kt` (791 líneas):**
  * Extraer la pantalla de información, integrantes y edición del nodo a `features/nodes/presentation/detail/NodeDetailScreen.kt`.
  * La pantalla de chat (actualmente `NodeChatScreen.kt` en `entries`) se formaliza en:
    `features/nodes/presentation/chat/NodeChatScreen.kt`.
* **Fragmentar `features/nodes/components/NodeBubbles.kt` (727 líneas) en burbujas atómicas:**
  * `features/nodes/presentation/chat/components/bubbles/MessageBubble.kt`
  * `features/nodes/presentation/chat/components/bubbles/ImageBubble.kt`
  * `features/nodes/presentation/chat/components/bubbles/ChecklistBubble.kt`
  * `features/nodes/presentation/chat/components/bubbles/ReminderBubble.kt`
  * `features/nodes/presentation/chat/components/bubbles/BaseNodeBubble.kt` (estilos comunes)
  * Eliminar `NodeBubbles.kt`.
* **Reorganizar Controles y Diálogos:**
  * `NodeControls.kt` $\rightarrow$ `features/nodes/presentation/chat/components/controls/NodeFabMenu.kt`.
  * `NodeDialogs.kt` $\rightarrow$ dividir en:
    * `features/nodes/presentation/list/components/CreateNodeDialog.kt`
    * `features/nodes/presentation/chat/components/dialogs/CreateReminderDialog.kt`
  * `FullScreenImageViewer.kt` $\rightarrow$ `features/nodes/presentation/chat/components/FullScreenImageViewer.kt`.
  * `NodeListComponents.kt` $\rightarrow `features/nodes/presentation/list/components/NodeCard.kt`.

#### 3.2. Descomposición del God ViewModel (`NodesViewModel` - 758 líneas)
Se divide en tres ViewModels especializados:
1. **`NodesViewModel` (en `features/nodes/presentation/list/`):**
   * *Responsabilidad:* Carga y observación de la lista de nodos, creación de nodos y unirse a nodos existentes.
   * *Estado:* `NodesUiState` (lista, loading, error, acción pendiente).
2. **`NodeChatViewModel` (en `features/nodes/presentation/chat/`):**
   * *Responsabilidad:* Escuchar en tiempo real las entradas (`NodeEntry`) del nodo activo (`nodeId`), envío de mensajes de texto, subida de imágenes, alternar checklist y recordatorios.
   * *Estado:* `NodeChatUiState` (lista de entries, subiendo imagen, error, id del nodo activo).
3. **`NodeDetailViewModel` (en `features/nodes/presentation/detail/`):**
   * *Responsabilidad:* Carga de metadatos del nodo, listado de miembros (`UserProfile`), actualización del nombre, cambio de imagen y permisos.
   * *Estado:* `NodeDetailUiState` (nodo actual, miembros, permiso de edición, loading).

### Tabla de Mapeo de Archivos - Fase 3

| Archivo Origen | Archivo Destino | Acción | Responsabilidad Asignada |
| :--- | :--- | :--- | :--- |
| `features/nodes/list/NodesViewModel.kt` | `features/nodes/presentation/list/NodesViewModel.kt` | **Refactorizar** | ViewModel acotado al listado de nodos |
| `features/nodes/list/NodesViewModel.kt` | `features/nodes/presentation/chat/NodeChatViewModel.kt` | **Crear / Extraer** | ViewModel exclusivo del chat y entries |
| `features/nodes/list/NodesViewModel.kt` | `features/nodes/presentation/detail/NodeDetailViewModel.kt` | **Crear / Extraer** | ViewModel de detalle y miembros del nodo |
| *Nuevo* | `features/nodes/presentation/chat/NodeChatUiState.kt` | **Crear** | Estado inmutable de la pantalla de chat |
| *Nuevo* | `features/nodes/presentation/detail/NodeDetailUiState.kt` | **Crear** | Estado inmutable de la pantalla de detalle |
| `features/nodes/entries/NodeChatScreen.kt` | `features/nodes/presentation/chat/NodeChatScreen.kt` | **Mover** | Pantalla principal de chat del nodo |
| `features/nodes/detail/NodeDetail.kt` | `features/nodes/presentation/detail/NodeDetailScreen.kt` | **Reestructurar** | Pantalla de ajustes y miembros del nodo |
| `features/nodes/components/NodeBubbles.kt` | `features/nodes/presentation/chat/components/bubbles/*` | **Dividir** | Burbujas de chat segregadas por tipo |
| `features/nodes/components/NodeControls.kt` | `features/nodes/presentation/chat/components/controls/NodeFabMenu.kt` | **Mover / Renombrar** | Menú FAB flotante del nodo |
| `features/nodes/components/FullScreenImageViewer.kt` | `features/nodes/presentation/chat/components/FullScreenImageViewer.kt` | **Mover** | Visor de imagen en pantalla completa |
| `features/nodes/components/NodeListComponents.kt` | `features/nodes/presentation/list/components/NodeCard.kt` | **Mover / Renombrar** | Tarjeta de visualización en la lista |
| `features/nodes/components/NodeDialogs.kt` | `features/nodes/presentation/list/components/CreateNodeDialog.kt` | **Dividir** | Diálogo de creación de nodo |
| `features/nodes/components/NodeDialogs.kt` | `features/nodes/presentation/chat/components/dialogs/CreateReminderDialog.kt` | **Dividir** | Diálogo de recordatorio |

### Criterio de Aceptación y Verificación - Fase 3
* Compilación limpia mediante `./gradlew assembleDebug`.
* `NodesViewModel` no supera las 200 líneas de código y delega chat y detalles a sus ViewModels correspondientes.
* El directorio `features/nodes/entries/` ha sido suprimido.

---

## 6. Fase 4: Abstracción de Datos, Desacoplamiento de Firebase y UDF

### Objetivo
Ocultar las llamadas directas al SDK de Firebase detrás de interfaces de repositorio en `domain`, unificar la reactividad a `StateFlow` y erradicar fugas de contexto en ViewModels.

### Tareas Paso a Paso

#### 4.1. Definición de Contratos en `domain/repository/`
* `domain/repository/NodeRepository.kt`: Interfaz que define operaciones de nodos (`getNodesStream`, `getNodeByIdStream`, `getEntriesStream`, `createNode`, `sendEntry`, `updateNodeName`, `updateNodePicture`).
* `domain/repository/UserRepository.kt`: Interfaz para perfiles (`getUserProfile`, `updateUserInfo`, `updateUserPicture`).
* `domain/repository/SettingsRepository.kt`: Interfaz para ajustes locales de la app.
* `domain/repository/AuthRepository.kt`: Interfaz para login, logout, registro y estado de sesión.

#### 4.2. Implementación de Repositorios en `data/repository/`
* `data/repository/NodeRepositoryImpl.kt`: Concentra las consultas a Firestore (`FirebaseFirestore`) y llamadas a Storage (`FirebaseStorageDataSource`), extrayéndolas por completo de los ViewModels.
* `data/repository/UserRepositoryImpl.kt`: Implementa `UserRepository`.
* `data/repository/SettingsRepositoryImpl.kt`: Consume `SettingsDataStoreSource` para lectura/escritura en DataStore.
* `data/repository/AuthRepositoryImpl.kt`: Encapsula `FirebaseAuth` y `FirebaseFunctions`.

#### 4.3. Formalización de Persistencia Local
* Renombrar `data/local/SettingsManager.kt` a `data/local/datastore/SettingsDataStoreSource.kt`.
* Inyectar el acceso a `DataStore<Preferences>` directamente en `SettingsRepositoryImpl`.

#### 4.4. Modernización Reactiva en `features/auth/AuthViewModel.kt`
* Eliminar `MutableLiveData` y `LiveData` (`_authState`, `_userId`).
* Definir estados inmutables mediante `StateFlow`:
  ```kotlin
  private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
  val authState: StateFlow<AuthState> = _authState.asStateFlow()
  ```
* En los composables (`LoginScreen`, `SignUpScreen`, `AppNavigation`), sustituir `observeAsState()` por `collectAsStateWithLifecycle()`.

#### 4.5. Eliminación de Context Leaks en ViewModels
* Convertir los ViewModels que heredan de `AndroidViewModel(application)` a `ViewModel()` puro de AndroidX.
* Extraer cadenas de texto (`context.getString(...)`) fuera de los ViewModels hacia la capa de presentación Compose o canalizarlas mediante identificadores `@StringRes Int`.
* Eliminar todas las anotaciones `@SuppressLint("StaticFieldLeak")`.

### Tabla de Mapeo de Archivos - Fase 4

| Archivo Origen | Archivo Destino | Acción | Responsabilidad Asignada |
| :--- | :--- | :--- | :--- |
| *Nuevo* | `domain/repository/NodeRepository.kt` | **Crear** | Contrato de operaciones sobre Nodos |
| *Nuevo* | `domain/repository/UserRepository.kt` | **Crear** | Contrato de operaciones de usuario |
| *Nuevo* | `domain/repository/SettingsRepository.kt` | **Crear** | Contrato de ajustes locales |
| *Nuevo* | `domain/repository/AuthRepository.kt` | **Crear** | Contrato de autenticación |
| `data/repository/NodeRepository.kt` | `data/repository/NodeRepositoryImpl.kt` | **Mover / Completar** | Implementación completa de operaciones Firestore para Nodos |
| `data/repository/UserRepository.kt` | `data/repository/UserRepositoryImpl.kt` | **Mover / Ajustar** | Implementación de repositorio de usuarios |
| `data/local/SettingsManager.kt` | `data/local/datastore/SettingsDataStoreSource.kt` | **Mover / Renombrar** | Fuente de datos local DataStore |
| *Nuevo* | `data/repository/SettingsRepositoryImpl.kt` | **Crear** | Implementación del repositorio de preferencias |
| `features/auth/UserViewModel.kt` | `features/settings/presentation/ProfileViewModel.kt` | **Mover / Renombrar** | ViewModel dedicado al perfil de usuario en Settings |

### Criterio de Aceptación y Verificación - Fase 4
* Compilación limpia mediante `./gradlew assembleDebug`.
* Ningún ViewModel importa clases directas de `com.google.firebase.*`.
* No quedan referencias a `LiveData` en todo el proyecto.
* Ningún ViewModel retiene referencias al `Context` ni utiliza `@SuppressLint("StaticFieldLeak")`.

---

## 7. Estructura de Directorios Final Completa (`nodes`)

```text
com.maxigimenez.nodes/
│
├── app/
│   ├── MainActivity.kt
│   └── NodesApplication.kt
│
├── core/
│   ├── common/
│   │   ├── extensions/
│   │   │   └── ModifierExtensions.kt          # entranceAnimation(), pressClickEffect()
│   │   └── result/
│   │       └── AppResult.kt                   # Sealed class Success / Error
│   │
│   ├── designsystem/
│   │   ├── theme/
│   │   │   ├── Color.kt
│   │   │   ├── Type.kt
│   │   │   ├── Theme.kt
│   │   │   └── Motion.kt                      # NodesAnimations
│   │   └── components/
│   │       ├── ShimmerImageLoader.kt
│   │       ├── CharacterLimitedTextField.kt
│   │       └── NodesParticleCanvas.kt         # ParticleNode + NodesBackground
│   │
│   ├── navigation/
│   │   ├── AppNavigation.kt                   # NavHost principal
│   │   ├── MainNavigation.kt                  # NavHost con Scaffold y BottomBar
│   │   ├── Routes.kt                          # Rutas y contratos de navegación
│   │   └── components/
│   │       └── MainBottomBar.kt
│   │
│   ├── notifications/
│   │   ├── NotificationHelper.kt
│   │   └── scheduler/
│   │       └── AlarmScheduler.kt              # Extraído de NodeReminders
│   │
│   └── util/
│       ├── ImageCompressor.kt                 # Exclusivo para compresión y EXIF
│       ├── LocaleHelper.kt
│       └── ToastHelper.kt
│
├── data/
│   ├── local/
│   │   └── datastore/
│   │       └── SettingsDataStoreSource.kt     # Antiguo SettingsManager
│   ├── remote/
│   │   └── storage/
│   │       └── FirebaseStorageDataSource.kt    # I/O en Firebase Storage
│   └── repository/
│       ├── AuthRepositoryImpl.kt
│       ├── NodeRepositoryImpl.kt
│       ├── SettingsRepositoryImpl.kt
│       └── UserRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── AppSettings.kt
│   │   ├── ChecklistItem.kt
│   │   ├── EntryType.kt
│   │   ├── Node.kt                            # Entidad central Node
│   │   ├── NodeEntry.kt                       # Entidad de entradas del Node
│   │   └── UserProfile.kt
│   └── repository/
│       ├── AuthRepository.kt
│       ├── NodeRepository.kt                  # Contrato central
│       ├── SettingsRepository.kt
│       └── UserRepository.kt
│
├── features/
│   │
│   ├── auth/
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   └── AuthState.kt
│   │   │   └── validation/
│   │   │       └── AuthValidator.kt
│   │   └── presentation/
│   │       ├── login/
│   │       │   ├── LoginScreen.kt             # Antiguo LoginPage
│   │       │   ├── LoginUiState.kt
│   │       │   └── components/
│   │       │       └── AuthTextField.kt       # Antiguo CustomLoginField
│   │       ├── signup/
│   │       │   ├── SignUpScreen.kt            # Antiguo SignUpPage
│   │       │   └── SignUpUiState.kt
│   │       └── AuthViewModel.kt               # 100% StateFlow
│   │
│   ├── home/
│   │   └── presentation/
│   │       ├── HomeScreen.kt                  # Antiguo HomePage
│   │       ├── HomeUiState.kt
│   │       └── HomeViewModel.kt
│   │
│   ├── nodes/                                 # Feature central Nodes
│   │   └── presentation/
│   │       ├── list/
│   │       │   ├── NodesScreen.kt             # Antiguo NodesScreen (sin NodesPage wrapper)
│   │       │   ├── NodesUiState.kt            # Extraído de Models.kt
│   │       │   ├── NodesViewModel.kt          # ViewModel exclusivo de listado
│   │       │   └── components/
│   │       │       ├── CategoryFilterRow.kt   # Extraído de Components.kt
│   │       │       ├── CreateNodeDialog.kt    # Extraído de NodeDialogs.kt
│   │       │       └── NodeCard.kt            # Antiguo NodeListComponents.kt
│   │       │
│   │       ├── chat/
│   │       │   ├── NodeChatScreen.kt          # Antiguo NodeChatScreen
│   │       │   ├── NodeChatUiState.kt
│   │       │   ├── NodeChatViewModel.kt       # Separado de NodesViewModel
│   │       │   └── components/
│   │       │       ├── bubbles/
│   │       │       │   ├── BaseNodeBubble.kt
│   │       │       │   ├── ChecklistBubble.kt
│   │       │       │   ├── ImageBubble.kt
│   │       │       │   ├── MessageBubble.kt
│   │       │       │   └── ReminderBubble.kt
│   │       │       ├── controls/
│   │       │       │   └── NodeFabMenu.kt     # Antiguo NodeControls.kt
│   │       │       ├── dialogs/
│   │       │       │   ├── CreateChecklistDialog.kt # Extraído de Components.kt
│   │       │       │   └── CreateReminderDialog.kt
│   │       │       └── FullScreenImageViewer.kt
│   │       │
│   │       └── detail/
│   │           ├── NodeDetailScreen.kt        # Extraído de NodeDetail.kt
│   │           ├── NodeDetailUiState.kt
│   │           └── NodeDetailViewModel.kt     # Separado de NodesViewModel
│   │
│   └── settings/
│       └── presentation/
│           ├── SettingsScreen.kt              # Antiguo SettingsPage
│           ├── SettingsUiState.kt
│           ├── SettingsViewModel.kt
│           └── ProfileViewModel.kt            # Antiguo UserViewModel
│
└── system/                                    # Integración con el Sistema Android
    ├── receivers/
    │   ├── BootReceiver.kt                    # Extraído de features/nodes/entries
    │   └── ReminderReceiver.kt                # Extraído de NodeReminders
    └── services/
        └── NodesFirebaseMessagingService.kt   # Renombrado para evitar shadowing del SDK
```

---

## 8. Matriz de Riesgos y Mitigación

| Riesgo Potencial | Causa Raíz | Estrategia de Mitigación en el Plan |
| :--- | :--- | :--- |
| **`ClassNotFoundException` en arranque o eventos** | Mover `BootReceiver`, `ReminderReceiver` o `FirebaseMessagingService` sin actualizar el Manifest. | La Fase 2 incluye explícitamente el bloque exacto del `AndroidManifest.xml` con los paquetes calificados. |
| **Pantalla en blanco o ciclo de recomposición infinito** | Rutas de navegación con IDs antiguos o strings en duro (`"workspaces"` vs `"nodes"`). | Se unifica `Routes.kt` en la Fase 2 y se actualizan los `NavHost` de inmediato. |
| **Pérdida de datos en Firestore** | Confusión entre el modelo `Node` y el nombre de la colección en la base de datos. | En `NodeRepositoryImpl`, la colección se mantiene explícitamente como `db.collection("workspaces")` mientras que toda la lógica de Kotlin habla el lenguaje `Node`. |
| **Fallas en previews de Jetpack Compose** | Componentes de `core` que dependían de ViewModels o contextos específicos. | Todos los componentes atómicos (`designsystem/components/`) quedan puramente stateless, dependiendo únicamente de datos primitivos o lambdas. |
