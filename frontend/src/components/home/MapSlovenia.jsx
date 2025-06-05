import React, { useRef, useState } from "react";
import {
  MapContainer,
  Marker,
  Polyline,
  Popup,
  TileLayer,
  ZoomControl,
  useMapEvents,
} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import {
  FaCarCrash,
  FaFire,
  FaHeartbeat,
  FaExclamationTriangle,
  FaHospital,
  FaShieldAlt,
  FaFireExtinguisher,
} from "react-icons/fa";
import { renderToStaticMarkup } from "react-dom/server";
import { RiCriminalLine } from "react-icons/ri";
import { toast } from "react-toastify";
import axios from "axios";
import Loading from "./Loading";

const hospitalIcon = L.divIcon({
  html: renderToStaticMarkup(<FaHospital color="#8B0000" size={22} />),
  iconSize: [30, 30],
  className: "",
});

const policeIcon = L.divIcon({
  html: renderToStaticMarkup(<FaShieldAlt color="#1E3A8A" size={22} />),
  iconSize: [30, 30],
  className: "",
});

const fireIcon = L.divIcon({
  html: renderToStaticMarkup(<FaFireExtinguisher color="orange" size={22} />),
  iconSize: [30, 30],
  className: "",
});

const getAccidentIcon = (type) => {
  let iconComponent;
  switch (type) {
    case "prometna":
      iconComponent = (
        <div className="w-9 h-9 rounded-full border-2 border-violet-600 bg-none flex items-center justify-center">
          <FaCarCrash color="crimson" size={24} />
        </div>
      );
      break;
    case "kriminal":
      iconComponent = (
        <div className="w-9 h-9 rounded-full border-2 border-violet-600 bg-none flex items-center justify-center">
          <RiCriminalLine color="black" size={24} />
        </div>
      );
      break;
    case "zdravstveni primer":
      iconComponent = (
        <div className="w-9 h-9 rounded-full border-2 border-violet-600 bg-none flex items-center justify-center">
          <FaHeartbeat color="darkgreen" size={24} />
        </div>
      );
      break;
    case "naravna nesreča":
      iconComponent = (
        <div className="w-9 h-9 rounded-full border-2 border-violet-600 bg-none flex items-center justify-center">
          <FaFire color="#ff6347" className="text-[20px] bg-none" />
        </div>
      );
      break;
    default:
      iconComponent = <FaExclamationTriangle color="gray" size={24} />;
  }

  return L.divIcon({
    html: renderToStaticMarkup(iconComponent),
    iconSize: [30, 30],
    className: "",
  });
};

// Komponenta za klik na zemljevid
function ClickToAddAccident({
  setAddedAccident,
  type,
  setCheck,
  searchingExSimulation,
}) {
  useMapEvents({
    click(e) {
      if (searchingExSimulation) return;
      const { lat, lng } = e.latlng;
      console.log("Clicked coordinates:", lat, lng);
      setAddedAccident({
        longitude: lng,
        latitude: lat,
        type: type,
      });

      setCheck(true);
    },
  });
  return null;
}

function AddStation({
  setStations,
  addedObject,
  setAddObject,
  setAddedObject,
}) {
  useMapEvents({
    click(e) {
      const { lat, lng } = e.latlng;

      const newStation = {
        isPermanent: false,
        region: "notSpecified",
        typeOfStation: addedObject,
        locationId: {
          geometry: {
            type: "Point",
            coordinates: [lat, lng],
          },
        },
      };

      setStations((prev) => [...prev, newStation]);
      setAddObject(false);
      setAddedObject(null);
    },
  });
  return null;
}

// se tole je treba urediti da bo vrnilo regijo, sedaj sem nastavil Osrednjeslovenska za vse
export const getRegionFromCoordinates = async (lat, lon) => {
  return "Osrednjeslovenska";
};

const saveStation = async (station, setLoading) => {
  const IP = process.env.REACT_APP_IP;
  // setLoading(true);
  try {
    const { geometry } = station.locationId || {};
    const [lng, lat] = geometry?.coordinates || [];
    console.log(geometry, "geometry");
    console.log(station, "station");
    console.log(lng, lat, "lng, lat");

    if (!lat || !lng) {
      toast.error("Lokacija ni pravilno določena.");
      return;
    }

    let region = station.region;
    if (region === "notSpecified") {
      try {
        region = await getRegionFromCoordinates(lat, lng);
      } catch (error) {
        region = "Osrednjeslovenska";
      }
    }

    console.log(lat);

    const locationResponse = await axios.post(
      `http://${IP}/api/location/create`,
      {
        long: lng,
        lat: lat,
      },
      { withCredentials: true }
    );

    const locationId = locationResponse.data?.location?._id;

    if (!locationId) {
      throw new Error("Lokacija ni bila uspešno ustvarjena.");
    }

    const stationResponse = await axios.post(
      `http://${IP}/api/station/create`,
      {
        latitude: lat,
        longitude: lng,
        locationId,
        typeOfStation: station.typeOfStation,
        isPermanent: true,
        region,
      },
      { withCredentials: true }
    );

    toast.success("Postaja uspešno shranjena");
    return stationResponse.data;
  } catch (error) {
    toast.error("Napaka pri shranjevanju postaje");
  } finally {
    // setLoading(false);
  }
};

export default function MapSlovenia({
  setLoading,
  gasilciVidnost,
  bolniceVidnost,
  policijaVidnost,
  stations,
  addedAccident,
  setAddedAccident,
  accidenceTypes,
  accidenceType,
  setAccidenceType,
  showCheck,
  setShowCheck,
  searchingExSimulation,
  currentSimulation,
  addObject,
  addedObject,
  setAddObject,
  setAddedObject,
  setStations,
  newPath,
}) {
  const [selectedStationId, setSelectedStationId] = useState(null);
  const IP = process.env.REACT_APP_IP;
  const inputRefs = useRef({});
  const [selectedTypeForGenerating, setSelectedTypeForGenerating] =
    useState("kriminal");
  const [stationsInRadius, setStationsInRadius] = useState([]);
  const saveAccident = async () => {
    try {
      const response = await axios.post(
        `http://${IP}/api/accident/create`,
        {
          longitude: addedAccident.longitude,
          latitude: addedAccident.latitude,
          type: addedAccident.type,
        },
        { withCredentials: true }
      );

      toast.success("Nesreča uspešno shranjena");
    } catch (error) {
      console.error("Napaka:", error.response?.data || error.message);
      toast.error("Napaka pri shranjevanju nesreče");
    } finally {
      setShowCheck(false);
    }
  };
  function typeOfAccidentToService(typeOfAccident) {
    switch (typeOfAccident) {
      case "prometna":
      case "kriminal":
        return "Policijska";
      case "zdravstveni primer":
        return "Bolnica";
      case "naravna nesreča":
        return "Gasilci";
      default:
        return "Neznano";
    }
  }
  async function handleSubmitGeneratStation() {
    if (!addedAccident) return;

    const accindetCount = inputRefs.current[5]?.value;
    const radius = inputRefs.current[6]?.value;

    if (!accindetCount || !radius) return;
    const selectedPointWithType = {
      //to je tak ker ma martin zamenjane
      long: addedAccident.latitude,
      lat: addedAccident.longitude,
      type: selectedTypeForGenerating,
    };

    try {
      const params = {
        long: selectedPointWithType.long,
        lat: selectedPointWithType.lat,
        radius: radius,
        type: typeOfAccidentToService(selectedPointWithType.type),
      };
      const response = await axios.get(
        `http://${IP}/api/station/getAllStationsInRadius`,
        {
          params,
        }
      );
      if (response.status == 200) {
        
        console.log(response.data.stations);
        setStationsInRadius(response.status.stations);
        try {
          const resForAccidents = await axios.post(
            `http://${IP}/api/accident/generateRandomInRadius`,
            {
              long: params.long,
              lat: params.lat,
              radius,
              count: accindetCount,
              type: selectedTypeForGenerating,
            }
          );
          if (resForAccidents.status == 200) {
            console.log(resForAccidents.data);
          }
        } catch (err) {
          console.log("ERROR creating n times accidents", err);
        }
      }
    } catch (err) {
      console.log("ERRROR geting station in radius: ", err);
    } finally {
      if (inputRefs.current[5]) inputRefs.current[5].value = "";
      if (inputRefs.current[6]) inputRefs.current[6].value = "";
    }
  }

  return (
    <div style={{ height: "100%", width: "100%" }}>
      <MapContainer
        center={[46.1512, 14.6955]}
        zoom={9}
        scrollWheelZoom={true}
        zoomControl={false}
        style={{ height: "100%", width: "100%" }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {stations
          ?.filter(
            (station) =>
              (station.typeOfStation === "Bolnica" && bolniceVidnost) ||
              (station.typeOfStation === "Policijska" && policijaVidnost) ||
              (station.typeOfStation === "Gasilci" && gasilciVidnost)
          )
          .filter((station) => !station.deleted)
          .map((station, index) => (
            <Marker
              key={station._id || index}
              position={[
                station.locationId?.geometry?.coordinates[0], //TUUU
                station.locationId?.geometry?.coordinates[1],
              ]}
              icon={
                station.typeOfStation === "Bolnica"
                  ? hospitalIcon
                  : station.typeOfStation === "Policijska"
                  ? policeIcon
                  : fireIcon
              }
              eventHandlers={{
                click: () => setSelectedStationId(station._id || index),
              }}
            >
              {selectedStationId === (station._id || index) && (
                <Popup
                  position={[
                    station.locationId?.geometry?.coordinates[1],
                    station.locationId?.geometry?.coordinates[0],
                  ]}
                  onClose={() => setSelectedStationId(null)}
                  closeButton={false}
                  closeOnClick={false}
                  autoPan={false}
                >
                  {station.region === "notSpecified" ? (
                    <div className="flex flex-col items-start gap-2">
                      <button
                        onClick={async () => {
                          const saved = await saveStation(station, setLoading);
                          if (saved?._id) {
                            setStations((prev) =>
                              prev.map((s) =>
                                (s._id || s) === (station._id || station)
                                  ? { ...saved, isPermanent: true }
                                  : s
                              )
                            );
                            setSelectedStationId(null);
                          }
                        }}
                        className="px-2 py-1 text-sm text-green-500 hover:text-green-600 rounded"
                      >
                        Save ✔
                      </button>
                      <button
                        onClick={() => {
                          setStations((prev) =>
                            prev.filter(
                              (s) => (s._id || s) !== (station._id || station)
                            )
                          );
                          setSelectedStationId(null);
                        }}
                        className="px-2 py-1 text-sm text-red-600 hover:text-red-800"
                      >
                        Delete ✖
                      </button>
                    </div>
                  ) : (
                    <button
                      onClick={() => {
                        setStations((prev) =>
                          prev.map((s) =>
                            (s._id || s) === (station._id || station)
                              ? { ...s, deleted: true }
                              : s
                          )
                        );
                        setSelectedStationId(null);
                      }}
                      className="px-2 py-1 text-sm text-red-600 hover:text-red-800"
                    >
                      Delete ✖
                    </button>
                  )}
                </Popup>
              )}
            </Marker>
          ))}

        {currentSimulation?.accidentId?.locationId?.geometry?.coordinates && (
          <Marker
            position={[
              currentSimulation.accidentId.locationId.geometry.coordinates[1],
              currentSimulation.accidentId.locationId.geometry.coordinates[0],
            ]}
            icon={getAccidentIcon(currentSimulation.accidentId.typeOfAccident)}
          />
        )}

        {newPath?.length > 0 ? (
          <Polyline
            positions={newPath.map((p) => [p.lat, p.lng])}
            pathOptions={{ color: "blue", weight: 4 }}
          />
        ) : (
          currentSimulation?.bestPathId?.locationPoints?.length > 0 && (
            <Polyline
              positions={currentSimulation.bestPathId.locationPoints.map(
                (p) => [p.lat, p.lng]
              )}
              pathOptions={{ color: "red", weight: 4 }}
            />
          )
        )}

        {addObject ? (
          <AddStation
            setStations={setStations}
            addedObject={addedObject}
            setAddObject={setAddObject}
            setAddedObject={setAddedObject}
          />
        ) : (
          <ClickToAddAccident
            setAddedAccident={setAddedAccident}
            type={accidenceTypes.length >= 5 ? null : accidenceType}
            setCheck={setShowCheck}
            searchingExSimulation={searchingExSimulation}
          />
        )}

        {Number.isFinite(addedAccident?.latitude) &&
          Number.isFinite(addedAccident?.longitude) && (
            <Marker
              position={[addedAccident.latitude, addedAccident.longitude]}
              icon={getAccidentIcon(addedAccident.type)}
            />
          )}

        <ZoomControl position="bottomright" />
      </MapContainer>
      {!searchingExSimulation && (
        <div className="absolute bottom-10 right-12 z-[1000] bg-white/80 backdrop-blur-md p-5 rounded-xl shadow-lg w-72 border border-gray-200">
          <h3 className="text-sm font-semibold text-gray-800 mb-4">
            Izberi vrsto nesreče
          </h3>

          {showCheck && (
            <div
              onClick={saveAccident}
              className="cursor-pointer absolute top-2 right-3 text-green-600 text-2xl font-semibold select-none"
            >
              ✔
            </div>
          )}
          <div className="grid grid-cols-1 gap-2">
            {accidenceTypes?.map((type) => {
              const isActive = accidenceType === type.type;
              if (type.id >= 5) {
                return (
                  <div key={type.id} className="flex items-center gap-1 mb-2">
                    <label className="text-sm font-medium min-w-[80px]">
                      {type.type}:
                    </label>
                    <input
                      type="text"
                      ref={(el) => (inputRefs.current[type.id] = el)}
                      className="flex-1 px-2 py-1 border rounded text-sm max-w-[160px]"
                    />
                  </div>
                );
              } else {
                const icon = {
                  prometna: <FaCarCrash className="text-red-500 text-lg" />,
                  kriminal: <RiCriminalLine className="text-black text-lg" />,
                  "zdravstveni primer": (
                    <FaHeartbeat className="text-green-600 text-lg" />
                  ),
                  "naravna nesreča": (
                    <FaFire className="text-orange-500 text-lg" />
                  ),
                }[type.type];

                return (
                  <button
                    key={type.id}
                    onClick={() => {
                      setAccidenceType(type.type);
                      setSelectedTypeForGenerating(type.type);
                      console.log(type.type);
                      setAddedAccident(null);
                      setShowCheck(false);
                    }}
                    className={`flex items-center gap-2 px-3 py-2 rounded-md border transition text-sm font-medium w-full text-left
                    ${
                      isActive
                        ? "bg-gray-700 text-white border-gray-700 shadow"
                        : "bg-white text-gray-800 border-gray-300 hover:bg-gray-100"
                    }`}
                  >
                    {icon}
                    <span>{type.type}</span>
                  </button>
                );
              }
            })}
            {accidenceTypes.length > 4 ? (
              <button
                onClick={handleSubmitGeneratStation}
                className="mt-2 w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-4 rounded shadow transition duration-150"
              >
                SUBMIT
              </button>
            ) : null}
          </div>
        </div>
      )}
    </div>
  );
}
