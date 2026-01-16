const Station = require("../models/StationModel");
const Location = require("../models/LocationModel");

const haversineDistance = (coords1, coords2) => {
  const toRad = (deg) => (deg * Math.PI) / 180;
  const [lon1, lat1] = coords1;
  const [lon2, lat2] = coords2;

  const R = 6371; // Earth radius in km
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);

  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
};

exports.createStation = async (req, res) => {
  const { latitude, longitude, typeOfStation, isPermanent, region } = req.body;
  try {
    const location = new Location({
      geometry: {
        type: "Point",
        coordinates: [longitude, latitude],
      },
    });
    await location.save();

    const newStation = new Station({
      locationId: location._id,
      typeOfStation,
      isPermanent: isPermanent, // pretvorba
      region,
    });

    console.log(newStation);

    await newStation.save();
    const populatedStation = await Station.findById(newStation._id).populate(
      "locationId"
    );

    return res.status(201).json({
      station: populatedStation,
      message: "Station created successfully",
    });
  } catch (error) {
    return res.status(500).json({ message: "Failed to create station" });
  }
};

exports.deleteStation = async (req, res) => {
  const { stationId } = req.params;
  if (!stationId) {
    return res.status(400).json({ message: "Station ID is required" });
  }
  try {
    const station = await Station.findByIdAndDelete(stationId);
    if (!station) {
      return res.status(404).json({ message: "Station not found" });
    }
    return res.status(200).json({ message: "Station deleted successfully" });
  } catch (error) {
    return res.status(500).json({ message: "Failed to delete station" });
  }
};

exports.updateStation = async (req, res) => {
  const { stationId } = req.params;
  const { locationId, typeOfStation, isPermanent, region } = req.body;
  if (
    !stationId ||
    !locationId ||
    !typeOfStation ||
    typeof isPermanent !== "boolean" ||
    !region
  ) {
    return res.status(400).json({ message: "All fields are required" });
  }
  try {
    const station = await Station.findByIdAndUpdate(
      stationId,
      { locationId, typeOfStation, isPermanent, region },
      { new: true }
    );
    if (!station) {
      return res.status(404).json({ message: "Station not found" });
    }
    return res
      .status(200)
      .json({ station, message: "Station updated successfully" });
  } catch (error) {
    return res.status(500).json({ message: "Failed to update Station" });
  }
};

exports.getAll = async (req, res) => {
  try {
    const stations = await Station.find().populate("locationId");
    console.log(stations)
    return res.status(200).json(stations);
  } catch (error) {
    return res.status(500).json({ message: "Failed to get all stations" });
  }
};

exports.getByCertainType = async (req, res) => {
  const { typeOfStation } = req.params; // to je misleno ce bomo filtirali po vrsti nesrece
  if (!typeOfStation) {
    return res.status(400).json({ message: "Type of station is required" });
  }
  try {
    const stations = await Station.find({ typeOfStation }).populate(
      "locationId"
    );
    if (stations.length === 0) {
      return res
        .status(404)
        .json({ message: "No stations found for this type" });
    }
    return res
      .status(200)
      .json({ stations, message: "Stations found for this type" });
  } catch (error) {
    return res.status(500).json({ message: "Failed to get stations by type" });
  }
};

exports.getByLocation = async (req, res) => {
  const { locationId } = req.params;
  if (!locationId) {
    return res.status(400).json({ message: "Location ID is required" });
  }

  try {
    const stations = await Station.find({ locationId }).populate("locationId");
    console.log(stations);
    if (stations.length === 0) {
      return res
        .status(404)
        .json({ message: "No stations found for this location" });
    }
    return res.status(200).json({
      stations,
      message: `Stations found for this location ${locationId}`,
    });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Failed to get stations by location" });
  }
};

exports.getByRegion = async (req, res) => {
  const { region } = req.params;
  if (!region) {
    return res.status(400).json({ message: "Region is required" });
  }

  try {
    const stations = await Station.find({ region }).populate("locationId");
    console.log(stations);
    if (stations.length === 0) {
      return res
        .status(404)
        .json({ message: "No stations found for this region" });
    }
    return res.status(200).json({
      stations,
      message: `Stations found for this region ${region}`,
    });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Failed to get stations by location" });
  }
};

exports.getByPermanence = async (req, res) => {
  let { isPermanent } = req.params;

  if (isPermanent !== "true" && isPermanent !== "false") {
    return res
      .status(400)
      .json({ message: "isPermanent must be 'true' or 'false'" });
  }

  isPermanent = isPermanent === "true";

  try {
    const stations = await Station.find({ isPermanent }).populate("locationId");

    if (stations.length === 0) {
      return res
        .status(404)
        .json({ message: "No stations found for this permanence value" });
    }

    return res.status(200).json({
      stations,
      message: `Stations found with isPermanent = ${isPermanent}`,
    });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Failed to get stations by permanence" });
  }
};
exports.getRadnomId = async (req, res) => {
  try {
    const count = await Station.countDocuments();
    if (count === 0) {
      return res.status(404).json({ message: "No stations found" });
    }
    const random = Math.floor(Math.random() * count);
    const station = await Station.findOne().skip(random).select("_id");
    if (!station) {
      return res.status(404).json({ message: "No station found" });
    }
    return res.status(200).json({ id: station._id });
  } catch (error) {
    return res.status(500).json({ message: "Failed to get random station ID" });
  }
};

exports.findNearestStationsByType = async (req, res) => {
  const { long, lat, type } = req.params;

  if (!long || !lat) {
    return res.status(400).json({ message: "No long, lat given" });
  }

  try {
    const longitude = parseFloat(long);
    const latitude = parseFloat(lat);

    const stations = await Station.find({ typeOfStation: type }).populate(
      "locationId"
    );

    const validStations = stations.filter(
      (station) =>
        station.locationId &&
        station.locationId.geometry &&
        Array.isArray(station.locationId.geometry.coordinates)
    );

    const stationsWithDistance = validStations.map((station) => {
      const coords = station.locationId.geometry.coordinates;
      const distance = haversineDistance([longitude, latitude], coords);
      return { station, distance };
    });

    stationsWithDistance.sort((a, b) => a.distance - b.distance);

    const nearestStations = stationsWithDistance
      .slice(0, 5)
      .map((s) => s.station);

    return res.status(200).json({ nearestStations });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Failed to find nearest stations" });
  }
};

exports.findAllStationsInRadius = async (req, res) => {
  const { long, lat, radius, type } = req.query;
  // log, lat to je center point od kroga; radius je v metrih
  if (!long || !lat || !radius || !type) {
    return res
      .status(400)
      .json({ message: "longitude, latitude and radius must be given" });
  }
  const radiusInRadians = Number(radius) / 6378137;

  try {
    const locations = await Location.find({
      geometry: {
        $geoWithin: {
          $centerSphere: [[Number(long), Number(lat)], radiusInRadians],
        },
      },
    });
    if (locations.length === 0) {
      return res.status(404).json({ message: "No locations in this radius" });
    }

    const locationIds = locations.map((loc) => loc._id);
    const stations = await Station.find({
      typeOfStation: type,
      locationId: { $in: locationIds },
    }).populate("locationId");

    if (stations.length === 0) {
      return res.status(404).json({ message: "No stations in this radius" });
    }

    return res
      .status(200)
      .json({ stations, message: "Successfully found stations in radius" });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Failed to find locations in radius" });
  }
};

//funkcija vrne array objektov v objektu je najbolj oddaljena postaja, nesreča in distanca med nesrečo in najbolj oddaljeno postajo
exports.getFurthestStationFromAccident = async (req, res) => {
  // v body-u sta array accidentov in array postaj
  const { accidents, stations } = req.body;
  if (
    !accidents ||
    !stations ||
    !Array.isArray(accidents) ||
    !Array.isArray(stations)
  ) {
    return res
      .status(400)
      .json({ message: "accidents and stations must be an array" });
  }
  try {
    console.log(accidents);
    console.log(stations);
    const results = accidents.map((accident) => {
      if (
        !accident.locationId ||
        !accident.locationId.geometry ||
        !Array.isArray(accident.locationId.geometry.coordinates)
      ) {
        return {
          accident: accident,
          furthestStation: null,
          distance: null,
        };
      }

      const accidentCoords = accident.locationId.geometry.coordinates;
      let maxDistance = -1;
      let furthestStation = null;

      stations.forEach((station) => {
        if (
          station.locationId &&
          station.locationId.geometry &&
          Array.isArray(station.locationId.geometry.coordinates)
        ) {
          const stationCoords = station.locationId.geometry.coordinates;
          const distance = haversineDistance(accidentCoords, stationCoords);
          if (distance > maxDistance) {
            maxDistance = distance;
            furthestStation = station;
          }
        }
      });

      return {
        accident: accident,
        furthestStation,
        distance: maxDistance,
      };
    });

    return res.status(200).json({ results });
  } catch (err) {
    return res
      .status(500)
      .json({ message: "Failed to find furhest station from accident" });
  }
};
