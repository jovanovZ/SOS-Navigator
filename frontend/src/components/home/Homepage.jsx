import { useEffect, useState } from 'react'
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
import Loading from './Loading';

export default function Homepage() {
  const IP = process.env.REACT_APP_IP;

  const [text, setText] = useState('Loading');

  const [searchingExSimulation, setSearchingExSimulation] = useState(false); 

  const [loading, setLoading] = useState(false);

  const [simulation, setSimulation] = useState(1);
  const [object, setObject] = useState(1);
  const navigate = useNavigate();

  const [addObject, setAddObject] = useState(false);
  const [addedObject, setAddedObject] = useState('bolnica');


  const [bolniceVidnost, setBolniceVidnost] = useState(true);
  const [policijaVidnost, setPolicijaVidnost] = useState(true);
  const [gasilciVidnost, setGasilciVidnost] = useState(true);
  const [hoveredMenu, setHoveredMenu] = useState(null);


  const removeSimulationFromLocalStorage = () => {
  localStorage.removeItem("simulation");
};

  const [simulationData, setSimulationData] = useState([]); 

useEffect(() => {
  const fetchSimulations = async () => {
    setLoading(true)
    try {
      const user = localStorage.getItem("user");
      const userId = JSON.parse(user)?.id || JSON.parse(user)?._id;
      console.log("User ID:", userId);

      const response = await axios.get(
        `http://${IP}/api/simulation/user/${userId}`,
        {
          withCredentials: true,
        }
      );

      console.log("Simulacije:", response.data);
      setSimulationData(response.data);
    } catch (error) {
      console.error("Napaka pri pridobivanju simulacij:", error);
      toast.error("Napaka pri pridobivanju simulacij");
    } finally {
      setLoading(false);
    }
  };

  fetchSimulations();
}, []);



  const handleLogout = async () => {
    try {
      await axios.post(`http://${IP}/api/user/logout`, {}, { withCredentials: true });
      console.log("Token cookie cleared");
      toast.success("Logged out");
      navigate("/login");
    } catch (err) {
      toast.error("Logout failed");
    }
  };

  

  const addObjectData = [
    { id: 1, icon: <FaHospitalSymbol color='yellow' size={50} />, type: 'Bolnica' },
    { id: 2, icon: <GrUserPolice color='blue' size={50} />, type: 'Policijska' },
    { id: 3, icon: <MdOutlineFireTruck color='orange' size={50} />, type: 'Gasilci' },
  ]


  const [currentSimulation, setCurrentSimulation] = useState(null);

  
  const [stations, setStations] = useState([]); 
  useEffect(() => {

    const fetchStations = async () => {
      try {
        const response = await axios.get(`http://${IP}/api/station/all`, 
          { withCredentials: true }
        );
        setStations(response.data);
        console.log(response.data);
        console.log('hi')
      } catch (error) {
        console.error("Error fetching stations:", error);
        toast.error("Error fetching stations");
      }
    };
    fetchStations();
  }, []);



  
  const [time, setTime] = useState(0); 
  const [newPath, setNewPath] = useState([]);  // tole je za sloveniaMaps
  const [addedAccident, setAddedAccident] = useState(null);
  const [accidenceType, setAccidenceType] = useState("kriminal");
  const accidenceTypes = [
    { id: 1, type: 'prometna' },
    { id: 2, type: 'kriminal' },
    { id: 3, type: 'zdravstveni primer' },
    { id: 4, type: 'naravna nesreča' },
  ]

  const [bestStation, setBestStation] = useState(null);

useEffect(() => {
    const findClosestByRoad = async () => {
    if (!addedAccident || stations.length === 0) return;

    const accidentToStationType = {
      "prometna": "Policijska",
      "kriminal": "Policijska",
      "zdravstveni primer": "Bolnica",
      "naravna nesreča": "Gasilci"
    };

    const relevantStations = stations.filter(
      (s) => s.typeOfStation === accidentToStationType[addedAccident.type]
    );

    let closestStation = null;
    let minDistance = Infinity;
    let finalPath = [];

    for (const station of relevantStations) {
      const coords = station.locationId?.geometry?.coordinates;
      if (!coords || coords.length !== 2) continue;

      const from = [coords[0], coords[1]];
      const to = [addedAccident.longitude, addedAccident.latitude];

      try {
        const response = await axios.post(
          "https://api.openrouteservice.org/v2/directions/driving-car/geojson",
          { coordinates: [from, to] },
          {
            headers: {
              Authorization: "5b3ce3597851110001cf6248e144b426a65242b68905aa92335e0183",
              "Content-Type": "application/json",
            },
          }
        );

        const route = response.data.features[0];
        const distance = route.properties.summary.distance;
        const duration = route.properties.summary.duration; // ⏱ čas v sekundah

        if (distance < minDistance) {
          minDistance = distance;
          closestStation = station;
          finalPath = route.geometry.coordinates.map(coord => ({
            lng: coord[0],
            lat: coord[1],
          }));
          setTime(duration * 1000); // v milisekundah
        }
      } catch (err) {
        console.error("Napaka pri ORS requestu:", err);
      }
    }


    if (closestStation && finalPath.length) {
      setNewPath(finalPath);
      setBestStation(closestStation);
      console.log("Najbližja postaja:", closestStation);
      console.log("Celotna pot:", finalPath);
    }
  };

  findClosestByRoad();
}, [addedAccident, stations]);

  const [showCheck, setShowCheck] = useState(false);

  const deleteRecentlyAddedStations = () => {
    setStations((prev) =>
      prev.filter((station) => station.region !== "notSpecified")
    );
  };

  const setDeletedTrue = () => {
    setStations((prev) =>
      prev.map((station) => {
        if (station.deleted ) {
          return { ...station, deleted: false };
        }
        return station;
      })
    );
  }

useEffect(() => {
  const storedSimulation = localStorage.getItem("simulation");
  if (storedSimulation) {
    const parsed = JSON.parse(storedSimulation); 
    setCurrentSimulation(parsed);
    setSimulation(parsed._id); 
  }
  console.log("Current simulation from localStorage:", storedSimulation);
}, []);





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
              <div onClick={() => {setSearchingExSimulation(false); setTime(0); setNewPath([]); setShowCheck(false); setCurrentSimulation(null); setDeletedTrue(); setAddedAccident(null); deleteRecentlyAddedStations()}} className={`w-full py-4 px-2 flex flex-col items-center hover:bg-blue-800 transition duration-200`}>
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
{Array.isArray(simulationData) && simulationData.length > 0 && simulationData.map((item, index) => (
  <div
    onClick={() => {
      setNewPath([]);
      setTime(0);
      removeSimulationFromLocalStorage();
      setShowCheck(false);
      setDeletedTrue();
      deleteRecentlyAddedStations();
      setSimulation(item._id);
      setSearchingExSimulation(true);
      setCurrentSimulation(item);
      setAddedAccident(null);
    }}
    key={index}
    className={`px-4 py-3 border-b border-blue-800 cursor-pointer transition duration-200 
      ${simulation === item._id ? 'bg-blue-800 font-bold text-white' : 'hover:bg-blue-800 hover:text-white'}`}
  >
    <span className="text-sm">{item.simulationName}</span>
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
                      onClick={() => { setObject(item.id); setAddedObject(item.type); setAddObject(true)}}
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
                searchingExSimulation={searchingExSimulation}
                currentSimulation={currentSimulation}
                addObject={addObject}
                addedObject={addedObject}
                setAddObject={setAddObject}
                setAddedObject={setAddedObject}
                setStations={setStations}
                newPath={newPath}
              />
              
            </div>
          </div>
        </div>

        <InformationPart setText={setText} setLoading={setLoading}  setSearchingExSimulation={setSearchingExSimulation} setShowCheck={setShowCheck} setDeletedTrue={setDeletedTrue} deleteRecentlyAddedStations={deleteRecentlyAddedStations}  simulation={currentSimulation} newPath={newPath} addedAccident={addedAccident} time={time} setNewPath={setNewPath} setAddedAccident={setAddedAccident} setTime={setTime} setCurrentSimulation={setCurrentSimulation} bestStation={bestStation} />
        {loading && (<Loading text={text} />)}
    </div>
  )
}


