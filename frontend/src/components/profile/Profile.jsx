import { useEffect, useState, useRef } from "react";
import Navigation from "../home/Navigation";
import { MdEmail } from "react-icons/md";
import { TbLockPassword } from "react-icons/tb";
import axios from "axios";
import { FaPlus } from "react-icons/fa";
import { useNavigate } from "react-router-dom";

export default function Profile() {
  const [user, setUser] = useState(null); // ⬅️ manjkajoča definicija
  const [editUsername, setEditUsername] = useState(false);
  const [editEmail, setEditEmail] = useState(false);
  const [tempEmail, setTempEmail] = useState("");
  const [tempUsername, setTempUsername] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [image, setImage] = useState("");
  const [tempImage, setTempImage] = useState("");
  const [showImageInput, setShowImageInput] = useState(false);

  const [simulations, setSimulations] = useState([]);

  const [changePassword, setChangePassword] = useState(false);
  const navigate = useNavigate();

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

useEffect(() => {
  const localStorageUserInfo = JSON.parse(localStorage.getItem("user"));
  setUser(localStorageUserInfo); // ⬅️ to dodaš

  setUsername(localStorageUserInfo.username);
  setEmail(localStorageUserInfo.email);
  setImage(localStorageUserInfo.image);
  const userId = localStorageUserInfo.id;

  const fetchSimulations = async () => {
    try {
      const res = await axios.get(
        `http://localhost:3002/api/simulation/user/${userId}`,
        { withCredentials: true }
      );

      if (res.status === 200) {
        const simulationsData = res.data || [];
        setSimulations(simulationsData);
      }
    } catch (error) {
      console.error("Error fetching simulations:", error);
    }
  };

  fetchSimulations();
}, []);



  function formatMs(ms) {
    const totalSeconds = Math.floor(ms / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    if (minutes >= 60) {
      const hours = Math.floor(minutes / 60);
      const remMinutes = minutes % 60;
      return `${hours}h ${remMinutes} min ${seconds} s`;
    }
    return `${minutes} min ${seconds} s`;
  }
  function formatTime(dateString) {
    if (!dateString) return "";
    const date = new Date(dateString);
    return date.toLocaleString("sl-SI", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  const handleChangeUsername = async () => {
    setEditUsername(false);
    try {
      const localStorageUserInfo = JSON.parse(localStorage.getItem("user"));
      const userId = localStorageUserInfo.id;
      const res = await axios.post(
        "http://localhost:3002/api/user/changeUsername",
        {
          username: tempUsername,
          userId: userId,
        },
        { withCredentials: true }
      );
      if (res.status === 200) {
        setUsername(tempUsername);
        setTempUsername("");
        localStorage.setItem(
          "user",
          JSON.stringify({
            ...localStorageUserInfo,
            username: res.data.username,
          })
        );
      }
    } catch (error) {
      console.error("handle change username", error);
    }
  };
  const handlechangeEmail = async () => {
    setEditEmail(false);
    try {
      const localStorageUserInfo = JSON.parse(localStorage.getItem("user"));
      const userId = localStorageUserInfo.id;
      const res = await axios.post(
        "http://localhost:3002/api/user/changeEmail",
        {
          email: tempEmail,
          userId: userId,
        },
        { withCredentials: true }
      );
      if (res.status === 200) {
        setEmail(tempEmail);
        setTempEmail("");
        localStorage.setItem(
          "user",
          JSON.stringify({
            ...localStorageUserInfo,
            email: res.data.email,
          })
        );
      }
    } catch (error) {
      console.error("handle change email", error);
    }
  };


  const fileInputRef = useRef();
  const [loader, setLoader] = useState(false);

  const handleProfileImageChange = async (e) => {
      setLoader(true)
      const file = e.target.files[0];
      if (!file) return;
    
      try {    
        const formData = new FormData();
        formData.append('image', file);
        formData.append('userId', JSON.parse(localStorage.getItem("user")).id);
    
        const res = await axios.post(
          'http://localhost:3002/api/user/update-profile-image',
          formData,
          { withCredentials: true }
        );
    
        // Posodobi lokalni storage z novo sliko
        const updatedUser = { ...user, image: res.data.image };
        localStorage.setItem('user', JSON.stringify(updatedUser));
    
        window.location.reload();
      } catch (error) {
        console.error('Error updating profile image:', error);
      } finally {
        setLoader(false);
      }
    };



  const handleChangePassword = async () => {
    try {
      const localStorageUserInfo = JSON.parse(localStorage.getItem("user"));
      const userId = localStorageUserInfo.id;
      const res = await axios.post(
        "http://localhost:3002/api/user/changePassword",
        {
          userId,
          password: currentPassword,
          newPassword: newPassword,
        },
        { withCredentials: true }
      );
      if (res.status === 200) {
        alert("Password changed successfully!");
        setChangePassword(false);
        setCurrentPassword("");
        setNewPassword("");
        setConfirmPassword("");
      } else {
        alert(res.data.message || "Failed to change password.");
      }
    } catch (error) {
      alert(
        error.response?.data?.message ||
          "Failed to change password. Please check your current password."
      );
    }
  };
  return (
    <>
      <Navigation />
      <div className="p-8 mt-16 text-black bg-blue-50 min-h-screen">
        <div className="flex justify-between items-center mb-8 bg-white shadow-md rounded-lg p-6">
          <div className="flex items-center gap-6">
<div className="relative w-28 h-28">
  <img
    src={user?.image}
    alt="Profile"
    className="w-full h-full object-cover rounded-full border-2 border-gray-300"
  />

  {user && username === user.username && (
    <>
      <div
        onClick={() => fileInputRef.current.click()}
        className="absolute bottom-1 right-1 bg-gray-800 border-2 border-white rounded-full p-1 cursor-pointer hover:bg-gray-900 transition"
      >
        <FaPlus className="text-white text-xs" />
      </div>
      <input
        type="file"
        accept="image/*"
        ref={fileInputRef}
        onChange={handleProfileImageChange}
        className="hidden"
      />
    </>
  )}

  {loader && (
    <div className="absolute inset-0 bg-white/70 flex items-center justify-center text-sm text-gray-700 rounded-full">
      Uploading...
    </div>
  )}
</div>

          <div>
            {editUsername ? (
              <div className="relative w-[250px]">
                <input
                    type="username"
                    placeholder={username}
                    value={tempUsername}
                    onChange={(e) => setTempUsername(e.target.value)}
                    className="w-full pr-10 focus:bg-gray-200 hover:bg-gray-100 px-2 py-1 border border-gray-400 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-600"
                />

                  <div className="absolute inset-y-0 right-0 flex items-center gap-1 pr-2 text-xl">
                    <button
                      className="text-green-600 hover:text-green-800 font-semibold"
                      title="Shrani"
                      onClick={handleChangeUsername}
                    >
                      ✔
                    </button>
                    <span className="font-semibold mb-1">|</span>
                    <button
                      className="text-red-500 hover:text-red-700 font-semibold"
                      title="Prekliči"
                      onClick={() => {
                        setTempUsername("");
                        setEditUsername(false);
                      }}
                    >
                      ✖
                    </button>
                  </div>
                </div>
              ) : (
                <h3 className="text-2xl font-semibold text-gray-800 mb-1 flex items-center gap-2">
                  {username}
                  <span
                    onClick={() => {
                      setEditUsername(true);
                      setEditEmail(false);
                    }}
                    className="text-xl cursor-pointer"
                  >
                    ✏️
                  </span>
                </h3>
              )}

              {editEmail ? (
                <div className="relative w-[250px]">
                  <input
                    type="email"
                    placeholder={email}
                    value={tempEmail}
                    onChange={(e) => setTempEmail(e.target.value)}
                    className="w-full pr-10 focus:bg-gray-200 hover:bg-gray-100 px-2 py-1 border border-gray-400 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-600"
                  />

                  <div className="absolute inset-y-0 right-0 flex items-center gap-1 pr-2 text-xl">
                    <button
                      className="text-green-600 hover:text-green-800 font-semibold"
                      title="Shrani"
                      onClick={handlechangeEmail}
                    >
                      ✔
                    </button>
                    <span className="font-semibold mb-1">|</span>
                    <button
                      className="text-red-500 hover:text-red-700 font-semibold"
                      title="Prekliči"
                      onClick={() => {
                        setTempEmail("");
                        setEditEmail(false);
                      }}
                    >
                      ✖
                    </button>
                  </div>
                </div>
              ) : (
                <p className="text-lg flex items-center gap-2 text-gray-600">
                  <MdEmail className="text-blue-500 text-xl mt-1" />
                  {email}
                  <span
                    onClick={() => {
                      setEditEmail(true);
                      setEditUsername(false);
                    }}
                    className="text-lg cursor-pointer"
                  >
                    ✏️
                  </span>
                </p>
              )}
            </div>
          </div>

          <div
            className={`fixed top-16 right-0 z-10 m-4 w-[320px] text-sm text-gray-700 space-y-3 border border-gray-200 rounded-md pr-7 py-4 px-4 shadow-xl bg-white transform transition-transform duration-300 ${
              changePassword ? "translate-x-0" : "translate-x-full"
            }`}
          >
            <button
              className="absolute top-1 right-1 text-red-500 hover:text-red-700 font-semibold text-xl"
              onClick={() => {
                setChangePassword(false);
                setCurrentPassword("");
                setNewPassword("");
                setConfirmPassword("");
              }}
            >
              ✖
            </button>

            {currentPassword !== "" &&
              newPassword !== "" &&
              confirmPassword !== "" &&
              newPassword === confirmPassword && (
                <button
                  className="absolute bottom-1 right-1 text-green-600 hover:text-green-700 font-bold text-xl"
                  onClick={handleChangePassword}
                >
                  ✔
                </button>
              )}

            <div className="flex flex-row justify-between items-center">
              <span className="font-medium mr-2">Current password:</span>
              <input
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                type="password"
                placeholder="********"
                className="inline-block w-[150px] px-3 py-1 border border-gray-300 rounded-md bg-gray-50 text-gray-800 focus:outline-none focus:ring-1 focus:ring-gray-500 focus:border-gray-500 transition-shadow"
              />
            </div>

            <div className="flex flex-row justify-between items-center">
              <span className="font-medium mr-2">New password:</span>
              <input
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                type="password"
                placeholder="********"
                className="inline-block w-[150px] px-3 py-1 border border-gray-300 rounded-md bg-gray-50 text-gray-800 focus:outline-none focus:ring-1 focus:ring-gray-500 focus:border-gray-500 transition-shadow"
              />
            </div>

            <div className="flex flex-row justify-between items-center">
              <span className="font-medium mr-2">Type again:</span>
              <input
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                type="password"
                placeholder="********"
                className="inline-block w-[150px] px-3 py-1 border border-gray-300 rounded-md bg-gray-50 text-gray-800 focus:outline-none focus:ring-1 focus:ring-gray-500 focus:border-gray-500 transition-shadow"
              />
            </div>
          </div>

          <div>
            <div
              onClick={() => {
                setChangePassword(true);
              }}
              className="bg-gray-400 flex items-center cursor-pointer justify-center text-white px-4 py-2 rounded-sm hover:bg-gray-500 transition duration-500"
            >
              <TbLockPassword className="inline-block mr-1" /> Change password
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-md p-6">
          <h3 className="text-2xl font-bold text-blue-800 mb-1">Zgodovina</h3>
          <p className="text-gray-600 mb-4">Podrobnosti simulacij</p>
          <div className="overflow-x-auto">
            <table className="w-full text-left table-auto border-separate border-spacing-y-2">
              <thead className="bg-blue-100 text-blue-800">
                <tr>
                  <th className="px-4 py-2 rounded-l-md">📅 Datum</th>
                  <th className="px-4 py-2">📍 Od</th>
                  <th className="px-4 py-2">📌 Do</th>
                  <th className="px-4 py-2">🚑 Postaje</th>
                  <th className="px-4 py-2 rounded-r-md">🕒 Čas</th>
                </tr>
              </thead>
              <tbody>
                {simulations.map((item) => (
                  <tr
                    onClick={() => {
                      localStorage.setItem(
                        "simulation",
                        JSON.stringify(item)
                      );
                      navigate('/');
                    }}
                    key={item._id}
                    className="bg-gray-100 cursor-pointer text-gray-800 hover:bg-blue-100 hover:text-blue-900 transition duration-150"
                  >
                    <td className="px-4 py-2">{formatTime(item.created)}</td>
                    <td className="px-4 py-2">{item.locationFrom}</td>
                    <td className="px-4 py-2">{item.locationTo}</td>
                    <td className="px-4 py-2">{item.typeOfServices}</td>
                    <td className="px-4 py-2">{formatMs(item.responseTime)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </>
  );
}
