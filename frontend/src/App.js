import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './App.css';
import React from 'react';
import Register from './components/auth/Register';
import Login from './components/auth/Login';
import Homepage from './components/home/Homepage';
import Profile from './components/profile/Profile';
import UpdateProfile from './components/profile/UpdateProfile'


function App() {
  return (
    <Router>
      <Routes>
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<Homepage />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/update" element={<UpdateProfile />} />
      </Routes>
    </Router>
  );
}

export default App;