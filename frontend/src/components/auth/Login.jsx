import React, {useState, useEffect} from "react";
import { IoLogoGithub } from "react-icons/io";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();


    useEffect(() => {
      axios.post("http://localhost:3002/api/logout", {}, { withCredentials: true })
        .then(() => {
          console.log("Token cookie cleared");
        })
        .catch((err) => {
          console.error("Logout on entry failed", err);
        });
    }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await axios.post("http://localhost:3002/api/login", 
        { username, password },
        { withCredentials: true }
    );
      if (res.status === 200) {
        toast.success("Login successful!");
        setUsername("");
        setPassword("");
        navigate("/");
      }
    } catch (error) {
      toast.error("Login error");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="h-screen bg-gradient-to-b from-gray-700 via-gray-500 to-gray-300 flex justify-center items-center">
      <div className="bg-white/10 backdrop-blur-md p-8 rounded-xl w-80 text-white shadow-2xl border border-white/20">
        <div className="flex flex-col items-center mb-6">
          <h2 className="text-2xl flex items-center font-semibold gap-2">
            <IoLogoGithub className="text-3xl" />
            Logoipsum
          </h2>
        </div>
        <form onSubmit={handleSubmit} className="flex flex-col space-y-3">
          <div>
            <label className="text-sm">Username:</label>
            <input
              onChange={(e) => setUsername(e.target.value)}
              value={username}
              type="text"
              placeholder="Type username..."
              className="w-full mt-2 px-3 py-2 rounded bg-gray-500 text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-white/30"
            />
          </div>
          <div>
            <label className="text-sm">Password:</label>
            <input
              onChange={(e) => setPassword(e.target.value)}
              value={password}
              type="password"
              placeholder="Type password..."
              className="w-full mt-2 px-3 py-2 rounded bg-gray-500 text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-white/30"
            />
          </div>
          <button type="submit" disabled={loading} className="bg-gray-500 py-2 rounded hover:bg-gray-600 transition text-white font-semibold flex items-center justify-center">
            {loading ? (
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              "LOGIN"
            )}
          </button>
        </form>
        <p className="mt-4 text-sm text-center">
          Don't have an account?{" "}
          <a href="/register" className="font-semibold hover:text-gray-100 underline">
            Register
          </a>
        </p>
      </div>
    </div>
  );
};

export default Login;