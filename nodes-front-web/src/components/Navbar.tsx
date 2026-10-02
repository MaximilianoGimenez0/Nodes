import React, { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { APP_CONFIG, ROUTES } from "../config/constants";
import styles from "./Navbar.module.css";

export const Navbar: React.FC = () => {
  const { currentUser, userProfile, logout } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const handleHomeClick = (e: React.MouseEvent) => {
    e.preventDefault();
    if (currentUser) {
      navigate(ROUTES.HOME);
    } else {
      navigate(ROUTES.LOGIN);
    }
  };

  const handleEulaClick = (e: React.MouseEvent) => {
    e.preventDefault();
    navigate(ROUTES.EULA);
  };

  const handleLogout = async () => {
    try {
      setIsLoggingOut(true);
      await logout();
      navigate(ROUTES.LOGIN, { replace: true });
    } catch (err) {
      console.error("Error al cerrar sesión desde la navbar:", err);
    } finally {
      setIsLoggingOut(false);
    }
  };

  const isHomeActive = location.pathname === ROUTES.HOME || (location.pathname === ROUTES.ROOT && Boolean(currentUser));
  const isEulaActive = location.pathname === ROUTES.EULA;

  const userDisplayName =
    userProfile?.firstName
      ? `${userProfile.firstName} ${userProfile.lastName || ""}`.trim()
      : currentUser?.displayName || currentUser?.email || "Usuario";

  const userInitial = userDisplayName.charAt(0).toUpperCase() || "U";
  const userPhoto = userProfile?.profilePicture || currentUser?.photoURL;

  return (
    <header className={styles.navbar}>
      <div className={styles.navbarContainer}>
        <div className={styles.brandGroup}>
          <button
            type="button"
            className={styles.navbarBrand}
            onClick={handleHomeClick}
            aria-label="Ir al inicio"
          >
            <div className={styles.brandIconSm}>
              <svg viewBox="0 0 500 500" fill="none" xmlns="http://www.w3.org/2000/svg">
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
            <span className={styles.navbarTitle}>{APP_CONFIG.name}</span>
          </button>

          {/* Enlaces de navegación */}
          <nav className={styles.navLinks} aria-label="Navegación principal">
            <button
              type="button"
              onClick={handleHomeClick}
              className={`${styles.navLink} ${isHomeActive ? styles.navLinkActive : ""}`}
              id="nav-home"
            >
              <svg className={styles.btnIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                <polyline points="9 22 9 12 15 12 15 22" />
              </svg>
              <span>Inicio</span>
            </button>

            <button
              type="button"
              onClick={handleEulaClick}
              className={`${styles.navLink} ${isEulaActive ? styles.navLinkActive : ""}`}
              id="nav-eula"
            >
              <svg className={styles.btnIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="16" y1="13" x2="8" y2="13" />
                <line x1="16" y1="17" x2="8" y2="17" />
                <polyline points="10 9 9 9 8 9" />
              </svg>
              <span>EULA</span>
            </button>
          </nav>
        </div>

        {/* Acciones de autenticación */}
        <div className={styles.navbarActions}>
          {currentUser ? (
            <>
              <div className={styles.userBadge} title={currentUser.email || "Usuario autenticado"}>
                <div className={styles.userBadgeAvatar}>
                  {userPhoto ? (
                    <img src={userPhoto} alt={userDisplayName} />
                  ) : (
                    <span>{userInitial}</span>
                  )}
                </div>
                <span className={styles.userBadgeEmail}>{currentUser.email || userDisplayName}</span>
              </div>
              <button
                type="button"
                className={styles.btnLogout}
                onClick={handleLogout}
                disabled={isLoggingOut}
                title="Cerrar sesión"
                id="nav-btn-logout"
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
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                  <polyline points="16 17 21 12 16 7" />
                  <line x1="21" y1="12" x2="9" y2="12" />
                </svg>
                <span>{isLoggingOut ? "Saliendo..." : "Cerrar sesión"}</span>
              </button>
            </>
          ) : (
            <button
              type="button"
              className={styles.btnLogin}
              onClick={() => navigate(ROUTES.LOGIN)}
              id="nav-btn-login"
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
                <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
                <polyline points="10 17 15 12 10 7" />
                <line x1="15" y1="12" x2="3" y2="12" />
              </svg>
              <span>Iniciar sesión</span>
            </button>
          )}
        </div>
      </div>
    </header>
  );
};

export default Navbar;
