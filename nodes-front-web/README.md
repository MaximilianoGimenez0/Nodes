# 🖥️ NODES Front Web — Support & Account Governance Portal

> Portal web desarrollado con **React 19**, **TypeScript 6** y **Vite 8** para brindar soporte, información legal y gestión de cuentas del ecosistema NODES. Cumple con las directivas de Google Play y privacidad (términos EULA públicos), permite iniciar sesión con Firebase Authentication, restablecer contraseñas y solicitar la eliminación completa y definitiva de la cuenta en la nube.

[![React](https://img.shields.io/badge/React-19.2.8-20232A?style=flat&logo=react&logoColor=%2361DAFB)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-6.0.2-3178C6?style=flat&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-8.2.2-646CFF?style=flat&logo=vite&logoColor=white)](https://vitejs.dev/)
[![React Router](https://img.shields.io/badge/React_Router-7.18.3-CA4245?style=flat&logo=reactrouter&logoColor=white)](https://reactrouter.com/)
[![Firebase Web SDK](https://img.shields.io/badge/Firebase_Web-12.18.0-FFCA28?style=flat&logo=firebase&logoColor=black)](https://firebase.google.com/)
[![CSS Modules](https://img.shields.io/badge/CSS_Modules-Vanilla_CSS-1572B6?style=flat&logo=css3&logoColor=white)](https://github.com/css-modules/css-modules)
[![ESLint](https://img.shields.io/badge/ESLint-10.9.0-4B32C3?style=flat&logo=eslint&logoColor=white)](https://eslint.org/)
[![Firebase Hosting](https://img.shields.io/badge/Firebase_Hosting-Edge_CDN-FFCA28?style=flat&logo=firebase&logoColor=black)](https://firebase.google.com/docs/hosting)

[← Volver al Ecosistema General](../README.md) · [App Móvil (Android Nativo Compose)](../nodes-android-nativo) · [Microservicios Serverless (Cloud Functions v2)](../nodes-functions) · [Demo Online](https://nodes-web.maxigimenez.com.ar/)

---

## Visión General & Propósito del Portal Web

El portal web **nodes-front-web** cumple dos funciones principales dentro del ecosistema NODES:

1. **Términos de Uso y Cumplimiento con Google Play:** Brinda acceso público e independiente al Acuerdo de Licencia de Usuario Final (EULA) y ofrece un mecanismo directo para que cualquier usuario pueda solicitar el borrado total de su cuenta y sus datos, tal como lo exige Google Play, sin necesidad de tener la app móvil instalada.
2. **Gestión de Perfil y Recuperación de Contraseña:** Permite a los usuarios consultar su información de perfil registrada, administrar su sesión y solicitar correos para restablecer su contraseña a través de Firebase Authentication.

---

## Arquitectura de Frontend & Principios Técnicos

* **React 19:** Aprovecha las mejoras reactivas de React 19, transiciones fluidas y sincronización de sesión en tiempo real a través de `AuthContext`.
* **Estilos Modulares y Ligeros:** Utiliza CSS Modules (`*.module.css`) con variables personalizadas HSL definidas en `index.css`, logrando un diseño visual cuidado sin dependencias pesadas de estilos.
* **Rutas Protegidas:** Sistema de navegación seguro (`ProtectedRoute.tsx`) que verifica la sesión del usuario antes de dar acceso a las pantallas privadas.

---

## Anatomía del Proyecto & Directorios

```text
nodes-front-web/
├── index.html                                 # Punto de entrada HTML con meta tags y viewport
├── vite.config.ts                             # Configuración del empaquetador Vite y plugin React
├── firebase.json                              # Configuración de hosting, rewrites SPA y cache
├── .firebaserc                                # Asociación con el proyecto Firebase (fir-test-75a01)
├── .env.example                               # Plantilla de variables de entorno públicas
├── .env                                       # Variables de entorno locales configuradas
├── package.json                               # Dependencias de producción y scripts de desarrollo
├── tsconfig.json                              # Configuración central TypeScript
├── tsconfig.app.json                          # Configuración de compilación para la aplicación
├── tsconfig.node.json                         # Configuración para scripts de Node / Vite
└── src/
    ├── main.tsx                               # Inicialización de la aplicación y montaje en DOM
    ├── App.tsx                                # Definición del árbol de rutas y AuthProvider
    ├── index.css                              # Reset global, paleta HSL y tipografía Outfit / Inter
    ├── App.css                                # Reglas globales de contenedor
    ├── components/
    │   ├── ConfirmModal.tsx                   # Modal de confirmación que solicita escribir "ELIMINAR" para mayor seguridad
    │   ├── ConfirmModal.module.css            # Estilos del modal destructivo
    │   ├── ForgotPasswordModal.tsx            # Modal de recuperación de contraseña con validación
    │   ├── ForgotPasswordModal.module.css     # Estilos del modal de restablecimiento
    │   ├── Navbar.tsx                         # Barra superior unificada con estado de autenticación
    │   ├── Navbar.module.css                  # Estilos responsivos de la barra de navegación
    │   ├── NodesBackground.tsx                # Motor Canvas 2D de partículas conectadas a 60 FPS
    │   ├── NodesBackground.module.css         # Posicionamiento absoluto fijo en fondo
    │   ├── ProfileCard.tsx                    # Tarjeta de perfil de usuario y opción para solicitar el borrado de cuenta
    │   ├── ProfileCard.module.css             # Estilos de tarjeta de perfil con avatar y badges
    │   ├── ProtectedRoute.tsx                 # Ruta protegida que comprueba si el usuario inició sesión antes de dar acceso
    │   └── ProtectedRoute.module.css          # Spinner de carga centrado
    ├── config/
    │   └── constants.ts                       # Constantes de negocio, deep links y textos legales
    ├── context/
    │   └── AuthContext.tsx                    # Proveedor reactivo de autenticación y perfil Firestore
    ├── pages/
    │   ├── Eula.tsx                           # Visualizador completo del Acuerdo de Licencia (EULA)
    │   ├── Eula.module.css                    # Estilos del documento legal con sidebar de navegación
    │   ├── Home.tsx                           # Pantalla principal para ver el perfil, privacidad y solicitar la baja de cuenta
    │   ├── Home.module.css                    # Estilos de la vista de inicio y alertas
    │   ├── Login.tsx                          # Inicio de sesión con correo, clave y deep links
    │   └── Login.module.css                   # Estilos inmersivos de autenticación
    ├── services/
    │   └── firebase.ts                        # Inicialización de Auth, Firestore y Functions
    └── types/
        └── user.ts                            # Interfaces TypeScript de perfiles de usuario
```

---

## Flujos y Capacidades Clave

### 1. Confirmación Segura de Baja de Cuenta (ConfirmModal)
* Para evitar eliminaciones accidentales de datos irreversibles, `ConfirmModal.tsx` solicita al usuario escribir exactamente la palabra clave `ELIMINAR`.
* Al confirmar, se invoca la función serverless `deleteUserAccount` (`httpsCallable`).
* Al completarse el proceso, se cierra la sesión local y se redirige a `/login` con un mensaje de confirmación.

### 2. Recuperación de Contraseña
* `ForgotPasswordModal.tsx` se conecta con `sendPasswordResetEmail(auth, email)` de Firebase Authentication.
* Traduce los códigos de error habituales (correo inválido, usuario no encontrado, etc.) en mensajes claros y en español para el usuario.

### 3. Acceso Rápido a la App Móvil (Deep Linking)
* En la pantalla de inicio de sesión (`Login.tsx`), un botón permite abrir directamente la app nativa instalada:
  ```typescript
  // Intento de apertura de app nativa vía esquema personalizado
  window.location.href = DEEP_LINKS.APP_LOGIN; // "nodes://login"
  
  // Temporizador de fallback a Google Play Store si no se abrió la app
  openAppTimerRef.current = setTimeout(() => {
    if (!document.hidden) {
      window.location.href = DEEP_LINKS.PLAY_STORE_URL;
    }
  }, 2500);
  ```

---

## Puesta en Marcha & Desarrollo

### Requisitos Previos
* **Node.js:** Versión 22.x LTS (o 20+).
* **Gestor de Paquetes:** `npm` (v10+) o `pnpm`.

---

### Configuración de Variables de Entorno

Creá tu archivo `.env` basándote en la plantilla `.env.example`:

```bash
cd nodes-front-web
cp .env.example .env
```

Configurá las credenciales de tu proyecto de Firebase:
```ini
VITE_FIREBASE_API_KEY=AIzaSy...
VITE_FIREBASE_AUTH_DOMAIN=tu-proyecto.firebaseapp.com
VITE_FIREBASE_DATABASE_URL=https://tu-proyecto-default-rtdb.firebaseio.com
VITE_FIREBASE_PROJECT_ID=tu-proyecto
VITE_FIREBASE_STORAGE_BUCKET=tu-proyecto.firebasestorage.app
VITE_FIREBASE_MESSAGING_SENDER_ID=1234567890
VITE_FIREBASE_APP_ID=1:1234567890:web:abcdef123456
VITE_FIREBASE_MEASUREMENT_ID=G-XXXXXXXXXX

# Deep links móviles
VITE_APP_LOGIN_DEEP_LINK=nodes://login
VITE_PLAY_STORE_URL=https://play.google.com/store/apps/details?id=com.maxigimenez.nodes
```

---

### Scripts NPM Disponibles

```bash
# 1. Instalar dependencias
npm install

# 2. Iniciar servidor de desarrollo local con Vite (HMR)
npm run dev

# 3. Validar linter con ESLint
npm run lint

# 4. Compilar proyecto con validación de tipos TypeScript y Rollup
npm run build

# 5. Previsualizar la compilación de producción localmente
npm run preview
```

La aplicación estará disponible por defecto en: [http://localhost:5173](http://localhost:5173).

---

## Despliegue en Producción (Firebase Hosting)

El portal está configurado en `firebase.json` para desplegar la carpeta compilada `dist` con soporte completo de Single Page Application (reescritura de todas las rutas hacia `/index.html`):

```bash
# 1. Compilar bundle de producción
npm run build

# 2. Desplegar al target de hosting configurado
firebase deploy --only hosting
```
