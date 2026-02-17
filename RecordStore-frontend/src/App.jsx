import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import { Routes, Route, Navigate } from "react-router-dom";
import { useEffect, useState } from "react";

import HomePage from "./components/HomePage";
import RecordList from "./components/RecordList";
import api, { loadStoredToken } from "./api/api";

function App() {
  const [isAuth, setIsAuth] = useState(null); // null = checking

  const checkAuth = () => {
    api.get("/home")
      .then(() => setIsAuth(true))
      .catch(() => setIsAuth(false));
  };

  useEffect(() => {
    loadStoredToken();
    checkAuth();
  }, []);

  if (isAuth === null) {
    return <div className="container mt-4">Loading...</div>;
  }

  return (
    <div className="container mt-4">
      <Routes>
        <Route path="/" element={<HomePage onAuthChanged={checkAuth} />} />

        <Route
          path="/records"
          element={isAuth ? <RecordList /> : <Navigate to="/" replace />}
        />
      </Routes>
    </div>
  );
}

export default App;
