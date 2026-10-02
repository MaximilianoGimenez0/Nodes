import React, { createContext, useContext, useEffect, useState } from "react";
import {
  type User,
  type UserCredential,
  signInWithEmailAndPassword,
  signOut,
  onAuthStateChanged,
} from "firebase/auth";
import { doc, onSnapshot, getDoc } from "firebase/firestore";
import { auth, db } from "../services/firebase";
import type { UserProfile } from "../types/user";
import { FIREBASE_CONSTANTS } from "../config/constants";

interface AuthContextType {
  currentUser: User | null;
  userProfile: UserProfile | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<UserCredential>;
  logout: () => Promise<void>;
  refreshProfile: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  // Escuchar cambios en el estado de autenticación de Firebase
  useEffect(() => {
    let unsubscribeProfile: (() => void) | undefined;

    const unsubscribeAuth = onAuthStateChanged(auth, (user) => {
      setCurrentUser(user);

      if (user) {
        setLoading(true);
        const userDocRef = doc(db, FIREBASE_CONSTANTS.COLLECTIONS.USERS, user.uid);

        // Suscripción en tiempo real a los datos del perfil en Firestore
        unsubscribeProfile = onSnapshot(
          userDocRef,
          (docSnap) => {
            if (docSnap.exists()) {
              const data = docSnap.data();
              setUserProfile({
                id: user.uid,
                email: data.email || user.email || "",
                firstName: data.firstName || data.nombre || "",
                lastName: data.lastName || data.apellido || "",
                profilePicture: data.profilePicture || data.photoURL || data.avatar || "",
              });
            } else {
              // Si aún no existe el documento en Firestore, creamos una vista preliminar con los datos de Auth
              const displayNameParts = (user.displayName || "").split(" ");
              setUserProfile({
                id: user.uid,
                email: user.email || "",
                firstName: displayNameParts[0] || "Usuario",
                lastName: displayNameParts.slice(1).join(" ") || "",
                profilePicture: user.photoURL || "",
              });
            }
            setLoading(false);
          },
          (error) => {
            console.error("Error al escuchar cambios del perfil en Firestore:", error);
            // Fallback con datos de Firebase Auth
            setUserProfile({
              id: user.uid,
              email: user.email || "",
              firstName: "Usuario",
              lastName: "",
              profilePicture: user.photoURL || "",
            });
            setLoading(false);
          }
        );
      } else {
        if (unsubscribeProfile) {
          unsubscribeProfile();
        }
        setUserProfile(null);
        setLoading(false);
      }
    });

    return () => {
      unsubscribeAuth();
      if (unsubscribeProfile) {
        unsubscribeProfile();
      }
    };
  }, []);

  const login = async (email: string, password: string): Promise<UserCredential> => {
    return await signInWithEmailAndPassword(auth, email, password);
  };

  const logout = async (): Promise<void> => {
    await signOut(auth);
  };

  const refreshProfile = async (): Promise<void> => {
    if (!currentUser) return;
    try {
      const userDocRef = doc(db, FIREBASE_CONSTANTS.COLLECTIONS.USERS, currentUser.uid);
      const docSnap = await getDoc(userDocRef);
      if (docSnap.exists()) {
        const data = docSnap.data();
        setUserProfile({
          id: currentUser.uid,
          email: data.email || currentUser.email || "",
          firstName: data.firstName || data.nombre || "",
          lastName: data.lastName || data.apellido || "",
          profilePicture: data.profilePicture || data.photoURL || data.avatar || "",
        });
      }
    } catch (err) {
      console.error("Error al refrescar perfil:", err);
    }
  };

  const value: AuthContextType = {
    currentUser,
    userProfile,
    loading,
    login,
    logout,
    refreshProfile,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth debe ser utilizado dentro de un AuthProvider");
  }
  return context;
};
