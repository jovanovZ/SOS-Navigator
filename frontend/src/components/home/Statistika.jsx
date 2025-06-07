import React, { useEffect, useState } from "react";
import axios from "axios";
import { toast } from "react-toastify";
import Navigation from "./Navigation";
import {
  BarChart, Bar,
  LineChart, Line,
  PieChart, Pie,
  Cell, Tooltip,
  ResponsiveContainer,
  Legend,
  XAxis,
  YAxis
} from "recharts";

const COLORS = ["#6EB1FF", "#98E4C4", "#FFD580", "#FFAB91", "#C2C3FF", "#FFCCCC", "#FF99CC"];

export default function Statistika() {
  const [stations, setStations] = useState([]);
  const [simulationData, setSimulationData] = useState([]);
  const IP = process.env.REACT_APP_IP;

  useEffect(() => {
    const fetchStations = async () => {
      try {
        const response = await axios.get(`http://${IP}/api/station/all`, {
          withCredentials: true,
        });
        setStations(response.data);
      } catch (error) {
        console.error("Error fetching stations:", error);
        toast.error("Error fetching stations");
      }
    };
    fetchStations();
  }, [IP]);

  useEffect(() => {
    const fetchSimulations = async () => {
      try {
        const user = localStorage.getItem("user");
        const userId = JSON.parse(user)?.id || JSON.parse(user)?._id;

        const response = await axios.get(`http://${IP}/api/simulation/user/${userId}`, {
          withCredentials: true,
        });

        setSimulationData(response.data);
      } catch (error) {
        console.error("Napaka pri pridobivanju simulacij:", error);
        toast.error("Napaka pri pridobivanju simulacij");
      }
    };
    fetchSimulations();
  }, [IP]);

  const totalSimulations = simulationData.length;
  const totalStations = stations.length;

  const stationTypes = stations.reduce((acc, s) => {
    acc[s.typeOfStation] = (acc[s.typeOfStation] || 0) + 1;
    return acc;
  }, {});

  const accidentTypes = simulationData.reduce((acc, s) => {
    const type = s.accidentId?.typeOfAccident;
    if (type) acc[type] = (acc[type] || 0) + 1;
    return acc;
  }, {});

  const simulationsByStationType = simulationData.reduce((acc, s) => {
    const type = s.bestStationId?.typeOfStation;
    if (type) acc[type] = (acc[type] || 0) + 1;
    return acc;
  }, {});

  const avgResponseTimeByStationType = {};
  simulationData.forEach((sim) => {
    const type = sim.bestStationId?.typeOfStation;
    const time = sim.responseTime;
    if (type && typeof time === 'number') {
      if (!avgResponseTimeByStationType[type]) {
        avgResponseTimeByStationType[type] = { total: 0, count: 0 };
      }
      avgResponseTimeByStationType[type].total += time;
      avgResponseTimeByStationType[type].count += 1;
    }
  });
  const avgResponseTimeData = Object.entries(avgResponseTimeByStationType).map(
    ([type, { total, count }]) => ({
      name: type,
      value: Math.round(total / count / 1000), // v sekundah
    })
  );

  const accidentTypeData = Object.entries(accidentTypes).map(([name, value]) => ({ name, value }));
  const stationTypeData = Object.entries(stationTypes).map(([name, value]) => ({ name, value }));

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#1f2e48] to-[#020c1b] text-white">
      <Navigation />

      <div className="p-8 mt-12 grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1 flex flex-col gap-6 mt-10">
          <div className="backdrop-blur-md bg-white/10 rounded-2xl p-6 text-center border border-yellow-200">
            <div className="text-3xl font-bold text-yellow-200">{totalStations}</div>
            <div className="text-gray-200">Skupno število postaj</div>
          </div>
          <div className="backdrop-blur-md bg-white/10 rounded-2xl p-6 text-center border border-blue-200">
            <div className="text-3xl font-bold text-blue-200">{totalSimulations}</div>
            <div className="text-gray-200">Skupno število simulacij</div>
          </div>
        </div>

        <div className="lg:col-span-2 backdrop-blur-md bg-white/15 rounded-2xl p-4 border border-blue-300">
          <h3 className="text-xl font-semibold mb-2 text-center text-white">Nesreče po tipu</h3>
          <ResponsiveContainer width="100%" height={250}>
            <PieChart>
              <Pie
                data={accidentTypeData}
                dataKey="value"
                nameKey="name"
                cx="50%"
                cy="50%"
                outerRadius={80}
                label
              >
                {accidentTypeData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="px-4 mt-8 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="backdrop-blur-md bg-white/15 shadow-lg rounded-2xl p-4 border border-green-200">
          <h3 className="text-xl font-semibold mb-2 text-center">Postaje po tipu</h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={stationTypeData} margin={{ top: 20, right: 30, left: 0, bottom: 5 }}>
              <XAxis dataKey="name" stroke="#ccc" />
              <YAxis stroke="#ccc" label={{ value: 'Število', angle: -90, position: 'insideLeft' }} />
              <Tooltip />
              <Legend />
              <Bar dataKey="value" name="Število postaj">
                {stationTypeData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="backdrop-blur-md bg-white/10 shadow-lg rounded-2xl p-4 border border-indigo-200">
          <h3 className="text-xl font-semibold mb-2 text-center">Povprečni odzivni časi (sek)</h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={avgResponseTimeData} margin={{ top: 20, right: 30, left: 0, bottom: 5 }}>
              <XAxis dataKey="name" stroke="#ccc" />
              <YAxis stroke="#ccc" label={{ value: 'sekunde', angle: -90, position: 'insideLeft' }} />
              <Tooltip />
              <Legend />
              <Bar dataKey="value" name="Povprečni čas">
                {avgResponseTimeData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="backdrop-blur-md bg-white/15 shadow-lg rounded-2xl p-4 border border-pink-200">
          <h3 className="text-xl font-semibold mb-2 text-center">Simulacije po tipu (črtni graf)</h3>
          <ResponsiveContainer width="100%" height={250}>
            <LineChart data={accidentTypeData}>
              <XAxis dataKey="name" stroke="#ccc" />
              <YAxis stroke="#ccc" label={{ value: 'Število', angle: -90, position: 'insideLeft' }} />
              <Tooltip />
              <Legend />
              <Line type="monotone" dataKey="value" stroke="#FF99CC" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}