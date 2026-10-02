import React, { useState } from "react";
import type { UserProfile } from "../types/user";
import { APP_CONFIG } from "../config/constants";
import styles from "./ProfileCard.module.css";

interface ProfileCardProps {
  profile: UserProfile;
  onDeleteClick: () => void;
}

export const ProfileCard: React.FC<ProfileCardProps> = ({ profile, onDeleteClick }) => {
  const [imageError, setImageError] = useState(false);
  const [copiedEmail, setCopiedEmail] = useState(false);

  // Obtener iniciales del usuario para el avatar de respaldo
  const getInitials = (): string => {
    const first = profile.firstName?.trim().charAt(0) || "";
    const last = profile.lastName?.trim().charAt(0) || "";
    const combined = (first + last).toUpperCase();
    if (combined) return combined;
    if (profile.email) return profile.email.charAt(0).toUpperCase();
    return "N";
  };

  const fullName = `${profile.firstName || ""} ${profile.lastName || ""}`.trim() || `Usuario de ${APP_CONFIG.name}`;
  const userEmail = profile.email || "No especificado";

  const handleCopyEmail = async () => {
    if (!profile.email) return;
    try {
      await navigator.clipboard.writeText(profile.email);
      setCopiedEmail(true);
      setTimeout(() => setCopiedEmail(false), 2000);
    } catch (err) {
      console.error("Error al copiar correo al portapapeles:", err);
    }
  };

  const shouldShowImage = Boolean(profile.profilePicture && !imageError);

  return (
    <div className={styles.profileCard}>
      <div className={styles.profileCardGlow}></div>

      {/* Encabezado del perfil con Avatar */}
      <div className={styles.profileHeader}>
        <div className={styles.avatarWrapper}>
          {shouldShowImage ? (
            <img
              src={profile.profilePicture}
              alt={`Foto de perfil de ${fullName}`}
              className={styles.avatarImage}
              onError={() => setImageError(true)}
            />
          ) : (
            <div className={styles.avatarFallback} aria-label={`Iniciales: ${getInitials()}`}>
              <span className={styles.avatarInitials}>{getInitials()}</span>
            </div>
          )}
          <div className={`${styles.statusIndicator} ${styles.online}`} title={`Cuenta activa en ${APP_CONFIG.name}`}></div>
        </div>

        <div className={styles.profileMeta}>
          <h2 className={styles.profileFullname}>{fullName}</h2>
          <div className={styles.profileBadgesRow}>
            <span className={styles.profileBadge}>Usuario Verificado</span>
          </div>
        </div>
      </div>

      {/* Información del Usuario: Correo Electrónico */}
      <div className={styles.profileDetailsSingle}>
        <div className={styles.detailItem}>
          <div className={styles.detailHeaderRow}>
            <span className={styles.detailLabel}>Correo Electrónico</span>
            {copiedEmail && (
              <span className={styles.copyFeedbackInline}>¡Copiado al portapapeles!</span>
            )}
          </div>
          <div
            className={`${styles.detailValueWrapper} ${styles.copyable}`}
            onClick={handleCopyEmail}
            title="Haz clic para copiar el correo electrónico"
          >
            <svg
              className={styles.detailIcon}
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
              <polyline points="22,6 12,13 2,6" />
            </svg>
            <span className={styles.detailValue} title={userEmail}>
              {userEmail}
            </span>
            <button
              type="button"
              className={styles.btnIconCopy}
              aria-label="Copiar correo electrónico"
              onClick={(e) => {
                e.stopPropagation();
                handleCopyEmail();
              }}
            >
              {copiedEmail ? (
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
              ) : (
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
                  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                </svg>
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Zona de peligro / Eliminación de cuenta */}
      <div className={styles.dangerZone}>
        <div className={styles.dangerZoneHeader}>
          <div className={styles.dangerIconContainer}>
            <svg
              className={styles.dangerIcon}
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M3 6h18" />
              <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6" />
              <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2" />
            </svg>
          </div>
          <div className={styles.dangerInfo}>
            <h3 className={styles.dangerTitle}>Zona de Privacidad y Eliminación</h3>
            <p className={styles.dangerDescription}>
              Solicita la supresión permanente de tu perfil, nodos y recursos en {APP_CONFIG.name}.
            </p>
          </div>
        </div>

        <button
          type="button"
          onClick={onDeleteClick}
          className={styles.btnDeleteAccount}
          id="btn-delete-account"
        >
          <svg
            className={styles.btnIcon}
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <polyline points="3 6 5 6 21 6" />
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
            <line x1="10" y1="11" x2="10" y2="17" />
            <line x1="14" y1="11" x2="14" y2="17" />
          </svg>
          <span>Eliminar cuenta y datos</span>
        </button>
      </div>
    </div>
  );
};

export default ProfileCard;
