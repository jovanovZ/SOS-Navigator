import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './App.css';
import React from 'react';
import Register from './components/auth/Register';
import Login from './components/auth/Login';
import Homepage from './components/home/Homepage';
import Profile from './components/profile/Profile';
import UpdateProfile from './components/profile/UpdateProfile'
import { ToastContainer } from 'react-toastify';
import ProtectedRoute from './components/protection/ProtectedRoute';


function App() {
  return (
    <>
    <Router>
      <Routes>
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Homepage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <Profile />
            </ProtectedRoute>
          }
        />

        <Route
          path="/update"
          element={
            <ProtectedRoute>
              <UpdateProfile />
            </ProtectedRoute>
          }
        />
      </Routes>
    </Router>

    <ToastContainer
        position="top-right"
        autoClose={2000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="dark"
        style={{ zIndex: 9999 }} 
    />
    </>
  );
}

export default App;