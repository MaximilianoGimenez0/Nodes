import React, { useEffect, useState, useCallback } from "react";
import { APP_CONFIG, CONFIRM_MODAL_CONSTANTS } from "../config/constants";
import styles from "./ConfirmModal.module.css";

interface ConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => Promise<void>;
  isLoading: boolean;
  userEmail?: string;
}

export const ConfirmModal: React.FC<ConfirmModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  isLoading,
  userEmail,
}) => {
  const [confirmKeyword, setConfirmKeyword] = useState("");
  const REQUIRED_KEYWORD = CONFIRM_MODAL_CONSTANTS.REQUIRED_KEYWORD;

  const handleClose = useCallback(() => {
    if (!isLoading) {
      setConfirmKeyword("");
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

  const isConfirmed = confirmKeyword.trim().toUpperCase() === REQUIRED_KEYWORD;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (isConfirmed && !isLoading) {
      onConfirm();
    }
  };

  return (
    <div
      className={styles.modalOverlay}
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-title"
      onClick={(e) => {
        if (e.target === e.currentTarget && !isLoading) {
          handleClose();
        }
      }}
    >
      <div className={styles.modalContainer}>
        <div className={styles.modalHeader}>
          <div className={styles.modalIconBadge}>
            <svg
              className={styles.iconWarning}
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
              <line x1="12" y1="9" x2="12" y2="13" />
              <line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
          </div>
          <h2 id="modal-title" className={styles.modalTitle}>
            ¿Eliminar cuenta permanentemente?
          </h2>
          <p className={styles.modalSubtitle}>
            Esta acción es irreversible y eliminará todos tus datos de {APP_CONFIG.name}.
          </p>
        </div>

        <div className={styles.modalBody}>
          <div className={styles.warningBox}>
            <h4 className={styles.warningBoxTitle}>Al proceder se eliminará:</h4>
            <ul className={styles.warningList}>
              <li>
                <span className={styles.bullet}>✕</span> Perfil y credenciales de acceso ({userEmail || "tu cuenta"})
              </li>
              <li>
                <span className={styles.bullet}>✕</span> Nodos creados, configuraciones e historial
              </li>
              <li>
                <span className={styles.bullet}>✕</span> Archivos, fotos y contenidos almacenados
              </li>
            </ul>
          </div>

          <form onSubmit={handleSubmit} className={styles.modalForm}>
            <label htmlFor="confirm-input" className={styles.formLabel}>
              Para confirmar, escribe <strong className={styles.textDanger}>{REQUIRED_KEYWORD}</strong> a continuación:
            </label>
            <input
              id="confirm-input"
              type="text"
              className={styles.formInput}
              placeholder={`Escribe ${REQUIRED_KEYWORD}`}
              value={confirmKeyword}
              onChange={(e) => setConfirmKeyword(e.target.value)}
              disabled={isLoading}
              autoFocus
              autoComplete="off"
            />

            <div className={styles.modalActions}>
              <button
                type="button"
                className={styles.btnCancel}
                onClick={handleClose}
                disabled={isLoading}
              >
                Cancelar
              </button>
              <button
                type="submit"
                className={styles.btnConfirm}
                disabled={!isConfirmed || isLoading}
              >
                {isLoading ? (
                  <>
                    <span className={styles.btnSpinner} aria-hidden="true"></span>
                    <span>Eliminando datos...</span>
                  </>
                ) : (
                  "Eliminar definitivamente"
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ConfirmModal;
