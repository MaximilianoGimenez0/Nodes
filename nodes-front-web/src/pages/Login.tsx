import React, { useState, useEffect, useCallback, useRef } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { FirebaseError } from "firebase/app";
import { APP_CONFIG, ROUTES, AUTH_ERRORS, DEEP_LINKS } from "../config/constants";
import ForgotPasswordModal from "../components/ForgotPasswordModal";
import styles from "./Login.module.css";

export const Login: React.FC = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isForgotPasswordOpen, setIsForgotPasswordOpen] = useState(false);
  const location = useLocation();
  const [successNotice, setSuccessNotice] = useState<string | null>(() => location.state?.message || null);

  const openAppTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    return () => {
      if (openAppTimerRef.current) {
        clearTimeout(openAppTimerRef.current);
      }
    };
  }, []);

  const handleOpenApp = useCallback(() => {
    if (openAppTimerRef.current) {
      clearTimeout(openAppTimerRef.current);
    }

    // 1. Intentar abrir la aplicación nativa mediante deep link
    window.location.href = DEEP_LINKS.APP_LOGIN;

    // 2. Fallback: Si en 2.5s la pestaña sigue activa (la app no tomó el control o está en PC),
    // redirigir a la tienda Play Store
    openAppTimerRef.current = setTimeout(() => {
      if (!document.hidden) {
        window.location.href = DEEP_LINKS.PLAY_STORE_URL;
      }
    }, 2500);
  }, []);

  const { login, currentUser, loading: authLoading } = useAuth();
  const navigate = useNavigate();

  // Limpiar el estado de navegación para que no persista al recargar
  useEffect(() => {
    if (location.state?.message) {
      window.history.replaceState({}, document.title);
    }
  }, [location]);

  // Si el usuario ya está autenticado, redirigir a /home
  useEffect(() => {
    if (!authLoading && currentUser) {
      const destination = location.state?.from?.pathname || ROUTES.HOME;
      navigate(destination, { replace: true });
    }
  }, [currentUser, authLoading, navigate, location]);

  const mapAuthError = (errorCode: string): string => {
    return AUTH_ERRORS[errorCode] || AUTH_ERRORS.DEFAULT;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!email.trim() || !password.trim()) {
      setErrorMessage("Por favor, completa todos los campos.");
      return;
    }

    try {
      setIsSubmitting(true);
      await login(email.trim(), password);
      navigate(ROUTES.HOME, { replace: true });
    } catch (error) {
      console.error("Error en login:", error);
      if (error instanceof FirebaseError) {
        setErrorMessage(mapAuthError(error.code));
      } else {
        setErrorMessage("Error inesperado al intentar iniciar sesión.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={styles.authPage}>
      <div className={styles.authCardContainer}>
        <div className={styles.authBrand}>
          <div className={styles.brandIconWrapper}>
            <svg
              className={styles.brandLogo}
              viewBox="0 0 500 500"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <circle cx="250" cy="250" r="240" fill="#8F98D6" />

              <line x1="250" y1="140" x2="140" y2="250" stroke="#6748A5" strokeWidth="24" strokeLinecap="round" />
              <line x1="250" y1="140" x2="360" y2="250" stroke="#6748A5" strokeWidth="24" strokeLinecap="round" />
              <line x1="140" y1="250" x2="250" y2="360" stroke="#6748A5" strokeWidth="24" strokeLinecap="round" />
              <line x1="360" y1="250" x2="250" y2="360" stroke="#6748A5" strokeWidth="24" strokeLinecap="round" />

              <circle cx="250" cy="140" r="56" fill="#6748A5" />
              <circle cx="250" cy="140" r="30" fill="#FFFFFF" />

              <circle cx="250" cy="360" r="56" fill="#6748A5" />
              <circle cx="250" cy="360" r="30" fill="#FFFFFF" />

              <circle cx="140" cy="250" r="56" fill="#6748A5" />
              <circle cx="140" cy="250" r="30" fill="#FFFFFF" />

              <circle cx="360" cy="250" r="56" fill="#6748A5" />
              <circle cx="360" cy="250" r="30" fill="#FFFFFF" />
            </svg>
          </div>
          <h1 className={styles.brandTitle}>{APP_CONFIG.name}</h1>
          <p className={styles.brandSubtitle}>{APP_CONFIG.description}</p>
        </div>

        {successNotice && (
          <div className={`${styles.alert} ${styles.alertSuccess}`} role="alert">
            <svg
              className={styles.alertIcon}
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
            <div className={styles.alertContent}>
              <span>{successNotice}</span>
            </div>
            <button
              type="button"
              className={styles.alertClose}
              onClick={() => setSuccessNotice(null)}
              aria-label="Cerrar notificación"
            >
              ✕
            </button>
          </div>
        )}

        {/* Formulario de Login */}
        <div className={styles.authCard}>
          <div className={styles.authCardHeaderRow}>
            <h2 className={styles.authCardTitle}>Iniciar Sesión</h2>
            <button
              type="button"
              className={styles.btnOpenAppCta}
              onClick={handleOpenApp}
              id="btn-login-open-app"
              title="Abrir en la aplicación móvil NODES o descargar desde Google Play"
            >
              <svg
                className={styles.btnOpenAppIcon}
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
              >
                <rect x="5" y="2" width="14" height="20" rx="2" ry="2" />
                <line x1="12" y1="18" x2="12.01" y2="18" />
              </svg>
              <span>Abrir en la App</span>
            </button>
          </div>
          <p className={styles.authCardDescription}>
            Accede con tus credenciales de {APP_CONFIG.name} para gestionar tu perfil y privacidad.
          </p>

          {errorMessage && (
            <div className={`${styles.alert} ${styles.alertDanger}`} role="alert">
              <svg
                className={styles.alertIcon}
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
              <span>{errorMessage}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className={styles.authForm} noValidate>
            <div className={styles.formGroup}>
              <label htmlFor="email-input" className={styles.formLabel}>
                Correo electrónico
              </label>
              <div className={styles.inputIconWrapper}>
                <svg
                  className={styles.inputIcon}
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                >
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
                  <polyline points="22,6 12,13 2,6" />
                </svg>
                <input
                  id="email-input"
                  type="email"
                  className={`${styles.formInput} ${styles.withIcon}`}
                  placeholder="usuario@ejemplo.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={isSubmitting}
                  autoComplete="email"
                  required
                />
              </div>
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="password-input" className={styles.formLabel}>
                Contraseña
              </label>
              <div className={styles.inputIconWrapper}>
                <svg
                  className={styles.inputIcon}
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                >
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
                <input
                  id="password-input"
                  type={showPassword ? "text" : "password"}
                  className={`${styles.formInput} ${styles.withIcon} ${styles.withAction}`}
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={isSubmitting}
                  autoComplete="current-password"
                  required
                />
                <button
                  type="button"
                  className={styles.btnInputAction}
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? "Ocultar contraseña" : "Ver contraseña"}
                  tabIndex={-1}
                >
                  {showPassword ? (
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                      <line x1="1" y1="1" x2="23" y2="23" />
                    </svg>
                  ) : (
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                  )}
                </button>
              </div>
            </div>

            <button
              type="submit"
              className={styles.btnSubmit}
              disabled={isSubmitting}
              id="btn-login-submit"
            >
              {isSubmitting ? (
                <>
                  <span className={styles.btnSpinner} aria-hidden="true"></span>
                  <span>Ingresando...</span>
                </>
              ) : (
                "Acceder a mi cuenta"
              )}
            </button>

            <div className={styles.forgotPasswordWrapper}>
              <button
                type="button"
                className={styles.btnTextForgotPassword}
                onClick={() => setIsForgotPasswordOpen(true)}
                id="btn-forgot-password"
              >
                Olvidé mi contraseña
              </button>
            </div>
          </form>
        </div>

        <footer className={styles.authFooter}>
          <p>{APP_CONFIG.getCopyright()}</p>
          <div className={styles.authFooterLinks}>
            <Link to={ROUTES.EULA} className={styles.eulaLink} id="link-login-eula">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
              <span>Ver Acuerdo de Licencia (EULA)</span>
            </Link>
            <span className={styles.privacyBadge}>Privacidad y Seguridad Garantizada</span>
          </div>
        </footer>

        {/* Modal para restablecimiento de contraseña vía Firebase Auth */}
        {isForgotPasswordOpen && (
          <ForgotPasswordModal
            isOpen={isForgotPasswordOpen}
            onClose={() => setIsForgotPasswordOpen(false)}
            defaultEmail={email}
            onSuccess={(sentEmail) => {
              setSuccessNotice(
                `Te hemos enviado un correo de recuperación a ${sentEmail}. Revisa tu bandeja de entrada o spam.`
              );
            }}
          />
        )}
      </div>
    </div>
  );
};

export default Login;
