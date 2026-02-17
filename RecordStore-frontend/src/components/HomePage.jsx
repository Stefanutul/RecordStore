import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api, { clearAuth } from "../api/api";
import LoginModal from "../components/LoginModal";

function HomePage({ onAuthChanged }) {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [showLogin, setShowLogin] = useState(false);

  const loadHome = () => {
    api.get("/home")
      .then((res) => {
        setData(res.data);
        setError(null);
      })
      .catch(() => {
        setData(null);
        setError("Not authenticated");
      });
  };

  useEffect(() => {
    loadHome();
  }, []);

  const handleLogout = () => {
    clearAuth();
    setData(null);
    setError("Not authenticated");
    if (onAuthChanged) onAuthChanged();
  };

  const handleLoginSuccess = (homeData) => {
    setData(homeData);
    setError(null);
    if (onAuthChanged) onAuthChanged();
  };

  return (
    <div className="d-flex justify-content-between align-items-start">
      <div>
        {data ? <h1>{data.message}</h1> : <h3>{error || "Loading..."}</h3>}
      </div>

      <div className="d-flex gap-2">
        <Link
          to={data ? "/records" : "/"}
          className={`btn btn-primary btn-sm ${data ? "" : "disabled"}`}
        >
          Catalog
        </Link>

        <button
          className="btn btn-outline-primary btn-sm"
          onClick={() => setShowLogin(true)}
        >
          Switch User
        </button>

        <button
          className="btn btn-outline-danger btn-sm"
          onClick={handleLogout}
          disabled={!data}
        >
          Logout
        </button>
      </div>

      <LoginModal
        show={showLogin}
        onClose={() => setShowLogin(false)}
        onLoginSuccess={handleLoginSuccess}
      />
    </div>
  );
}

export default HomePage;
