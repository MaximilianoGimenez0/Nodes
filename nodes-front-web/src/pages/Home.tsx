import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { httpsCallable } from "firebase/functions";
import { useAuth } from "../context/AuthContext";
import { functions } from "../services/firebase";
import Navbar from "../components/Navbar";
import ProfileCard from "../components/ProfileCard";
import ConfirmModal from "../components/ConfirmModal";
import type { UserProfile } from "../types/user";
import { APP_CONFIG, ROUTES, FIREBASE_CONSTANTS } from "../config/constants";
import styles from "./Home.module.css";

export const Home: React.FC = () => {
  const { currentUser, userProfile, logout, loading: authLoading } = useAuth();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const navigate = useNavigate();

  const effectiveProfile: UserProfile = {
    id: userProfile?.id || currentUser?.uid || "",
    email: userProfile?.email || currentUser?.email || "",
    firstName: userProfile?.firstName || (currentUser?.displayName || "").split(" ")[0] || "Usuario",
    lastName: userProfile?.lastName || (currentUser?.displayName || "").split(" ").slice(1).join(" ") || "",
    profilePicture: userProfile?.profilePicture || currentUser?.photoURL || "",
  };

  const handleDeleteAccount = async () => {
    setErrorMessage(null);
    setIsDeleting(true);

    try {
      const deleteUserAccountFn = httpsCallable(functions, FIREBASE_CONSTANTS.FUNCTIONS.DELETE_USER_ACCOUNT);
      await deleteUserAccountFn();

      // Tolerar fallos de cierre de sesión porque la cuenta ya fue destruida en Firebase Auth por el backend
      try {
        await logout();
      } catch (authError) {
        console.warn("Sesión cerrada tras eliminación:", authError);
      }

      navigate(ROUTES.LOGIN, {
        replace: true,
        state: {
          message: "Tu cuenta y todos tus datos fueron eliminados correctamente.",
        },
      });
    } catch (error: unknown) {
      console.error("Error al eliminar cuenta:", error);
      setIsDeleting(false);
      setIsModalOpen(false);

      const detailedMsg =
        error instanceof Error
          ? error.message
          : "No se pudo completar la eliminación de la cuenta. Por favor, intenta nuevamente o contacta a soporte.";
      setErrorMessage(detailedMsg);
    }
  };

  return (
    <div className={styles.homePage}>
      <Navbar />

      <main className={styles.mainContent}>
        <div className={styles.contentContainer}>
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
              <div className={styles.alertContent}>
                <strong>Error:</strong> {errorMessage}
              </div>
              <button
                type="button"
                className={styles.alertClose}
                onClick={() => setErrorMessage(null)}
                aria-label="Cerrar"
              >
                ✕
              </button>
            </div>
          )}

          <div className={styles.pageHeading}>
            <h1 className={styles.pageTitle}>Gestión de Perfil y Privacidad</h1>
            <p className={styles.pageDescription}>
              Visualiza tus datos registrados en {APP_CONFIG.name} o solicita la eliminación definitiva de tu cuenta y recursos.
            </p>
          </div>

          {authLoading && !userProfile ? (
            <div className={styles.skeletonCard}>
              <div className={styles.skeletonAvatar}></div>
              <div className={`${styles.skeletonLine} ${styles.full}`}></div>
              <div className={`${styles.skeletonLine} ${styles.half}`}></div>
            </div>
          ) : (
            <ProfileCard
              profile={effectiveProfile}
              onDeleteClick={() => setIsModalOpen(true)}
            />
          )}

          <div className={styles.infoCard}>
            <div className={styles.infoCardIcon}>
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              </svg>
            </div>
            <div className={styles.infoCardText}>
              <h4>Compromiso de Privacidad {APP_CONFIG.name}</h4>
              <p>
                Al solicitar la eliminación de tu cuenta, ejecutamos una función segura que purga tus documentos en Firestore, archivos en Cloud Storage y tu usuario en Firebase Authentication de manera permanente.
              </p>
            </div>
          </div>
        </div>
      </main>

      <ConfirmModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onConfirm={handleDeleteAccount}
        isLoading={isDeleting}
        userEmail={effectiveProfile.email}
      />
    </div>
  );
};

export default Home;
