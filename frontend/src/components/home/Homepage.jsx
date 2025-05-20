import React, { useState } from 'react'
import Navigation from './Navigation'
import { TbMapStar } from "react-icons/tb";
import { FaPlus } from "react-icons/fa";
import { FaHospitalSymbol } from "react-icons/fa";
import { GrUserPolice } from "react-icons/gr";
import { MdOutlineFireTruck } from "react-icons/md";
import { FaRegEye } from "react-icons/fa";
import { FaEyeSlash } from "react-icons/fa";
import MapSlovenia from './MapSlovenia';
import InformationPart from './InformationPart';
import { CiLogout } from "react-icons/ci";
import { toast } from 'react-toastify';
import axios from 'axios';
import { useNavigate } from "react-router-dom";

export default function Homepage() {
  const [simulation, setSimulation] = useState(1);
  const [object, setObject] = useState(1);
  const navigate = useNavigate();

  const [bolniceVidnost, setBolniceVidnost] = useState(true);
  const [policijaVidnost, setPolicijaVidnost] = useState(true);
  const [gasilciVidnost, setGasilciVidnost] = useState(true);
  const [hoveredMenu, setHoveredMenu] = useState(null);

  const historyData = [
    {id: 1, title: 'simulation1'},
    {id: 2, title: 'simulation2'},
    {id: 3, title: 'simulation3'},
    {id: 4, title: 'simulation4'},
    {id: 5, title: 'simulation5'},
    {id: 6, title: 'simulation6'},
    {id: 7, title: 'simulation7'},
    {id: 8, title: 'simulation8'},
    {id: 9, title: 'simulation9'},
    {id: 10, title: 'simulation10'},
    {id: 11, title: 'simulation11'},
    {id: 12, title: 'simulation12'},
  ]

  const handleLogout = async () => {
    try {
      await axios.post("http://localhost:3002/api/logout", {}, { withCredentials: true });
      console.log("Token cookie cleared");
      toast.success("Logged out");
      navigate("/login");
    } catch (err) {
      toast.error("Logout failed");
    }
  };

  

  const addObjectData = [
    { id: 1, icon: <FaHospitalSymbol color='red' size={50} /> },
    {id: 2, icon: <GrUserPolice color='blue' size={50} /> },
    { id: 3, icon: <MdOutlineFireTruck color='orange' size={50} /> },
  ]

  return (
    <div className='w-full'>
        <Navigation/>
        <div className="flex">
          <div className="w-[100px] z-10 h-[calc(100vh-64px)] top-[70px] fixed bg-blue-900 text-white shadow-md flex flex-col justify-between cursor-pointer">
            <div>
              <div onMouseEnter={() => setHoveredMenu('simulacije')} onMouseLeave={() => setHoveredMenu(null)} className={`w-full py-4 px-2 flex flex-col items-center hover:bg-blue-800 transition duration-200 ${hoveredMenu === 'simulacije' && 'bg-blue-800 font-bold'}`}>
                <TbMapStar size={30} />
                <span className='text-sm mt-1 text-center'>Simulacije</span>
              </div>

              <div onMouseEnter={() => setHoveredMenu('dodaj')} onMouseLeave={() => setHoveredMenu(null)} className={`w-full py-4 px-2 flex flex-col items-center hover:bg-blue-800 transition duration-200 ${hoveredMenu === 'dodaj' && 'bg-blue-800 font-bold'}`}>
                <FaPlus size={30} />
                <span className='text-sm mt-1 text-center'>Dodaj</span>
              </div>
            </div>

            {/* Spodnji logout */}
            <div onClick={handleLogout} className='w-full py-4 px-2 flex flex-col items-center hover:bg-blue-800 transition duration-200'>
              <CiLogout size={30} />
              <span className='text-sm mt-1 text-center'>Logout</span>
            </div>
          </div>


            {hoveredMenu === 'simulacije' && (
              <div className="fixed left-[100px] top-[70px] w-[220px] bg-blue-950 text-white shadow-lg overflow-y-auto max-h-[400px] z-20 rounded-r-md"
                onMouseEnter={() => setHoveredMenu('simulacije')}
                onMouseLeave={() => setHoveredMenu(null)}
              >
                {historyData.map((item, index) => (
                  <div
                    onClick={() => { setSimulation(item.id); }}
                    key={index}
                    className={`px-4 py-3 border-b border-blue-800 cursor-pointer transition duration-200 
                      ${simulation === item.id ? 'bg-blue-800 font-bold' : 'hover:bg-blue-800 hover:text-white'}`}
                  >
                    <span className="text-sm">{item.title}</span>
                  </div>
                ))}
              </div>
            )}

         {hoveredMenu === 'dodaj' && (
            <div className="fixed left-[100px] top-[156px] w-[90px] bg-blue-950 text-white shadow-lg max-h-[400px] z-20 rounded-r-md"
              onMouseEnter={() => setHoveredMenu('dodaj')}
              onMouseLeave={() => setHoveredMenu(null)}
            >
              {addObjectData.map((item, index) => (
                <div
                  onClick={() => { setObject(item.id); }}
                  key={index}
                  className={`p-3 border-b border-blue-800 flex items-center justify-center cursor-pointer transition duration-200
                    ${object === item.id ? 'bg-blue-800' : 'hover:bg-blue-800'}`}
                >
                  {item.icon}
                </div>
              ))}
            </div>
          )}

          {/* main content area */}
          <div className="flex-1 bg-white p-2 z-1">

<div className='fixed space-y-2 top-[80px] left-[120px] z-10 bg-gray-300 bg-opacity-70 rounded-md px-4 py-2 shadow-md'>
              <div onClick={() => setBolniceVidnost(!bolniceVidnost)} className={`${!bolniceVidnost && 'text-gray-600'} flex items-center gap-2 cursor-pointer font-bold text-[25px]`}>
                {bolniceVidnost ? <FaRegEye className='mt-1' /> : <FaEyeSlash className='mt-1' />  } bolnice
              </div>
              <div onClick={() => setPolicijaVidnost(!policijaVidnost)} className={`${!policijaVidnost && 'text-gray-600'} flex items-center gap-2 cursor-pointer font-bold text-[25px]`}>
                {policijaVidnost ? <FaRegEye className='mt-1' /> : <FaEyeSlash className='mt-1' />  } policijske postaje
              </div>
              <div onClick={() => setGasilciVidnost(!gasilciVidnost)} className={`${!gasilciVidnost && 'text-gray-600'} flex items-center gap-2 cursor-pointer font-bold text-[25px]`}>
                {gasilciVidnost ? <FaRegEye className='mt-1' /> : <FaEyeSlash className='mt-1' />  } gasilske postaje
              </div>
            </div>

            <div className="absolute inset-0 z-0">
              <MapSlovenia bolniceVidnost={bolniceVidnost} gasilciVidnost={gasilciVidnost} policijaVidnost={policijaVidnost} />
            </div>

          </div>
        </div>
        <InformationPart />
    </div>
  )
}



/*
git add -A
git commit -m "SCRUM-19 Implementacija izgleda Domače strani"
git push
*/
