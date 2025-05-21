import React, { useEffect, useState } from 'react'
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
import { MdAutorenew } from "react-icons/md";

export default function Homepage() {
  const [simulation, setSimulation] = useState(1);
  const [object, setObject] = useState(1);
  const navigate = useNavigate();


  const [addedObject, setAddedObject] = useState('bolnica');


  const [bolniceVidnost, setBolniceVidnost] = useState(true);
  const [policijaVidnost, setPolicijaVidnost] = useState(true);
  const [gasilciVidnost, setGasilciVidnost] = useState(true);
  const [hoveredMenu, setHoveredMenu] = useState(null);

  /*
  const [simulations, setSimulations] = useState([])

  useEffect(() => {
    const fetchSimulations = async () => {
      try {
        const response = await axios.get("http://localhost:3002/api/simulations", { withCredentials: true });
        setSimulations(response.data);
      } catch (error) {
        console.error("Error fetching simulations:", error);
        toast.error("Error fetching simulations");
      } finally {
        setLoading(false);
      }
  };
  */

  const simulationData = [   // simulacije
    {id: 1, title: 'simulation1', time: 15000, type: 'kriminal'},
    {id: 2, title: 'simulation2', time: 150300, type: 'prometna'},
    {id: 3, title: 'simulation3', time: 153000, type: 'požar'},
    {id: 4, title: 'simulation4', time: 153000, type: 'kriminal'},
    {id: 5, title: 'simulation5', time: 159000, type: 'zdravstveni primer'},
    {id: 6, title: 'simulation6', time: 152000, type: 'požar'},
    {id: 7, title: 'simulation7', time: 151000, type: 'prometna'},
    {id: 8, title: 'simulation8', time: 152000, type: 'kriminal'},
    {id: 9, title: 'simulation9', time: 157000, type: 'zdravstveni primer'},
    {id: 10, title: 'simulation10', time: 125000, type: 'zdravstveni primer'},
    {id: 11, title: 'simulation11', time: 145000, type: 'požar'},
    {id: 12, title: 'simulation12', time: 155000, type: 'prometna'},
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
    { id: 1, icon: <FaHospitalSymbol color='yellow' size={50} />, type: 'bolnica' },
    { id: 2, icon: <GrUserPolice color='blue' size={50} />, type: 'policija' },
    { id: 3, icon: <MdOutlineFireTruck color='orange' size={50} />, type: 'gasilci' },
  ]


  const [currentSimulation, setCurrentSimulation] = useState({id: 0, title: 'naslov simulacije', time: 0, type: ''});



  /*
  const [stations, setStations] = useState([]); 
  useEffect(() => {
    const fetchStations = async () => {
      try {
        const response = await axios.get("http://localhost:3002/api/stations", { withCredentials: true });
        setStationsData(response.data);
      } catch (error) {
        console.error("Error fetching stations:", error);
        toast.error("Error fetching stations");
      }
    };
    fetchStations();
  }, []);
  */
  const stations = [ 
        { type: 'Gasilci', latitude: 46.0569, longitude: 14.5058 },
        { type: 'Bolnica', latitude: 46.5547, longitude: 15.6459 },
        { type: 'Policijska', latitude: 46.2381, longitude: 15.2675 },
        { type: 'Gasilci', latitude: 46.2396, longitude: 14.3556 },
        { type: 'Policijska', latitude: 46.3625, longitude: 15.1103 },
        { type: 'Gasilci', latitude: 45.9578, longitude: 13.6431 },
        { type: 'Bolnica',  latitude: 45.5481, longitude: 13.7300 },
        { type: 'Gasilci',  latitude: 46.1556, longitude: 15.0535 },
        { type: 'Policijska',  latitude: 46.5111, longitude: 15.0800 },
        { type: 'Bolnica', latitude: 46.1383, longitude: 14.5934 },
        { type: 'Gasilci',  latitude: 46.2253, longitude: 14.6094 },
        { type: 'Bolnica',  latitude: 46.5450, longitude: 14.9645 },
        { type: 'Gasilci', latitude: 46.6581, longitude: 16.1666 },
        { type: 'Bolnica',  latitude: 46.5645, longitude: 16.4544 },
        { type: 'Policijska',  latitude: 45.5282, longitude: 13.5686 }
    ];

  // tole je za sloveniaMaps
  const [addedAccident, setAddedAccident] = useState(null);
  const [accidenceType, setAccidenceType] = useState("kriminal");
  const accidenceTypes = [
    { id: 1, type: 'prometna' },
    { id: 2, type: 'kriminal' },
    { id: 3, type: 'zdravstveni primer' },
    { id: 4, type: 'naravna nesreča' },
  ]

  const [showCheck, setShowCheck] = useState(false);


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
              <div onClick={() => {setShowCheck(false); setCurrentSimulation({id: 0, title: 'naslov simulacije', time: 0, type: ''}); setAddedAccident(null);}} className={`w-full py-4 px-2 flex flex-col items-center hover:bg-blue-800 transition duration-200`}>
                <MdAutorenew  size={30} />
                <span className='text-sm mt-1 text-center'>Nova simulacija</span>
              </div>
            </div>
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
                {simulationData.map((item, index) => (
                  <div
                    onClick={() => {setShowCheck(false); setSimulation(item.id); setCurrentSimulation(item); setAddedAccident(null); }}
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
                      onClick={() => { setObject(item.id); setAddedObject(item.type); }}
                      key={index}
                      className={`p-3 border-b border-blue-800 flex items-center justify-center cursor-pointer transition duration-200
                        ${object === item.id ? 'bg-blue-800' : 'hover:bg-blue-800'}`}
                    >
                      {item.icon}
                    </div>
                  ))}
                </div>
              )}

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

              <MapSlovenia
                bolniceVidnost={bolniceVidnost} 
                gasilciVidnost={gasilciVidnost} 
                policijaVidnost={policijaVidnost} 
                stations={stations}
                addedAccident={addedAccident}
                setAddedAccident={setAddedAccident}
                accidenceType={accidenceType}
                setAccidenceType={setAccidenceType}
                accidenceTypes={accidenceTypes}
                showCheck={showCheck}
                setShowCheck={setShowCheck}
              />
              
            </div>
          </div>
        </div>

        <InformationPart simulation={currentSimulation} />
    </div>
  )
}


