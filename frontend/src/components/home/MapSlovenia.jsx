import React, { useState } from "react";
import {MapContainer,Marker,Polyline,Popup,TileLayer,ZoomControl,useMapEvents} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import { FaCarCrash, FaFire, FaHeartbeat, FaExclamationTriangle, FaHospital, FaShieldAlt, FaFireExtinguisher,} from "react-icons/fa";
import { renderToStaticMarkup } from "react-dom/server";
import { RiCriminalLine } from "react-icons/ri";
import { toast } from "react-toastify";
import axios from "axios";

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
      iconComponent = 
        <div className="w-9 h-9 rounded-full border-2 border-violet-600 bg-none flex items-center justify-center">
          <FaCarCrash color="crimson" size={24} />
        </div>;
      break;
    case "kriminal":

      iconComponent = 
        <div className="w-9 h-9 rounded-full border-2 border-violet-600 bg-none flex items-center justify-center">
          <RiCriminalLine  color="black" size={24} />
        </div>;
      break;
    case "zdravstveni primer":
      iconComponent = 
        <div className="w-9 h-9 rounded-full border-2 border-violet-600 bg-none flex items-center justify-center">
          <FaHeartbeat color="darkgreen" size={24} />
        </div>;
      break;
    case "naravna nesreča":
      iconComponent =  
       <div className="w-9 h-9 rounded-full border-2 border-violet-600 bg-none flex items-center justify-center">
          <FaFire color="#ff6347" className="text-[20px] bg-none"  />
        </div>;
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
function ClickToAddAccident({ setAddedAccident, type, setCheck, searchingExSimulation }) {
  useMapEvents({
    click(e) {
      if(searchingExSimulation) return;
      const { lat, lng } = e.latlng;
      console.log("Clicked coordinates:", lat, lng);
      setAddedAccident({
        latitude: lat,
        longitude: lng,
        type: type,
      });

      setCheck(true);
    },
  });
  return null;
}

function AddStation({ setStations, addedObject, setAddObject, setAddedObject }) {

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
            coordinates: [lng, lat],
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


export default function MapSlovenia({gasilciVidnost,bolniceVidnost,policijaVidnost,stations, addedAccident, setAddedAccident, accidenceTypes, accidenceType, setAccidenceType, showCheck, setShowCheck, searchingExSimulation, currentSimulation, addObject, addedObject, setAddObject, setAddedObject, setStations }) {
  const [selectedStationId, setSelectedStationId] = useState(null);

  const saveAccident = async () => {
    try {
      const response = await axios.post(
        "http://localhost:3002/api/accident/create",
        {
          latitude: addedAccident.latitude,
          longitude: addedAccident.longitude,
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

        {stations?.filter(
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
              station.locationId?.geometry?.coordinates[1],
              station.locationId?.geometry?.coordinates[0],
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
    <div
      className="text-red-600 font-semibold cursor-pointer"
      onClick={() => {
        if (station.region === "notSpecified") {
          setStations((prev) =>
            prev.filter((s) => (s._id || s) !== (station._id || station))
          );
        } else {
          setStations((prev) =>
            prev.map((s) =>
              (s._id || s) === (station._id || station) ? { ...s, deleted: true } : s
            )
          );
        }
        setSelectedStationId(null);
      }}
    >
      Delete ✖
    </div>
  </Popup>
)}

          </Marker>
        ))}


        {currentSimulation?.accidentId?.locationId?.geometry?.coordinates && (
          <Marker
            position={[
              currentSimulation.accidentId.locationId.geometry.coordinates[1],
              currentSimulation.accidentId.locationId.geometry.coordinates[0]
            ]}
            icon={getAccidentIcon(currentSimulation.accidentId.typeOfAccident)}
          />
        )}

        {currentSimulation?.bestPathId?.locationPoints?.length > 0 && (
          <Polyline
            positions={currentSimulation.bestPathId.locationPoints.map((p) => [p.lat, p.lng])}
            pathOptions={{ color: 'red', weight: 4 }}
          />
        )}

        {addObject ? (
          <AddStation setStations={setStations} addedObject={addedObject} setAddObject={setAddObject} setAddedObject={setAddedObject} />
        ) : (
          <ClickToAddAccident setAddedAccident={setAddedAccident} type={accidenceType} setCheck={setShowCheck} searchingExSimulation={searchingExSimulation}/>
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
            <div onClick={saveAccident} className="cursor-pointer absolute top-2 right-3 text-green-600 text-2xl font-semibold select-none">
              ✔
            </div>
          )}
          <div className="grid grid-cols-1 gap-2">
            {accidenceTypes?.map((type) => {
              const isActive = accidenceType === type.type;

              const icon = {
                "prometna": <FaCarCrash className="text-red-500 text-lg" />,
                "kriminal": <RiCriminalLine  className="text-black text-lg" />,
                "zdravstveni primer": <FaHeartbeat className="text-green-600 text-lg" />,
                "naravna nesreča": <FaFire className="text-orange-500 text-lg" />,
              }[type.type];

              return (
                <button
                  key={type.id}
                  onClick={() => {
                    setAccidenceType(type.type);
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
            })}
          </div>
        </div>
      )}
    </div>
  );
}
