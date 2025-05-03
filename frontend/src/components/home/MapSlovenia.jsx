import React from "react";
import {
  MapContainer,
  Marker,
  Popup,
  TileLayer,
  ZoomControl,
} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";



export default function MapSlovenia({gasilciVidnost, bolniceVidnost, policijaVidnost}) {
    const hospitalIcon = L.icon({
        iconUrl: 'hospital.png',
        iconSize: [25, 25],
        iconAnchor: [17, 35],
        popupAnchor: [0, -35]
      });
      
      const policeIcon = L.icon({
        iconUrl: 'police.png',
        iconSize: [25, 25],
        iconAnchor: [17, 35],
        popupAnchor: [0, -35]
      });
      
      const fireIcon = L.icon({
        iconUrl: 'firefighter.png',
        iconSize: [25, 25],
        iconAnchor: [17, 35],
        popupAnchor: [0, -35]
      });
      
      
      const locations = [
        { type: 'hospital', icon: hospitalIcon, latitude: 46.0569, longitude: 14.5058 },
        { type: 'hospital', icon: hospitalIcon, latitude: 46.5547, longitude: 15.6459 },
        { type: 'police', icon: policeIcon, latitude: 46.2381, longitude: 15.2675 },
        { type: 'fire', icon: fireIcon, latitude: 46.2396, longitude: 14.3556 },
        { type: 'police', icon: policeIcon, latitude: 46.3625, longitude: 15.1103 },
        { type: 'fire', icon: fireIcon, latitude: 45.9578, longitude: 13.6431 },
        { type: 'hospital', icon: hospitalIcon, latitude: 45.5481, longitude: 13.7300 },
        { type: 'fire', icon: fireIcon, latitude: 46.1556, longitude: 15.0535 },
        { type: 'police', icon: policeIcon, latitude: 46.5111, longitude: 15.0800 },
        { type: 'hospital', icon: hospitalIcon, latitude: 46.1383, longitude: 14.5934 },
        { type: 'fire', icon: fireIcon, latitude: 46.2253, longitude: 14.6094 },
        { type: 'hospital', icon: hospitalIcon, latitude: 46.5450, longitude: 14.9645 },
        { type: 'fire', icon: fireIcon, latitude: 46.6581, longitude: 16.1666 },
        { type: 'hospital', icon: hospitalIcon, latitude: 46.5645, longitude: 16.4544 },
        { type: 'police', icon: policeIcon, latitude: 45.5282, longitude: 13.5686 }
      ];
      
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

        {
        locations.filter(loc =>
            (loc.type === 'hospital' && bolniceVidnost) ||
            (loc.type === 'police' && policijaVidnost) ||
            (loc.type === 'fire' && gasilciVidnost)
        ).map((loc, index) => (
            <Marker
            key={index}
            position={[loc.latitude, loc.longitude]}
            icon={loc.icon}
            />
        ))}


        <ZoomControl position="bottomright" />
      </MapContainer>
    </div>
  );
}
