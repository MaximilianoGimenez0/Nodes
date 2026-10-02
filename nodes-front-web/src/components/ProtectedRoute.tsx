import React from "react";
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { ROUTES } from "../config/constants";
import styles from "./ProtectedRoute.module.css";

interface ProtectedRouteProps {
  children?: React.ReactNode;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { currentUser, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className={styles.loadingScreen} role="status" aria-label="Cargando sesión...">
        <div className={styles.spinnerContainer}>
          <div className={styles.spinner}></div>
          <p className={styles.loadingText}>Verificando sesión...</p>
        </div>
      </div>
    );
  }

  if (!currentUser) {
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  return children ? <>{children}</> : <Outlet />;
};

export default ProtectedRoute;
