import React, { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import axios from "axios";

const ProtectedRoute = ({ children }) => {
  const [auth, setAuth] = useState(null); // null = loading, false = not logged in
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    axios.get("http://localhost:3002/api/me", { withCredentials: true })
      .then((res) => {
        setAuth(true);
      })
      .catch((err) => {
        setAuth(false);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="text-white text-center p-4">Checking authentication...</div>;

  return auth ? children : <Navigate to="/login" />;
};

export default ProtectedRoute;
