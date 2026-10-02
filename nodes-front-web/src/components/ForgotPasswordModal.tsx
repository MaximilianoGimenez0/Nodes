import React, { useState, useEffect, useCallback } from "react";
import { sendPasswordResetEmail } from "firebase/auth";
import { FirebaseError } from "firebase/app";
import { auth } from "../services/firebase";
import { AUTH_ERRORS } from "../config/constants";
import styles from "./ForgotPasswordModal.module.css";

interface ForgotPasswordModalProps {
  isOpen: boolean;
  onClose: () => void;
  defaultEmail?: string;
  onSuccess?: (email: string) => void;
}

export const ForgotPasswordModal: React.FC<ForgotPasswordModalProps> = ({
  isOpen,
  onClose,
  defaultEmail = "",
  onSuccess,
}) => {
  const [email, setEmail] = useState(defaultEmail);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isSuccess, setIsSuccess] = useState(false);

  const handleClose = useCallback(() => {
    if (!isLoading) {
      setError(null);
      setIsSuccess(false);
      onClose();
    }
  }, [isLoading, onClose]);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape" && isOpen && !isLoading) {
        handleClose();
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, isLoading, handleClose]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const trimmedEmail = email.trim();
    if (!trimmedEmail) {
      setError("Por favor, ingresa tu correo electrónico.");
      return;
    }

    // Validación básica de formato de correo
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(trimmedEmail)) {
      setError("Ingresa un correo electrónico con formato válido.");
      return;
    }

    try {
      setIsLoading(true);
      await sendPasswordResetEmail(auth, trimmedEmail);
      setIsSuccess(true);
      if (onSuccess) {
        onSuccess(trimmedEmail);
      }
    } catch (err) {
      console.error("Error al enviar correo de recuperación:", err);
      if (err instanceof FirebaseError) {
        setError(AUTH_ERRORS[err.code] || "No se pudo enviar el correo de recuperación. Verifica la dirección.");
      } else {
        setError("Ocurrió un error inesperado al conectar con Firebase.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div
      className={styles.modalOverlay}
      role="dialog"
      aria-modal="true"
      aria-labelledby="forgot-password-modal-title"
      onClick={(e) => {
        if (e.target === e.currentTarget && !isLoading) {
          handleClose();
        }
      }}
    >
      <div className={styles.modalContainer}>
        <header className={styles.modalHeader}>
          <div
            className={`${styles.modalIconBadge} ${
              isSuccess ? styles.modalIconBadgeSuccess : ""
            }`}
          >
            {isSuccess ? (
              <svg
                className={styles.iconSuccess}
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
              >
                <polyline points="20 6 9 17 4 12" />
              </svg>
            ) : (
              <svg
                className={styles.iconHeader}
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
              >
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
            )}
          </div>

          <h2 id="forgot-password-modal-title" className={styles.modalTitle}>
            {isSuccess ? "¡Correo enviado!" : "Recuperar contraseña"}
          </h2>
          <p className={styles.modalSubtitle}>
            {isSuccess
              ? "Revisa tu bandeja de entrada para continuar."
              : "Ingresa el correo electrónico asociado a tu cuenta y te enviaremos un enlace de restablecimiento."}
          </p>
        </header>

        <div className={styles.modalBody}>
          {isSuccess ? (
            <div className={styles.successContent}>
              <p className={styles.successText}>
                Hemos enviado un correo a{" "}
                <span className={styles.highlightEmail}>{email}</span> con las
                instrucciones para crear una nueva contraseña.
              </p>
              <button
                type="button"
                className={`${styles.btnSubmit} ${styles.btnSuccessClose}`}
                onClick={handleClose}
                id="btn-forgot-password-success-close"
              >
                Volver al inicio de sesión
              </button>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className={styles.modalForm} noValidate>
              {error && (
                <div className={styles.alertDanger} role="alert">
                  <svg
                    className={styles.alertIcon}
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    aria-hidden="true"
                  >
                    <circle cx="12" cy="12" r="10" />
                    <line x1="12" y1="8" x2="12" y2="12" />
                    <line x1="12" y1="16" x2="12.01" y2="16" />
                  </svg>
                  <span>{error}</span>
                </div>
              )}

              <div className={styles.formGroup}>
                <label htmlFor="reset-email-input" className={styles.formLabel}>
                  Correo electrónico
                </label>
                <div className={styles.inputIconWrapper}>
                  <svg
                    className={styles.inputIcon}
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    aria-hidden="true"
                  >
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
                    <polyline points="22,6 12,13 2,6" />
                  </svg>
                  <input
                    id="reset-email-input"
                    type="email"
                    className={styles.formInput}
                    placeholder="usuario@ejemplo.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    disabled={isLoading}
                    autoComplete="email"
                    required
                    autoFocus
                  />
                </div>
              </div>

              <div className={styles.modalActions}>
                <button
                  type="button"
                  className={styles.btnCancel}
                  onClick={handleClose}
                  disabled={isLoading}
                  id="btn-cancel-forgot-password"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className={styles.btnSubmit}
                  disabled={isLoading}
                  id="btn-submit-forgot-password"
                >
                  {isLoading ? (
                    <>
                      <span className={styles.btnSpinner} aria-hidden="true" />
                      <span>Enviando...</span>
                    </>
                  ) : (
                    "Enviar correo"
                  )}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default ForgotPasswordModal;
