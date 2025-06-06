import { useEffect, useState } from "react";
import Navigation from "./Navigation";
import { TbMapStar } from "react-icons/tb";
import { FaPlus } from "react-icons/fa";
import { FaHospitalSymbol } from "react-icons/fa";
import { GrUserPolice } from "react-icons/gr";
import { MdOutlineFireTruck } from "react-icons/md";
import { FaRegEye } from "react-icons/fa";
import { FaEyeSlash } from "react-icons/fa";
import MapSlovenia from "./MapSlovenia";
import InformationPart from "./InformationPart";
import { CiLogout } from "react-icons/ci";
import { toast } from "react-toastify";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { MdAutorenew } from "react-icons/md";
import Loading from "./Loading";
import { VscLayoutSidebarRight } from "react-icons/vsc";
import { BsChevronBarRight, BsChevronBarLeft } from "react-icons/bs";
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import { MdOutlineLightbulb } from "react-icons/md";

export default function Homepage() {
  const IP = process.env.REACT_APP_IP;

  const [text, setText] = useState("Loading");

  const [searchingExSimulation, setSearchingExSimulation] = useState(false);

  const [loading, setLoading] = useState(false);

  const [simulation, setSimulation] = useState(1);
  const [object, setObject] = useState(1);
  const navigate = useNavigate();

  const [addObject, setAddObject] = useState(false);
  const [addedObject, setAddedObject] = useState("bolnica");

  const [bolniceVidnost, setBolniceVidnost] = useState(true);
  const [policijaVidnost, setPolicijaVidnost] = useState(true);
  const [gasilciVidnost, setGasilciVidnost] = useState(true);
  const [hoveredMenu, setHoveredMenu] = useState(null);

  const [editingId, setEditingId] = useState(null);
  const [newNameValue, setNewNameValue] = useState("");
  const [time, setTime] = useState(0);
  const [newPath, setNewPath] = useState([]); // tole je za sloveniaMaps
  const [addedAccident, setAddedAccident] = useState(null);
  const [accidenceType, setAccidenceType] = useState("kriminal");
  const [fiveNearestStations, setFiveNearesStations] = useState([]);

  const [allAccidents, setAllAccidents] = useState([]);
  const [currentSimulation, setCurrentSimulation] = useState(null);
  const [bestStation, setBestStation] = useState(null);

  const [simulationData, setSimulationData] = useState([]);
  const [isOpen, setIsOpen] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [simulationToDelete, setSimulationToDelete] = useState(null);
  const [showCheck, setShowCheck] = useState(false);
  const [showInfo, setShowInfo] = useState(true);
  const [accidenceTypes, setAccidenceTypes] = useState([
    { id: 1, type: "prometna" },
    { id: 2, type: "kriminal" },
    { id: 3, type: "zdravstveni primer" },
    { id: 4, type: "naravna nesreča" },
  ]);

  const removeSimulationFromLocalStorage = () => {
    localStorage.removeItem("simulation");
  };

  useEffect(() => {
    const fetchSimulations = async () => {
      setLoading(true);
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
      await axios.post(
        `http://${IP}/api/user/logout`,
        {},
        { withCredentials: true }
      );
      console.log("Token cookie cleared");
      toast.success("Logged out");
      navigate("/login");
    } catch (err) {
      toast.error("Logout failed");
    }
  };

  const addObjectData = [
    {
      id: 1,
      icon: <FaHospitalSymbol color="yellow" size={50} />,
      type: "Bolnica",
    },
    {
      id: 2,
      icon: <GrUserPolice color="blue" size={50} />,
      type: "Policijska",
    },
    {
      id: 3,
      icon: <MdOutlineFireTruck color="orange" size={50} />,
      type: "Gasilci",
    },
  ];

  const [stations, setStations] = useState([]);
  useEffect(() => {
    const fetchStations = async () => {
      try {
        const response = await axios.get(`http://${IP}/api/station/all`, {
          withCredentials: true,
        });
        setStations(response.data);
        // console.log(response.data);
        // console.log("hi");
      } catch (error) {
        console.error("Error fetching stations:", error);
        toast.error("Error fetching stations");
      }
    };
    fetchStations();
  }, []);

  useEffect(() => {
    const findClosestByRoad = async () => {
      if (!addedAccident) return;

      const accidentToStationType = {
        prometna: "Policijska",
        kriminal: "Policijska",
        "zdravstveni primer": "Bolnica",
        "naravna nesreča": "Gasilci",
      };
      // console.log(addedAccident, "MIHAAA2222---------")
      try {
        const res = await axios.get(
          `http://${IP}/api/station/getFiveNearestStations/${
            addedAccident.latitude
          }/${addedAccident.longitude}/${
            accidentToStationType[addedAccident.type]
          }`
        );
        const nearest = res.data.nearestStations;
        setFiveNearesStations(nearest);

        let closestStation = null;
        let minDistance = Infinity;
        let finalPath = [];

        for (const station of nearest) {
          const coords = station.locationId?.geometry?.coordinates;
          if (!coords || coords.length !== 2) continue;

          const from = [coords[1], coords[0]];
          const to = [addedAccident.longitude, addedAccident.latitude];
          try {
            const response = await axios.post(
              "https://api.openrouteservice.org/v2/directions/driving-car/geojson",
              { coordinates: [from, to] },
              {
                headers: {
                  Authorization:
                    "5b3ce3597851110001cf624801f5f69a289c476da754157ff8ab9298",
                  "Content-Type": "application/json",
                },
              }
            );

            const route = response.data.features[0];
            const distance = route.properties.summary.distance;
            const duration = route.properties.summary.duration;

            if (distance < minDistance) {
              minDistance = distance;
              closestStation = station;
              finalPath = route.geometry.coordinates.map((coord) => ({
                lng: coord[0],
                lat: coord[1],
              }));
              setTime(duration * 1000);
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
      } catch (err) {
        console.log(err);
      }
    };

    findClosestByRoad();
  }, [addedAccident]);

  const deleteRecentlyAddedStations = () => {
    setStations((prev) =>
      prev.filter((station) => station.region !== "notSpecified")
    );
  };

  const setDeletedTrue = () => {
    setStations((prev) =>
      prev.map((station) => {
        if (station.deleted) {
          return { ...station, deleted: false };
        }
        return station;
      })
    );
  };

  useEffect(() => {
    const storedSimulation = localStorage.getItem("simulation");
    if (storedSimulation) {
      const parsed = JSON.parse(storedSimulation);
      setCurrentSimulation(parsed);
      setSimulation(parsed._id);
    }
    console.log("Current simulation from localStorage:", storedSimulation);
  }, []);

  const simulationNameChange = async (id, newName) => {
    try {
      await axios.put(
        `http://${IP}/api/simulation/change-name`,
        {
          simulationId: id,
          newName,
        },
        { withCredentials: true }
      );

      toast.success("Ime simulacije posodobljeno");

      setSimulationData((prev) =>
        prev.map((sim) =>
          sim._id === id ? { ...sim, simulationName: newName } : sim
        )
      );
      window.location.reload();
    } catch (error) {
      toast.error("Napaka pri spreminjanju imena simulacije");
    }
  };

  const deleteSimulation = async (id) => {
    setLoading(true);
    try {
      await axios.delete(`http://${IP}/api/simulation/delete/${id}`, {
        withCredentials: true,
      });
      toast.success("Simulacija izbrisana");
      setSimulationData((prev) => prev.filter((sim) => sim._id !== id));
    } catch (error) {
      toast.error("Napaka pri brisanju simulacije");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const fetchAllAccidents = async () => {
      try {
        const response = await axios.get(`http://${IP}/api/accident/all`, {
          withCredentials: true,
        });
        setAllAccidents(response.data);
      } catch (error) {
        console.error("Napaka pri pridobivanju nesreč:", error);
      }
    };

    fetchAllAccidents();
  }, []);

  return (
    <>
      <div className="w-full">
        <Navigation />

        <div className="flex">
          <div className="w-[100px] z-10 h-[calc(100vh-64px)] top-[70px] fixed bg-blue-900 text-white shadow-md flex flex-col justify-between cursor-pointer">
            <div>
              <div
                onMouseEnter={() => setHoveredMenu("simulacije")}
                onMouseLeave={() => setHoveredMenu(null)}
                className={`w-full py-4 px-2 flex flex-col items-center hover:bg-blue-800 transition duration-200 ${
                  hoveredMenu === "simulacije" && "bg-blue-800 font-bold"
                }`}
              >
                <TbMapStar size={30} />
                <span className="text-sm mt-1 text-center">Simulacije</span>
              </div>
              <div
                onMouseEnter={() => setHoveredMenu("dodaj")}
                onMouseLeave={() => setHoveredMenu(null)}
                className={`w-full py-4 px-2 flex flex-col items-center hover:bg-blue-800 transition duration-200 ${
                  hoveredMenu === "dodaj" && "bg-blue-800 font-bold"
                }`}
              >
                <FaPlus size={30} />
                <span className="text-sm mt-1 text-center">Dodaj</span>
              </div>
              <div
                onClick={() => {
                  setSearchingExSimulation(false);
                  setTime(0);
                  setNewPath([]);
                  setShowCheck(false);
                  setCurrentSimulation(null);
                  setDeletedTrue();
                  setAddedAccident(null);
                  deleteRecentlyAddedStations();
                }}
                className={`w-full py-4 px-2 flex flex-col items-center hover:bg-blue-800 transition duration-200`}
              >
                <MdAutorenew size={30} />
                <span className="text-sm mt-1 text-center">
                  Nova simulacija
                </span>
              </div>
              <div
                className={`w-full py-4 px-2 flex flex-col items-center hover:bg-blue-800 transition duration-200`}
              >
                <MdOutlineLightbulb size={30} />{" "}
                <span
                  className="text-sm mt-1 text-center"
                  onClick={() => {
                    setShowInfo((prev) => {
                      if (prev) {
                        setAccidenceTypes([
                          ...accidenceTypes,
                          { id: 5, type: "Št nesreč" },
                          { id: 6, type: "Radius (m)" },
                        ]);
                      } else {
                        setAccidenceTypes(accidenceTypes.slice(0, -2));
                      }
                      return !prev;
                    });
                  }}
                >
                  Predlog za postajo
                </span>
              </div>
            </div>
            <div
              onClick={handleLogout}
              className="w-full py-4 px-2 flex flex-col items-center hover:bg-blue-800 transition duration-200"
            >
              <CiLogout size={30} />
              <span className="text-sm mt-1 text-center">Logout</span>
            </div>
          </div>

          {hoveredMenu === "simulacije" && (
            <div
              className="fixed left-[100px] top-[70px] w-[220px] bg-blue-950 text-white shadow-lg overflow-y-auto max-h-[400px] z-20 rounded-r-md"
              onMouseEnter={() => setHoveredMenu("simulacije")}
              onMouseLeave={() => setHoveredMenu(null)}
            >
              {Array.isArray(simulationData) &&
                simulationData.length > 0 &&
                simulationData.map((item, index) => (
                  <div
                    key={index}
                    onClick={() => {
                      if (editingId === item._id) return;
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
                    className={`px-4 py-3 border-b border-blue-800 cursor-pointer flex justify-between items-center transition duration-200 
      ${
        simulation === item._id
          ? "bg-blue-800 font-bold text-white"
          : "hover:bg-blue-800 hover:text-white"
      }`}
                  >
                    {editingId === item._id ? (
                      <input
                        value={newNameValue}
                        onChange={(e) => setNewNameValue(e.target.value)}
                        onClick={(e) => e.stopPropagation()}
                        onKeyDown={async (e) => {
                          if (e.key === "Enter") {
                            simulationNameChange(item._id, newNameValue);
                            setEditingId(null);
                          }
                          if (e.key === "Escape") {
                            setEditingId(null);
                          }
                        }}
                        className="bg-blue-900 text-white px-2 py-1 text-sm border border-white rounded w-full max-w-[140px]"
                        autoFocus
                      />
                    ) : (
                      <span className="text-sm truncate max-w-[140px]">
                        {item.simulationName}
                      </span>
                    )}

                    <div className="flex gap-2 items-center text-base ml-4">
                      <span
                        title="Uredi ime"
                        onClick={(e) => {
                          e.stopPropagation();
                          setEditingId(item._id);
                          setNewNameValue(item.simulationName);
                        }}
                        className="hover:text-yellow-300"
                      >
                        ✏️
                      </span>
                      <span
                        title="Izbriši simulacijo"
                        onClick={(e) => {
                          e.stopPropagation();
                          setSimulationToDelete(item._id);
                          setShowDeleteModal(true);
                        }}
                        className="hover:text-red-400"
                      >
                        🗑️
                      </span>
                    </div>
                  </div>
                ))}
            </div>
          )}

          {hoveredMenu === "dodaj" && (
            <div
              className="fixed left-[100px] top-[156px] w-[90px] bg-blue-950 text-white shadow-lg max-h-[400px] z-20 rounded-r-md"
              onMouseEnter={() => setHoveredMenu("dodaj")}
              onMouseLeave={() => setHoveredMenu(null)}
            >
              {addObjectData.map((item, index) => (
                <div
                  onClick={() => {
                    setObject(item.id);
                    setAddedObject(item.type);
                    setAddObject(true);
                  }}
                  key={index}
                  className={`p-3 border-b border-blue-800 flex items-center justify-center cursor-pointer transition duration-200
                        ${
                          object === item.id
                            ? "bg-blue-800"
                            : "hover:bg-blue-800"
                        }`}
                >
                  {item.icon}
                </div>
              ))}
            </div>
          )}

          <div className="flex-1 bg-white p-2 z-1">
            <div className="fixed space-y-2 top-[80px] left-[120px] z-10 bg-gray-300 bg-opacity-70 rounded-md px-4 py-2 shadow-md">
              <div
                onClick={() => setBolniceVidnost(!bolniceVidnost)}
                className={`${
                  !bolniceVidnost && "text-gray-600"
                } flex items-center gap-2 cursor-pointer font-bold text-[25px]`}
              >
                {bolniceVidnost ? (
                  <FaRegEye className="mt-1" />
                ) : (
                  <FaEyeSlash className="mt-1" />
                )}{" "}
                bolnice
              </div>
              <div
                onClick={() => setPolicijaVidnost(!policijaVidnost)}
                className={`${
                  !policijaVidnost && "text-gray-600"
                } flex items-center gap-2 cursor-pointer font-bold text-[25px]`}
              >
                {policijaVidnost ? (
                  <FaRegEye className="mt-1" />
                ) : (
                  <FaEyeSlash className="mt-1" />
                )}{" "}
                policijske postaje
              </div>
              <div
                onClick={() => setGasilciVidnost(!gasilciVidnost)}
                className={`${
                  !gasilciVidnost && "text-gray-600"
                } flex items-center gap-2 cursor-pointer font-bold text-[25px]`}
              >
                {gasilciVidnost ? (
                  <FaRegEye className="mt-1" />
                ) : (
                  <FaEyeSlash className="mt-1" />
                )}{" "}
                gasilske postaje
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
        {showInfo ? (
          <InformationPart
            setText={setText}
            setLoading={setLoading}
            setSearchingExSimulation={setSearchingExSimulation}
            setShowCheck={setShowCheck}
            setDeletedTrue={setDeletedTrue}
            deleteRecentlyAddedStations={deleteRecentlyAddedStations}
            simulation={currentSimulation}
            newPath={newPath}
            addedAccident={addedAccident}
            time={time}
            setNewPath={setNewPath}
            setAddedAccident={setAddedAccident}
            setTime={setTime}
            setCurrentSimulation={setCurrentSimulation}
            bestStation={bestStation}
          />
        ) : null}

        {loading && <Loading text={text} />}
      </div>

      {showDeleteModal && (
        <div className="fixed inset-0  bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-[3px] border-black rounded-md  shadow-lg p-4 w-[300px]">
            <p className="text-gray-700 font-semibold mb-6">
              Ali res želiš izbrisati simulacijo?
            </p>
            <div className="flex justify-between">
              <button
                onClick={() => {
                  setShowDeleteModal(false);
                  setSimulationToDelete(null);
                }}
                className="px-4 py-2 font-semibold text-sm rounded bg-gray-300 hover:bg-gray-400 transition"
              >
                Prekliči
              </button>
              <button
                onClick={() => {
                  deleteSimulation(simulationToDelete);
                  setShowDeleteModal(false);
                  setSimulationToDelete(null);
                }}
                className="px-4 py-2 text-sm rounded bg-red-500 font-semibold text-white hover:bg-red-600 transition"
              >
                {loading ? (
                  <span className="animate-spin w-4 h-4 border-2 border-t-transparent rounded-full"></span>
                ) : (
                  "Izbriši"
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      <>
        <div
          className={`fixed top-20 right-0 h-[550px] w-[350px] bg-black shadow-lg z-40 transform transition-transform duration-300 ${
            isOpen ? "translate-x-0" : "translate-x-[330px]"
          }`}
        >
          <div
            onClick={() => setIsOpen(!isOpen)}
            className="absolute top-0 left-0 h-full w-[20px] bg-blue-950 cursor-pointer flex items-center justify-center text-white font-bold text-lg select-none"
            title={isOpen ? "Zapri" : "Odpri"}
          >
            {isOpen ? <BsChevronBarRight /> : <BsChevronBarLeft />}
          </div>

          <div className="py-3">
            <div className="text-white">
              <ResponsiveContainer width="100%" height={250}>
                <PieChart>
                  <Pie
                    data={[
                      {
                        type: "Policija",
                        value: stations.filter(
                          (s) => s.typeOfStation === "Policijska"
                        ).length,
                      },
                      {
                        type: "Bolnišnice",
                        value: stations.filter(
                          (s) => s.typeOfStation === "Bolnica"
                        ).length,
                      },
                      {
                        type: "Gasilci",
                        value: stations.filter(
                          (s) => s.typeOfStation === "Gasilci"
                        ).length,
                      },
                    ]}
                    dataKey="value"
                    nameKey="type"
                    cx="50%"
                    cy="50%"
                    outerRadius={80}
                    label
                  >
                    <Cell fill="#3b82f6" /> {/* modra za Policija */}
                    <Cell fill="#10b981" /> {/* zelena za Bolnišnice */}
                    <Cell fill="#f59e0b" /> {/* oranžna za Gasilci */}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="py-4 text-white">
            <ResponsiveContainer width="100%" height={250}>
              <PieChart>
                <Pie
                  data={[
                    {
                      type: "Prometna",
                      value: allAccidents.filter(
                        (a) => a.typeOfAccident === "prometna"
                      ).length,
                    },
                    {
                      type: "Kriminal",
                      value: allAccidents.filter(
                        (a) => a.typeOfAccident === "kriminal"
                      ).length,
                    },
                    {
                      type: "Zdravstveni primer",
                      value: allAccidents.filter(
                        (a) => a.typeOfAccident === "zdravstveni primer"
                      ).length,
                    },
                    {
                      type: "Naravna nesreča",
                      value: allAccidents.filter(
                        (a) => a.typeOfAccident === "naravna nesreča"
                      ).length,
                    },
                  ]}
                  dataKey="value"
                  nameKey="type"
                  cx="50%"
                  cy="50%"
                  innerRadius={50}
                  outerRadius={80}
                  paddingAngle={3}
                  label
                >
                  <Cell fill="#ef4444" />
                  <Cell fill="#3b82f6" />
                  <Cell fill="#10b981" />
                  <Cell fill="#f59e0b" />
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        {!isOpen && (
          <button
            onClick={() => setIsOpen(true)}
            className="fixed bottom-6 right-6 z-50 bg-white text-blue-600 border border-blue-500 shadow-md p-3 rounded-full hover:bg-blue-50 transition"
            title="Odpri nastavitve"
          >
            <VscLayoutSidebarRight size={24} />
          </button>
        )}
      </>
    </>
  );
}
