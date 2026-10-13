# ⚡ NODES Functions — Serverless Backend & Cloud Triggers

> Núcleo serverless de procesamiento en segundo plano construido con **Firebase Cloud Functions v2** sobre **Node.js 22 LTS** y **TypeScript**. Implementa disparadores reactivos de Firestore (`onDocumentCreated`) para notificaciones push multicast vía FCM y funciones HTTPS invocables (`onCall`) para la supresión atómica de cuentas y purga de recursos en Cloud Storage con privilegios administrativos elevados.

[![Firebase Functions](https://img.shields.io/badge/Firebase_Functions-v2_(6.0.1)-FFCA28?style=flat&logo=firebase&logoColor=black)](https://firebase.google.com/docs/functions)
[![Node.js](https://img.shields.io/badge/Node.js-22_LTS-339933?style=flat&logo=nodedotjs&logoColor=white)](https://nodejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.7.3-3178C6?style=flat&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Firebase Admin SDK](https://img.shields.io/badge/Firebase_Admin-12.6.0-FFCA28?style=flat&logo=firebase&logoColor=black)](https://firebase.google.com/docs/admin/setup)
[![Cloud Firestore](https://img.shields.io/badge/Cloud_Firestore-Triggers_v2-FFCA28?style=flat&logo=firebase&logoColor=black)](https://firebase.google.com/docs/firestore)
[![FCM Multicast](https://img.shields.io/badge/FCM-Multicast_Messaging-FF6F00?style=flat&logo=firebase&logoColor=white)](https://firebase.google.com/docs/cloud-messaging)
[![Cloud Storage](https://img.shields.io/badge/Cloud_Storage-Bucket_Purge-4285F4?style=flat&logo=googlecloud&logoColor=white)](https://cloud.google.com/storage)
[![ESLint](https://img.shields.io/badge/ESLint-8.9.0-4B32C3?style=flat&logo=eslint&logoColor=white)](https://eslint.org/)

[← Volver al Ecosistema General](../README.md) · [App Móvil (Android Nativo Compose)](../nodes-android-nativo) · [Portal Web (React 19 SPA)](../nodes-front-web)

---

## Visión General del Backend Serverless

El paquete **nodes-functions** centraliza la lógica empresarial sensible y las operaciones con privilegios elevados que no deben exponerse directamente en las aplicaciones cliente (móvil o web):

1. **Despacho Multicast No Bloqueante:** Cuando cualquier usuario envía un mensaje, lista de tareas o imagen en un nodo, una Cloud Function de segunda generación captura el evento de creación del documento de forma asíncrona, resuelve la lista de miembros destinatarios (excluyendo al remitente), extrae sus tokens FCM y entrega notificaciones push en paralelo a través de `sendEachForMulticast`.
2. **Protocolo Transaccional de Borrado Atómico en Cascada:** Para dar cumplimiento a las leyes de privacidad (GDPR) y las normas de convivencia de Google Play (EULA), la función invocable `deleteUserAccount` purga todos los rastros de un usuario (mensajes, salas donde es dueño, archivos binarios multimedia en Cloud Storage, tokens y su perfil en Firebase Authentication) sin sufrir las limitaciones de batch del SDK cliente.

---

## Arquitectura Serverless v2 & Principios Técnicos

* **Google Cloud Functions v2:** Diseñadas sobre la infraestructura moderna de Cloud Run, permitiendo inicialización rápida, concurrencia escalable y menor latencia de arranque en frío (*cold starts*).
* **Firebase Admin SDK Privilegiado:** Opera con credenciales de cuenta de servicio en la nube, lo que permite realizar tareas administrativas de mantenimiento, eliminación recursiva y lectura cruzada de tokens saltando las restricciones de reglas de seguridad cliente.
* **Separación de Responsabilidades:** El cliente sólo se encarga de persistir datos y reaccionar; toda la orquestación distribuida (notificaciones push a terceros y purga de buckets en Storage) es delegada al motor serverless.

---

## Anatomía del Proyecto & Directorios

```text
nodes-functions/
├── package.json                               # Dependencias (firebase-functions v2, admin 12, Node 22)
├── tsconfig.json                              # Configuración del compilador TypeScript (target es2021)
├── tsconfig.dev.json                          # Configuración TypeScript para entorno de desarrollo
├── .eslintrc.js                               # Reglas de linting Google standard para TypeScript
├── .firebaserc                                # Asociación con el proyecto Firebase (fir-test-75a01)
├── firebase.json                              # Reglas de empaquetado, predeploy hooks y runtime
└── src/
    └── index.ts                               # Definición de funciones: sendChatNotification y deleteUserAccount
```

---

## Catálogo Detallado de Funciones Cloud

### 1. `sendChatNotification` (Trigger Firestore v2)
* **Disparador:** `onDocumentCreated` escuchando la ruta declarativa:
  ```typescript
  {
    document: "workspaces/{workspaceId}/entries/{entryId}",
    region: "us-central1"
  }
  ```
* **Flujo Operativo:**
  1. Extrae los metadatos de la nueva entrada: `userId`, `type`, `value`, `name`.
  2. Determina polimórficamente el cuerpo de la notificación según el enum `EntryType`:
     * `MESSAGE` $\rightarrow$ Contenido del texto ingresado.
     * `CHECK_LIST` $\rightarrow$ *"Se agregó una nueva lista de tareas"*.
     * `LOCALIZATION` $\rightarrow$ *"Se compartió una localización"*.
     * `IMAGE` $\rightarrow$ *"Se envió una imagen"*.
     * `REMINDER` $\rightarrow$ *"Se creó un recordatorio"*.
     * `default` $\rightarrow$ *"Se actualizó el workspace"*.
  3. Consulta el documento del espacio en `workspaces/{workspaceId}` para obtener la lista `members`.
  4. Filtra activamente al remitente:
     ```typescript
     const recipientUids = memberUids.filter((uid) => uid !== senderUid);
     ```
  5. Consulta concurrentemente la subcolección `users/{uid}/tokens` de todos los miembros destinatarios recopilando los tokens FCM válidos.
  6. Si existen tokens activos, despacha la carga útil multicast mediante:
     ```typescript
     await admin.messaging().sendEachForMulticast({
       tokens: allTokens,
       data: {
         title: `${senderName} mandó un nuevo mensaje en ${workspaceName}`,
         body: notificationBody,
         workspaceId,
         sender: senderUid,
       },
     });
     ```

---

### 2. `deleteUserAccount` (HTTPS Callable v2)
* **Tipo de Función:** `onCall` (Invocable mediante `httpsCallable` desde React Web o Android Nativo).
* **Control de Acceso:** Verifica obligatoriamente `request.auth.uid`. Si no existe sesión válida, arroja `HttpsError("unauthenticated")`.
* **Secuencia de Supresión en Cascada:**
  1. **Tratamiento en Workspaces:**
     * Consulta todas las salas donde `members` contiene el `uid` del usuario.
     * **Si es Dueño (`ownerId === uid`):**
       * Lee y elimina en paralelo todos los documentos de la subcolección `entries`.
       * Elimina el documento raíz del espacio.
       * Purga recursivamente todos los archivos de imagen del espacio en Cloud Storage bajo el prefijo `node_pictures/{workspaceId}/`.
     * **Si es Miembro Regular:**
       * Remueve su UID del arreglo de miembros mediante `admin.firestore.FieldValue.arrayRemove(uid)`.
       * Busca y elimina todas las entradas creadas por dicho usuario en ese espacio (`where("userId", "==", uid)`).
  2. **Eliminación de Tokens & Perfil:**
     * Borra todos los documentos registrados en `users/{uid}/tokens`.
     * Borra el documento del perfil de usuario en `users/{uid}`.
     * Purga las fotografías de perfil en Cloud Storage bajo `profile_pictures/{uid}/`.
  3. **Eliminación Definitiva en Firebase Authentication:**
     * Invoca `await admin.auth().deleteUser(uid)`, deshabilitando irreversiblemente las credenciales del usuario.
     * Retorna `{ result: "Cuenta eliminada exitosamente" }`.

---

## Puesta en Marcha & Emuladores Locales

### Requisitos Previos
* **Node.js:** Versión 22.x LTS instalada en el sistema.
* **Firebase CLI:** Instalado globalmente mediante `npm install -g firebase-tools`.

---

### Instalación de Dependencias

```bash
# 1. Navegar al directorio de funciones
cd nodes-functions

# 2. Instalar módulos de Node.js
npm install
```

---

### Scripts NPM Disponibles

```bash
# Compilar código TypeScript a JavaScript (lib/index.js)
npm run build

# Compilar continuamente en modo observación
npm run build:watch

# Validar reglas de estilo con ESLint
npm run lint

# Iniciar emulador local de Firebase Functions
npm run serve

# Abrir consola interactiva de pruebas de funciones (Shell)
npm run shell

# Ver logs de ejecución remota en la nube
npm run logs
```

---

## Despliegue en la Nube (Production Deployment)

El archivo `firebase.json` contiene ganchos de predespliegue (`predeploy`) que compilan y validan el linter antes de subir el paquete:

```json
{
  "functions": {
    "source": ".",
    "codebase": "default",
    "disallowLegacyRuntimeConfig": true,
    "predeploy": [
      "npm run lint",
      "npm run build"
    ]
  }
}
```

Para publicar las funciones en Google Cloud Platform:
```bash
# Desplegar únicamente los microservicios serverless
firebase deploy --only functions
```
