import React, { useEffect, useState } from 'react'
import { IoMdDownload } from "react-icons/io";
import { FaCarCrash, FaFire, FaHeartbeat } from "react-icons/fa";
import { RiCriminalLine } from 'react-icons/ri';

export default function InformationPart({ simulation }) {
  const [fromAddress, setFromAddress] = useState('');
  const [toAddress, setToAddress] = useState('');

  const formatTime = (ms) => {
    if (!simulation?.responseTime) return '0h 0min 0sec';
    const totalSeconds = Math.floor(ms / 1000);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;
    return `${hours}h ${minutes}min ${seconds}sec`;
  };

  const getAddressFromCoordinates = async (lat, lon) => {
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json`
      );
      const data = await response.json();
      const addr = data.address;
      return `${addr.road || "Cesta"} ${addr.house_number || ""}, ${addr.city || addr.town || addr.village || ""}`;
    } catch (error) {
      console.error("Napaka pri geokodiranju:", error);
      return `lat:${lat}, lng:${lon}`;
    }
  };

useEffect(() => {
  const fetchAddresses = async () => {
    const from = simulation?.bestPathId?.locationPoints?.[0];
    const toCoords = simulation?.accidentId?.locationId?.geometry?.coordinates;

    if (from && toCoords) {
      const fromAddr = await getAddressFromCoordinates(from.lat, from.lng);
      const toAddr = await getAddressFromCoordinates(toCoords[1], toCoords[0]); // [lat, lon]

      setFromAddress(fromAddr);
      setToAddress(toAddr);
    }
  };

  if (
    simulation?.bestPathId?.locationPoints?.length &&
    simulation?.accidentId?.locationId?.geometry?.coordinates
  ) {
    fetchAddresses();
  }
}, [simulation]);


  const iconByType = {
    "prometna": <FaCarCrash size={50} className="text-red-500" />,
    "kriminal": <RiCriminalLine size={50} className="text-black" />,
    "zdravstveni primer": <FaHeartbeat size={50} className="text-green-600" />,
    "naravna nesreča": <FaFire size={50} className="text-orange-500" />,
  };

  const helpNeeded = {
    "zdravstveni primer": "Rešilci",
    "prometna": "Policija",
    "kriminal": "Policija",
    "naravna nesreča": "Gasilci",
  };

  return (
    <div className="fixed border-[3px] py-[15px] bg-gray-200 border-black bottom-4 left-[130px] shadow-lg p-4 w-[280px] z-50">
      <h1 className="font-bold justify-center flex text-xl mb-1 uppercase">
        {simulation?.simulationName || 'Naslov simulacije'}
      </h1>

      <p className="text-md mb-2">
        Potrebna pomoč: <span className="font-semibold uppercase">
          {helpNeeded[simulation?.accidentId?.typeOfAccident] || "-"}
        </span>
      </p>

      <p className="text-md mb-2">
        Tip nesreče: <span className="font-semibold">
          {simulation?.accidentId?.typeOfAccident || '-'}
        </span>
      </p>

      <div className='w-full flex justify-between'>
        <div>
          <div className="text-lg font-bold text-black mb-2">
            {formatTime(simulation?.responseTime)}
          </div>
          <p className="text-sm mb-1">
            Od: <span className="font-medium">{fromAddress || 'Nalaganje...'}</span>
          </p>
          <p className="text-sm mb-3">
            Do: <span className="font-medium">{toAddress || 'Nalaganje...'}</span>
          </p>
        </div>
        <div className='bg-gray-200 flex items-center justify-center w-[120px]'>
          {iconByType[simulation?.accidentId?.typeOfAccident] || null}
        </div>
      </div>

      <div className="flex justify-between mt-2">
        <button className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded">PREKLIČI</button>
        <button className="bg-gray-800 hover:bg-black text-white px-3 py-1 rounded">SHRANI</button>
        <button className="text-gray-600 hover:text-black" title="Prenesi PDF">
          <IoMdDownload size={30} />
        </button>
      </div>
    </div>
  );
}
