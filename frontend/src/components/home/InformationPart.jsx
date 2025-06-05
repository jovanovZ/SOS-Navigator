import React, { useEffect, useState } from "react";
import { IoMdDownload } from "react-icons/io";
import { FaCarCrash, FaFire, FaHeartbeat } from "react-icons/fa";
import { RiCriminalLine } from "react-icons/ri";
import axios from "axios";
import { toast } from "react-toastify";

import {
  Page,
  Text,
  View,
  Document,
  StyleSheet,
  Image,
  pdf,
} from "@react-pdf/renderer";

const pathIcon =
  "https://static.vecteezy.com/system/resources/previews/009/836/363/non_2x/dashed-line-arrow-free-png.png";
const ambulanceIcon = "https://cdn-icons-png.flaticon.com/512/9154/9154206.png";
const firefighterIcon =
  "https://cdn-icons-png.flaticon.com/512/6734/6734073.png";
const policeIcon = "https://cdn-icons-png.flaticon.com/512/386/386437.png";

const clockIcon =
  "https://static.vecteezy.com/system/resources/thumbnails/050/757/348/small_2x/clock-icon-transparent-background-free-png.png";

const accidentIcons = {
  prometna:
    "https://png.pngtree.com/png-vector/20220722/ourmid/pngtree-car-crash-icon-png-png-image_6033718.png",
  kriminal: "https://cdn-icons-png.flaticon.com/512/7902/7902313.png",
  "zdravstveni primer": "https://static.thenounproject.com/png/204885-200.png",
  "naravna nesreča":
    "https://www.pngarts.com/files/9/House-On-Fire-PNG-Transparent-Image.png",
};

export default function InformationPart({
  simulation,
  newPath,
  addedAccident,
  time,
  setNewPath,
  setTime,
  setAddedAccident,
  bestStation,
  setSearchingExSimulation,
  setShowCheck,
  setCurrentSimulation,
  setDeletedTrue,
  deleteRecentlyAddedStations,
  setSaveSimulationLoading,
  setText,
  setLoading
}) {
  const IP = process.env.REACT_APP_IP;
  const [fromAddress, setFromAddress] = useState("");
  const [toAddress, setToAddress] = useState("");
  // console.log("SimulationTime:", time);

  const formatTime = (ms) => {
    if (!ms || ms === 0) return "0h 0min 0sec";
    const totalSeconds = Math.floor(ms / 1000);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;
    return `${hours}h ${minutes}min ${seconds}sec`;
  };

  const saveSimulation = async () => {
    setLoading(true);
    setText("Saving simulation")
    try {
      const user = JSON.parse(localStorage.getItem("user"));
      const userId = user?._id || user?.id;

      if (!userId || !bestStation || !newPath.length || !addedAccident) {
        toast.error("Manjkajo podatki za shranjevanje simulacije.");
        return;
      }

      const accidentRes = await axios.post(
        `http://${IP}/api/accident/create`,
        {
          latitude: addedAccident.latitude,
          longitude: addedAccident.longitude,
          type: addedAccident.type,
        },
        { withCredentials: true }
      );
      console.log("Nesreča uspešno ustvarjena");
      const accidentId = accidentRes.data?.accident?.id;

      const pathRes = await axios.post(
        `http://${IP}/api/path/create`,
        {
          accidentId,
          locationPoints: newPath,
        },
        { withCredentials: true }
      );
      console.log("Pot uspešno ustvarjena");
      const pathId = pathRes.data?.path?._id;

      const locationFrom = `${newPath[0].lat}, ${newPath[0].lng}`;
      const locationTo = `${addedAccident.latitude}, ${addedAccident.longitude}`;

      await axios.post(
        `http://${IP}/api/simulation/create`,
        {
          userId,
          simulationName: `Simulacija - ${new Date().toLocaleString()}`,
          bestStationId: bestStation._id,
          bestPathId: pathId,
          accidentId,
          responseTime: time,
          typeOfServices: bestStation.typeOfStation,
          locationFrom,
          locationTo,
        },
        { withCredentials: true }
      );

      toast.success("Simulacija uspešno shranjena.");

      setNewPath([]);
      setTime(0);
      setAddedAccident(null);
      setSearchingExSimulation(false);
      setShowCheck(false);
      setCurrentSimulation(null);
      setDeletedTrue();
      deleteRecentlyAddedStations();
    } catch (error) {
      console.error("Napaka pri shranjevanju simulacije:", error);
      toast.error("Napaka pri shranjevanju simulacije.");
    } finally {
      setLoading(false)
      setText("Loading")
    }
  };

  const SimulationPDF = ({
    simulationName,
    fromAddress,
    toAddress,
    time,
    accidentType,
    helpService,
  }) => {
    const borderColor =
      {
        Policija: "#3b82f6",
        Rešilci: "#f9a8d4",
        Gasilci: "#f97316",
      }[helpService] || "#6b7280";

    const helpIcons = {
      Policija: policeIcon,
      Rešilci: ambulanceIcon,
      Gasilci: firefighterIcon,
    };

    const helpIcon = helpIcons[helpService];
    const endIcon = accidentIcons[accidentType];

    const styles = StyleSheet.create({
      page: {
        padding: 30,
        border: `8px solid ${borderColor}`,
        fontFamily: "Courier",
        fontSize: 12,
      },
      title: {
        fontSize: 22,
        marginBottom: 30,
        textAlign: "center",
        fontWeight: "bold",
        textTransform: "uppercase",
      },
      addressLabel: {
        textAlign: "center",
        fontSize: 14,
        fontWeight: "bold",
        marginVertical: 5,
      },
      iconsRow: {
        flexDirection: "row",
        justifyContent: "center",
        alignItems: "center",
        marginVertical: 10,
      },
      icon: {
        width: 110,
        height: 110,
      },
      path: {
        width: 200,
        height: 60,
        marginHorizontal: 15,
      },
      centerTime: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "center",
        marginTop: 40,
      },
      clockIcon: {
        width: 60,
        height: 60,
        marginRight: 10,
      },
      timeText: {
        fontSize: 20,
        fontWeight: "bold",
      },
    });

    return (
      <Document>
        <Page size="A4" style={styles.page}>
          <Text style={styles.title}>{simulationName}</Text>

          <Text style={styles.addressLabel}>od: {fromAddress}</Text>

          <View style={styles.iconsRow}>
            <Image src={helpIcon} style={styles.icon} />
            <Image src={pathIcon} style={styles.path} />
            <Image src={endIcon} style={styles.icon} />
          </View>

          <Text style={styles.addressLabel}>do: {toAddress}</Text>

          <View style={styles.centerTime}>
            <Image src={clockIcon} style={styles.clockIcon} />
            <Text style={styles.timeText}>
              {Math.floor(time / 3600000)}h{" "}
              {Math.floor((time % 3600000) / 60000)}min{" "}
              {Math.floor((time % 60000) / 1000)}sec
            </Text>
          </View>
        </Page>
      </Document>
    );
  };

  const createPdf = async (
    simulation,
    fromAddress,
    toAddress,
    addedAccident
  ) => {
    const time = simulation?.responseTime || 0;
    const simulationName = simulation?.simulationName || "Simulacija";
    const accidentType =
      simulation?.accidentId?.typeOfAccident || addedAccident?.type || "N/A";
    const helpService =
      {
        "zdravstveni primer": "Rešilci",
        prometna: "Policija",
        kriminal: "Policija",
        "naravna nesreča": "Gasilci",
      }[accidentType] || "N/A";

    const doc = (
      <SimulationPDF
        simulationName={simulationName}
        fromAddress={fromAddress}
        toAddress={toAddress}
        time={time}
        accidentType={accidentType}
        helpService={helpService}
      />
    );

    const blobPdf = await pdf(doc).toBlob();
    const url = URL.createObjectURL(blobPdf);
    const link = document.createElement("a");
    link.href = url;
    link.download = "simulacija.pdf";
    link.click();
  };

  const getAddressFromCoordinates = async (lat, lon) => {
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json`
      );
      const data = await response.json();
      const addr = data.address;
      return `${addr.road || "Cesta"} ${addr.house_number || ""}, ${
        addr.city || addr.town || addr.village || ""
      }`;
    } catch (error) {
      console.error("Napaka pri geokodiranju:", error);
      return `lat:${lat}, lng:${lon}`;
    }
  };

  useEffect(() => {
    const fetchAddresses = async () => {
      const from = simulation?.bestPathId?.locationPoints?.[0];
      const toCoords =
        simulation?.accidentId?.locationId?.geometry?.coordinates;

      if (from && toCoords) {
        const fromAddr = await getAddressFromCoordinates(from.lat, from.lng);
        const toAddr = await getAddressFromCoordinates(
          toCoords[1],
          toCoords[0]
        ); // [lat, lon]

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

  useEffect(() => {
    const fetchAddresses = async () => {
      if (
        !newPath?.length ||
        !addedAccident?.latitude ||
        !addedAccident?.longitude
      )
        return;

      const from = newPath[0];
      const to = {
        lat: addedAccident.latitude,
        lng: addedAccident.longitude,
      };

      try {
        const fromAddr = await getAddressFromCoordinates(from.lat, from.lng);
        const toAddr = await getAddressFromCoordinates(to.lat, to.lng);
        setFromAddress(fromAddr);
        setToAddress(toAddr);
      } catch (error) {
        console.error("Napaka pri pridobivanju naslovov:", error);
      }
    };

    fetchAddresses();
  }, [newPath, addedAccident]);

  function preklici() {
    setNewPath([]);
    setTime(0);
    setAddedAccident(null);
  }

  const iconByType = {
    prometna: <FaCarCrash size={50} className="text-red-500" />,
    kriminal: <RiCriminalLine size={50} className="text-black" />,
    "zdravstveni primer": <FaHeartbeat size={50} className="text-green-600" />,
    "naravna nesreča": <FaFire size={50} className="text-orange-500" />,
  };

  const helpNeeded = {
    "zdravstveni primer": "Rešilci",
    prometna: "Policija",
    kriminal: "Policija",
    "naravna nesreča": "Gasilci",
  };

  return (
    <div className="fixed border-[3px] py-[15px] bg-gray-200 border-black bottom-4 left-[130px] shadow-lg p-4 w-[280px] z-50">
      <h1 className="font-bold justify-center flex text-xl mb-1 uppercase">
        {simulation?.simulationName || "Nova Simulacija"}
      </h1>

      <p className="text-md mb-2">
        Potrebna pomoč:{" "}
        <span className="font-semibold uppercase">
          {helpNeeded[simulation?.accidentId?.typeOfAccident] ||
            helpNeeded[addedAccident?.type] ||
            "N/A"}
        </span>
      </p>

      <p className="text-md mb-2">
        Tip nesreče:{" "}
        <span className="font-semibold">
          {simulation?.accidentId?.typeOfAccident ||
            addedAccident?.type ||
            "N/A"}
        </span>
      </p>

      <div className="w-full flex justify-between">
        <div>
          <div className="text-lg font-bold text-black mb-2">
            {simulation?.responseTime
              ? formatTime(simulation.responseTime)
              : time
              ? formatTime(time)
              : "0h 0min 0sec"}
          </div>
          <p className="text-sm mb-1">
            Od:{" "}
<span className="font-medium">
  {fromAddress || (
    <span className="inline-flex">
      <span className="animate-bounce [animation-delay:0ms]">.</span>
      <span className="animate-bounce [animation-delay:200ms]">.</span>
      <span className="animate-bounce [animation-delay:400ms]">.</span>
    </span>
  )}
</span>
          </p>
          <p className="text-sm mb-3">
            Do:{" "}
<span className="font-medium">
  {toAddress || (
    <span className="inline-flex">
      <span className="animate-bounce [animation-delay:0ms]">. </span>
      <span className="animate-bounce [animation-delay:200ms]">. </span>
      <span className="animate-bounce [animation-delay:400ms]">.</span>
    </span>
  )}
</span>
          </p>
        </div>
        <div className="bg-gray-200 flex items-center justify-center w-[120px]">
          {iconByType[simulation?.accidentId?.typeOfAccident] ||
            iconByType[addedAccident?.type] ||
            null}
        </div>
      </div>
      <div className=" flex justify-between mt-2">
        <button
          onClick={() => preklici()}
          className={`${
            simulation != null && "hidden"
          } bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded`}
        >
          PREKLIČI
        </button>
        <button
          onClick={() => saveSimulation()}
          className={`${
            (simulation != null || time == 0) && "hidden"
          } bg-gray-800 hover:bg-black text-white px-3 py-1 rounded`}
        >
          SHRANI
        </button>
        <button
          onClick={() =>
            createPdf(simulation, fromAddress, toAddress, addedAccident)
          }
          className={`${
            simulation == null && "hidden"
          } text-gray-600 hover:text-black`}
          title="Prenesi PDF"
        >
          <IoMdDownload size={30} />
        </button>
      </div>
    </div>
  );
}
