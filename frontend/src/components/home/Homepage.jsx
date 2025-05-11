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
  const [history, setHistory] = useState(false);
  const [simulation, setSimulation] = useState(1);
  const [addObject, setAddObject] = useState(false);
  const [object, setObject] = useState(1);
  const navigate = useNavigate();

  const [bolniceVidnost, setBolniceVidnost] = useState(true);
  const [policijaVidnost, setPolicijaVidnost] = useState(true);
  const [gasilciVidnost, setGasilciVidnost] = useState(true);

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
          {/* left sidebar */}
          <div className="w-[100px] z-10 h-[calc(100vh-64px)] top-[70px] fixed cursor-pointer bg-gray-400">
            <div onClick={() => {setHistory(!history); setAddObject(false)}}  className={`w-[100px] ${history && 'bg-gray-500'}`}>
              <TbMapStar size={50} className='w-full' />
              <span className='w-full justify-center flex'>Simulacije</span>
            </div>
            
            <div onClick={() => {setAddObject(!addObject); setHistory(false)}}  className={`w-[100px] mt-2 ${addObject && 'bg-gray-500'}`}>
              <FaPlus size={50} className='w-full' />
              <span className='w-full justify-center flex'>Dodaj objekt</span>
            </div>       

            <div onClick={handleLogout} className='hover:bg-gray-500'>
              <CiLogout size={50} className='w-full' />
              <span className='w-full justify-center flex'>Logout</span>
            </div>       
          </div>


          <div className={`${!history && 'hidden'} fixed left-[100px] top-[70px] w-[200px] bg-gray-100 overflow-y-scroll max-h-[400px] z-20`}>
            {historyData.map((item, index) => (
              <div
                onClick={() => {setSimulation(item.id); setHistory(false);}}
                key={index}
                className={`${simulation === item.id && 'bg-gray-300'} flex border-b-2 items-center gap-2 cursor-pointer hover:bg-gray-200 p-2`}
              >
                <span className="text-lg">{item.title}</span>
              </div>
            ))}
          </div>

          
          <div className={`${!addObject && 'hidden'} fixed left-[100px] top-[152px] w-[70px] bg-gray-100 max-h-[400px] z-20`}>
            {addObjectData.map((item, index) => (
              <div
                onClick={() => {setObject(item.id); setAddObject(false);}}
                key={index}
                className={`${object === item.id && 'bg-gray-300'} flex border-b-2 items-center justify-center gap-2 cursor-pointer hover:bg-gray-200 p-2`}
              >
                {item.icon}
              </div>
            ))}
          </div>



          {/* main content area */}
          <div className="flex-1 bg-white p-2 z-1">

            <div className='fixed space-y-2 top-[80px] left-[120px] z-10'>
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
