import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import NodesBackground from "./components/NodesBackground";
import Login from "./pages/Login";
import Home from "./pages/Home";
import Eula from "./pages/Eula";
import { ROUTES } from "./config/constants";

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <NodesBackground />
      <BrowserRouter>
        <Routes>
          <Route path={ROUTES.LOGIN} element={<Login />} />
          <Route path={ROUTES.EULA} element={<Eula />} />

          <Route element={<ProtectedRoute />}>
            <Route path={ROUTES.HOME} element={<Home />} />
          </Route>

          <Route path={ROUTES.ROOT} element={<Navigate to={ROUTES.HOME} replace />} />
          <Route path="*" element={<Navigate to={ROUTES.HOME} replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
