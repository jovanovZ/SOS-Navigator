import React, { useState } from 'react';
import Navigation from '../home/Navigation';
import { Link } from 'react-router-dom';
import { FaUserCircle } from 'react-icons/fa';
import { MdEmail } from 'react-icons/md';
import { FiEdit } from 'react-icons/fi';
import { TbLockPassword } from "react-icons/tb";

const historyExample = [
  { id: 1, date: '1.05.2021', from: 'Ljubljana 123a', to: 'Grosuplje 12b', stations: 'Policija, rešilci', time: '45 minut' },
  { id: 2, date: '1.12.2025', from: 'Ljubljana 123a', to: 'Grosuplje 12b', stations: 'Policija, rešilci, gasilci', time: '1h 15 minut' },
  { id: 3, date: '1.12.2022', from: 'Ljubljana 123a', to: 'Grosuplje 12b', stations: 'Gasilci', time: '15 minut' },
  { id: 4, date: '1.05.2025', from: 'Ljubljana 123a', to: 'Grosuplje 12b', stations: 'Policija, gasilci', time: '2h 35 minut' },
  { id: 5, date: '1.06.2024', from: 'Ljubljana 123a', to: 'Grosuplje 12b', stations: 'Policija, gasilci', time: '25 minut' },
  { id: 6, date: '1.06.2025', from: 'Ljubljana 123a', to: 'Grosuplje 12b', stations: 'Policija, rešilci', time: '1h ' },
];

export default function Profile() {
  const [editUsername, setEditUsername] = useState(false);
  const [editEmail, setEditEmail] = useState(false);
  const [tempEmail, setTempEmail] = useState('');
  const [tempUsername, setTempUsername] = useState('');

  const [editUsernameLoading, setEditUsernameLoading] = useState(false);
  const [editEmailLoading, setEditEmailLoading] = useState(false);

  const [changePassword, setChangePassword] = useState(false);


  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  return (
    <>
      <Navigation />
      <div className="p-8 mt-16 text-black bg-blue-50 min-h-screen">
        <div className="flex justify-between items-center mb-8 bg-white shadow-md rounded-lg p-6">
          <div className="flex items-center gap-6">
            <FaUserCircle className="text-blue-700 text-[80px]" />

            <div>

              {editUsername ? (
                <div className="relative w-[250px]">
                  <input
                    type="username"
                    placeholder="martin"
                    value={tempUsername}
                    onChange={(e) => setTempUsername(e.target.value)}
                    className="w-full pr-10 focus:bg-gray-200 hover:bg-gray-100 px-2 py-1 border border-gray-400 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-600"
                  />

                  <div className="absolute inset-y-0 right-0 flex items-center gap-1 pr-2 text-xl" onClick={() => setEditUsername(false)}>
                    <button className="text-green-600 hover:text-green-800 font-semibold" title="Shrani">✔</button>
                    <span className='font-semibold mb-1'>|</span>
                    <button className="text-red-500 hover:text-red-700 font-semibold" title="Prekliči" onClick={() => setTempUsername('')}>✖</button>
                  </div>
                </div>
                ) : (
                  <h3 className="text-2xl font-semibold text-gray-800 mb-1 flex items-center gap-2">
                    martin<span onClick={() => {setEditUsername(true); setEditEmail(false);}} className="text-xl cursor-pointer">✏️</span>
                  </h3>
                )
              }

              {editEmail ? (
                <div className="relative w-[250px]">
                  <input
                    type="email"
                    placeholder="martin.kobal1@gmail.com"
                    value={tempEmail}
                    onChange={(e) => setTempEmail(e.target.value)}
                    className="w-full pr-10 focus:bg-gray-200 hover:bg-gray-100 px-2 py-1 border border-gray-400 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-600"
                  />

                  <div className="absolute inset-y-0 right-0 flex items-center gap-1 pr-2 text-xl" onClick={() => setEditEmail(false)}>
                    <button className="text-green-600 hover:text-green-800 font-semibold" title="Shrani">✔</button>
                    <span className='font-semibold mb-1'>|</span>
                    <button className="text-red-500 hover:text-red-700 font-semibold" title="Prekliči" onClick={() => setTempEmail('')}>✖</button>
                  </div>
                </div>
              ) : (
                <p className="text-lg flex items-center gap-2 text-gray-600">
                  <MdEmail className="text-blue-500 text-xl mt-1" />
                  martin.kobal1@gmail.com <span onClick={() => {setEditEmail(true); setEditUsername(false);}} className="text-lg cursor-pointer">✏️</span>
                </p>  
              )}
            </div>
          </div>

        <div className={`fixed top-16 right-0 z-10 m-4 w-[320px] text-sm text-gray-700 space-y-3 border border-gray-200 rounded-md pr-7 py-4 px-4 shadow-xl bg-white transform transition-transform duration-300 ${changePassword ? 'translate-x-0' : 'translate-x-full'}`}>
          <button
            className="absolute top-1 right-1 text-red-500 hover:text-red-700 font-semibold text-xl"
            onClick={() => {setChangePassword(false); setCurrentPassword(''); setNewPassword(''); setConfirmPassword('');}}>
            ✖
          </button>

          {
            currentPassword !== '' && newPassword !== '' && confirmPassword !== '' &&  newPassword === confirmPassword && (
          <button
              className="absolute bottom-1 right-1 text-green-600 hover:text-green-700 font-bold text-xl"
            >
              ✔
            </button>     
            )
          }

          <div className='flex flex-row justify-between items-center'>
            <span className="font-medium mr-2">Current password:</span>
            <input
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              type="password"
              placeholder="********"
              className="inline-block w-[150px] px-3 py-1 border border-gray-300 rounded-md bg-gray-50 text-gray-800 focus:outline-none focus:ring-1 focus:ring-gray-500 focus:border-gray-500 transition-shadow"
            />
          </div>

          <div className='flex flex-row justify-between items-center'>
            <span className="font-medium mr-2">New password:</span>
            <input
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              type="password"
              placeholder="********"
              className="inline-block w-[150px] px-3 py-1 border border-gray-300 rounded-md bg-gray-50 text-gray-800 focus:outline-none focus:ring-1 focus:ring-gray-500 focus:border-gray-500 transition-shadow"
            />
          </div>

          <div className='flex flex-row justify-between items-center'>
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
            <div onClick={() => {setChangePassword(true)}} className="bg-gray-400 flex items-center cursor-pointer justify-center text-white px-4 py-2 rounded-sm hover:bg-gray-500 transition duration-500">
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
                {historyExample.map((item) => (
                  <tr
                    key={item.id}
                    className="bg-gray-100 cursor-pointer text-gray-800 hover:bg-blue-100 hover:text-blue-900 transition duration-150"
                  >
                    <td className="px-4 py-2">{item.date}</td>
                    <td className="px-4 py-2">{item.from}</td>
                    <td className="px-4 py-2">{item.to}</td>
                    <td className="px-4 py-2">{item.stations}</td>
                    <td className="px-4 py-2">{item.time}</td>
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
