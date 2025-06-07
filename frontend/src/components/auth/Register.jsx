import React, { useEffect, useState } from "react";
import { IoLogoGithub } from "react-icons/io";
import axios from "axios";
import { toast } from "react-toastify";
import { useNavigate } from "react-router-dom";

const Register = () => {
  const IP = process.env.REACT_APP_IP;
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    axios
      .post(`http://${IP}/api/user/logout`, {}, { withCredentials: true })
      .then(() => {
        localStorage.removeItem("user");
      })
      .catch((err) => {
        console.error("Logout on entry failed", err);
      });
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    if (password !== confirmPassword) {
      toast.error("Passwords do not match");
      setLoading(false);
      return;
    }

    try {
      const res = await axios.post(`http://${IP}/api/user/register`, {
        username,
        email,
        password,
      });

      if (res.status === 201) {
        toast.success("Registration successful!");
        setUsername("");
        setEmail("");
        setPassword("");
        setConfirmPassword("");
        navigate("/login");
      }
    } catch (error) {
      toast.error("Registration error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="h-screen bg-gradient-to-b from-blue-950 via-blue-950 to-black flex justify-center items-center">
      <div className="bg-blue-700/20 backdrop-blur-md p-8 rounded-xl w-80 text-gray-200 shadow-2xl border border-blue-300">
        <div className="flex flex-col items-center mb-6">
          <h2 className="text-4xl font-extrabold tracking-tight font-mono text-gray-100">
            SOS<span className="text-blue-300">-Navigator</span>
          </h2>
        </div>

        <form className="flex flex-col space-y-3" onSubmit={handleSubmit}>
          <div>
            <label className="text-sm text-gray-300">Username:</label>
            <input
              onChange={(e) => setUsername(e.target.value)}
              value={username}
              type="text"
              placeholder="Type username..."
              className={`w-full mt-2 px-3 py-2 rounded bg-blue-900/30 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-300 ${
                username ? "text-blue-300 font-bold" : "text-gray-200 font-bold"
              }`}
            />
          </div>

          <div>
            <label className="text-sm text-gray-300">Email:</label>
            <input
              onChange={(e) => setEmail(e.target.value)}
              value={email}
              type="text"
              placeholder="Type email..."
              className={`w-full mt-2 px-3 py-2 rounded bg-blue-900/30 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-300 ${
                email ? "text-blue-300 font-bold" : "text-gray-200 font-bold"
              }`}
            />
          </div>

          <div>
            <label className="text-sm text-gray-300">Password:</label>
            <input
              onChange={(e) => setPassword(e.target.value)}
              value={password}
              type="password"
              placeholder="Type password..."
              className={`w-full mt-2 px-3 py-2 rounded bg-blue-900/30 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-300 ${
                password ? "text-blue-300 font-bold" : "text-gray-200 font-bold"
              }`}
            />
          </div>

          <div>
            <label className="text-sm text-gray-300">Repeat password:</label>
            <input
              onChange={(e) => setConfirmPassword(e.target.value)}
              value={confirmPassword}
              type="password"
              placeholder="Repeat password..."
              className={`w-full mt-2 px-3 py-2 rounded bg-blue-900/30 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-300 ${
                confirmPassword ? "text-blue-300 font-bold" : "text-gray-200 font-bold"
              }`}
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="bg-blue-800/70 py-2 rounded hover:bg-blue-800/90 transition text-white font-semibold flex items-center justify-center"
          >
            {loading ? (
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              "REGISTER"
            )}
          </button>
        </form>

        <p className="mt-4 text-sm text-center text-gray-400">
          Already have an account?{" "}
          <a
            href="/login"
            className="font-semibold hover:text-blue-200 underline"
          >
            Login
          </a>
        </p>
      </div>
    </div>
  );
};

export default Register;
