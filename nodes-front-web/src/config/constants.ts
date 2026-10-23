export const APP_CONFIG = {
  name: "NODES",
  fullName: "NODES — Soporte y Gestión de Cuenta",
  description: "Portal de Soporte y Gestión de Cuenta",
  author: "Maximiliano Giménez",
  version: "1.0",
  releaseDate: "Agosto 2026",
  getCopyright: (year: number = new Date().getFullYear()) =>
    `© Maximiliano Giménez ${year} NODES. Todos los derechos reservados.`,
} as const;

export const ROUTES = {
  ROOT: "/",
  HOME: "/home",
  LOGIN: "/login",
  EULA: "/eula",
} as const;

export const DEEP_LINKS = {
  APP_LOGIN: import.meta.env.VITE_APP_LOGIN_DEEP_LINK || "nodes://login",
  PLAY_STORE_URL:
    import.meta.env.VITE_PLAY_STORE_URL ||
    "https://play.google.com/store/apps/details?id=com.tu.paquete.nodes",
} as const;

export const FIREBASE_CONSTANTS = {
  COLLECTIONS: {
    USERS: "users",
  },
  FUNCTIONS: {
    DELETE_USER_ACCOUNT: "deleteUserAccount",
  },
} as const;

export const AUTH_ERRORS: Record<string, string> = {
  "auth/invalid-email": "El formato del correo electrónico no es válido.",
  "auth/user-not-found": "No existe una cuenta asociada a este correo electrónico.",
  "auth/wrong-password": "La contraseña ingresada es incorrecta.",
  "auth/invalid-credential": "Credenciales inválidas. Verifica tu correo y contraseña.",
  "auth/user-disabled": "Esta cuenta ha sido deshabilitada.",
  "auth/too-many-requests": "Demasiados intentos fallidos. Por favor, intenta de nuevo más tarde.",
  "auth/network-request-failed": "Error de red. Verifica tu conexión a internet.",
  DEFAULT: "Ocurrió un error al procesar tu solicitud. Por favor, verifica tus datos.",
} as const;

export const CONFIRM_MODAL_CONSTANTS = {
  REQUIRED_KEYWORD: "ELIMINAR",
} as const;

export const NODES_BACKGROUND_CONFIG = {
  NODE_COUNT: 20,
  MAX_DISTANCE: 250,
  NODE_COLOR_BASE: "196, 198, 255",
} as const;

export const EULA_CONFIG = {
  badgeCategory: "Documento Legal Oficial",
  lastUpdated: "Agosto 2026",
  version: "1",
  title: "Acuerdo de Licencia de Usuario Final (EULA)",
  subtitle:
    "Términos de uso, licencia y normas de convivencia de la plataforma y aplicaciones móviles NODES.",
} as const;
