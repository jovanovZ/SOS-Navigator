import React from "react";
import { IoLogoGithub } from "react-icons/io";

const Login = () => {
  return (
    <div className="h-screen bg-gradient-to-b from-gray-700 via-gray-500 to-gray-300 flex justify-center items-center">
      <div className="bg-white/10 backdrop-blur-md p-8 rounded-xl w-80 text-white shadow-2xl border border-white/20">
        <div className="flex flex-col items-center mb-6">
          <h2 className="text-2xl flex items-center font-semibold gap-2">
            <IoLogoGithub className="text-3xl" />
            Logoipsum
          </h2>
        </div>
        <form className="flex flex-col space-y-3">
          <div>
            <label className="text-sm">Username:</label>
            <input
              type="text"
              placeholder="Type username..."
              className="w-full mt-2 px-3 py-2 rounded bg-gray-500 text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-white/30"
            />
          </div>
          <div>
            <label className="text-sm">Password:</label>
            <input
              type="password"
              placeholder="Type password..."
              className="w-full mt-2 px-3 py-2 rounded bg-gray-500 text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-white/30"
            />
          </div>
          <button
            type="submit"
            className="bg-gray-500 py-2 rounded hover:bg-gray-600 transition text-white font-semibold"
          >
            LOGIN
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