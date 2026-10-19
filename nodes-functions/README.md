# NODES Functions — Serverless Backend & Cloud Triggers

> Backend serverless desarrollado con **Firebase Cloud Functions v2**, **Node.js 22 LTS** y **TypeScript**. Se encarga de procesar tareas en la nube: envía notificaciones push automáticas cuando hay nuevos mensajes en un nodo y gestiona la eliminación completa y segura de cuentas y archivos multimedia utilizando permisos de administrador.

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

El paquete **nodes-functions** centraliza la lógica del servidor y las operaciones que requieren permisos especiales para no sobrecargar ni exponer las aplicaciones cliente (móvil o web):

1. **Envío Automático de Notificaciones Push:** Cuando un usuario envía un mensaje, foto o lista de tareas en un nodo, una función en segundo plano detecta la nueva entrada, busca a los demás miembros de la sala y les entrega una notificación push en sus dispositivos.
2. **Eliminación Completa de Cuentas:** Para cumplir con las políticas de privacidad y directivas de Google Play, la función invocable `deleteUserAccount` permite que un usuario borre su cuenta junto a todos sus datos asociados (mensajes, salas donde es dueño, imágenes guardadas en Cloud Storage, tokens y su perfil de autenticación).

---

## Arquitectura Serverless v2 & Principios Técnicos

* **Funciones de 2ª Generación:** Construidas sobre la infraestructura de Cloud Run, lo que permite respuestas rápidas, manejo eficiente de conexiones simultáneas y menores tiempos de arranque.
* **Operaciones Administrativas Seguras:** Gracias al uso de Firebase Admin SDK en el servidor, las funciones pueden realizar limpiezas completas de datos y acceder a los tokens de mensajería sin comprometer las reglas de seguridad de los clientes.
* **Separación de Tareas:** Las aplicaciones móvil y web se enfocan en la experiencia del usuario, mientras que el envío de notificaciones y la limpieza de archivos quedan centralizados en la nube.

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
* **Disparador:** `onDocumentCreated` escuchando en la ruta:
  ```typescript
  {
    document: "workspaces/{workspaceId}/entries/{entryId}",
    region: "us-central1"
  }
  ```
* **Flujo Operativo:**
  1. Detecta la nueva entrada y extrae sus datos (`userId`, `type`, `value`, `name`).
  2. Ajusta el texto de la notificación según el tipo de contenido:
     * Texto $\rightarrow$ Contenido del mensaje ingresado.
     * Lista de tareas $\rightarrow$ *"Se agregó una nueva lista de tareas"*.
     * Ubicación $\rightarrow$ *"Se compartió una localización"*.
     * Imagen $\rightarrow$ *"Se envió una imagen"*.
     * Recordatorio $\rightarrow$ *"Se creó un recordatorio"*.
     * Por defecto $\rightarrow$ *"Se actualizó el workspace"*.
  3. Consulta el documento del espacio en `workspaces/{workspaceId}` para obtener la lista de miembros (`members`).
  4. Excluye al remitente para que no reciba la notificación de su propio mensaje:
     ```typescript
     const recipientUids = memberUids.filter((uid) => uid !== senderUid);
     ```
  5. Obtiene los tokens de notificación activos de los miembros destinatarios desde `users/{uid}/tokens`.
  6. Si hay tokens disponibles, envía la notificación push a todos ellos mediante:
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
* **Control de Acceso:** Verifica la sesión del usuario (`request.auth.uid`). Si no está autenticado, responde con error de autenticación.
* **Proceso de Eliminación:**
  1. **Espacios de Trabajo:**
     * Revisa todas las salas donde el usuario es miembro.
     * **Si es Dueño:**
       * Borra todas las entradas en la subcolección `entries`.
       * Elimina el documento principal del espacio.
       * Limpia los archivos e imágenes del espacio en Cloud Storage (`node_pictures/{workspaceId}/`).
     * **Si es Miembro Regular:**
       * Lo quita de la lista de miembros de la sala.
       * Elimina únicamente los mensajes y entradas creados por él en ese espacio.
  2. **Tokens y Perfil:**
     * Elimina los tokens registrados en `users/{uid}/tokens`.
     * Borra los datos del perfil en `users/{uid}`.
     * Elimina las fotos de perfil almacenadas en Cloud Storage (`profile_pictures/{uid}/`).
  3. **Baja en Firebase Authentication:**
     * Invoca `admin.auth().deleteUser(uid)` para dar de baja la cuenta de forma definitiva.
     * Devuelve una confirmación de éxito `{ result: "Cuenta eliminada exitosamente" }`.

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
