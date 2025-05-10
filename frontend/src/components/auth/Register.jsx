import React from "react";
import { IoLogoGithub } from "react-icons/io";
import axios from "axios";
import { toast } from "react-toastify"; 
import { useNavigate } from "react-router-dom"; 


const Register = () => {
  const [username, setUsername] = React.useState("");
  const [email, setEmail] = React.useState("");
  const [password, setPassword] = React.useState("");
  const [confirmPassword, setConfirmPassword] = React.useState("");
  const [loading, setLoading] = React.useState(false);
  const navigator = useNavigate(); 

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    if (password !== confirmPassword) {
      toast.error("Passwords do not match");
      return;
    }
    try {
      const res = await axios.post('http://localhost:3002/api/register', {
        username, email, password,
      });

      if (res.status === 201) {
        toast.success("Registration successful!");
        setUsername(""); setEmail(""); setPassword(""); setConfirmPassword("");
        navigator("/login"); 
      }
      console.log(res.data);
       
    } catch (error) {
      toast.error("Registration error"); 
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

        <form className="flex flex-col space-y-2" onSubmit={handleSubmit}>
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
            <label className="text-sm">Email:</label>
            <input
              onChange={(e) => setEmail(e.target.value)}
              value={email}
              type="text"
              placeholder="Type email..."
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
          <div>
            <label className="text-sm">Repeat password:</label>
            <input
              onChange={(e) => setConfirmPassword(e.target.value)}
              value={confirmPassword}
              type="password"
              placeholder="Type password..."
              className="w-full mt-2 px-3 py-2 rounded bg-gray-500 text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-white/30"
            />
          </div>
          <button type="submit" disabled={loading} className="bg-gray-500 py-2 rounded hover:bg-gray-600 transition text-white font-semibold flex items-center justify-center">
            {loading ? (
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              "REGISTER"
            )}
          </button>
        </form>

        <p className="mt-4 text-sm text-center">
          Already have an account?{" "}
          <a href="/login" className="font-semibold hover:text-gray-100 underline">
            Login
          </a>
        </p>
      </div>
    </div>
  );
};

export default Register;