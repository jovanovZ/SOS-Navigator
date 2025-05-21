const Station = require("../models/StationModel");
const Location = require("../models/LocationModel");

exports.createStation = async (req, res) => {
  const { locationId, typeOfStation, isPermanent, region } = req.body;
  if (!locationId || !typeOfStation || !isPermanent || !region) {
    return res.status(400).json({ message: "All fields are required" });
  }
  try {
    const location = await Location.findById(locationId);
    if (!location) {
      return res.status(404).json({ message: "Location not found" });
    }
    const newStation = new Station({
      locationId,
      typeOfStation,
      isPermanent: Boolean(isPermanent), // pretvorba
      region,
    });

    console.log(newStation);
    console.log('hi')
    await newStation.save();

    return res.status(201).json({
      station: {
        id: newStation._id,
        locationId,
        typeOfStation,
        isPermanent,
        region,
      },
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
  if (!stationId || !locationId || !typeOfStation || !isPermanent || !region) {
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

  if (isPermanent !== 'true' && isPermanent !== 'false') {
    return res.status(400).json({ message: "isPermanent must be 'true' or 'false'" });
  }

  isPermanent = isPermanent === 'true';

  try {
    const stations = await Station.find({ isPermanent }).populate("locationId");

    if (stations.length === 0) {
      return res.status(404).json({ message: "No stations found for this permanence value" });
    }

    return res.status(200).json({ stations, message: `Stations found with isPermanent = ${isPermanent}` });
  } catch (error) {
    return res.status(500).json({ message: "Failed to get stations by permanence" });
  }
};

