import React, { useState } from "react";
import {MapContainer,Marker,TileLayer,ZoomControl,useMapEvents} from "react-leaflet";
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
      iconComponent = <FaCarCrash color="crimson" size={24} />;
      break;
    case "kriminal":
      iconComponent = <RiCriminalLine  color="black" size={24} />;
      break;
    case "zdravstveni primer":
      iconComponent = <FaHeartbeat color="darkgreen" size={24} />;
      break;
    case "naravna nesreča":
      iconComponent = <FaFire color="orange" size={24} />;
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
function ClickToAddAccident({ setAddedAccident, type, setCheck }) {
  useMapEvents({
    click(e) {
      const { lat, lng } = e.latlng;
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

export default function MapSlovenia({gasilciVidnost,bolniceVidnost,policijaVidnost,stations, addedAccident, setAddedAccident, accidenceTypes, accidenceType, setAccidenceType, showCheck, setShowCheck}) {


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
              (station.type === "Bolnica" && bolniceVidnost) ||
              (station.type === "Policijska" && policijaVidnost) ||
              (station.type === "Gasilci" && gasilciVidnost)
          )
          .map((station, index) => (
            <Marker
              key={index}
              position={[station.latitude, station.longitude]}
              icon={
                station.type === "Bolnica"
                  ? hospitalIcon
                  : station.type === "Policijska"
                  ? policeIcon
                  : fireIcon
              }
            />
          ))}

        {/* Dodajanje ali posodabljanje nesreče */}
        <ClickToAddAccident setAddedAccident={setAddedAccident} type={accidenceType} setCheck={setShowCheck} />

        {Number.isFinite(addedAccident?.latitude) &&
          Number.isFinite(addedAccident?.longitude) && (
            <Marker
              position={[addedAccident.latitude, addedAccident.longitude]}
              icon={getAccidentIcon(addedAccident.type)}
            />
          )}

        <ZoomControl position="bottomright" />
      </MapContainer>

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


    </div>
  );
}
