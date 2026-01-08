const Accident = require("../models/AccidenceModel");
const Location = require("../models/LocationModel");

exports.createAccident = async (req, res) => {
  const { latitude, longitude, type, locationFreq } = req.body;

  if (typeof latitude !== "number" || typeof longitude !== "number" || !type) {
    return res
      .status(400)
      .json({ message: "Polja latitude, longitude in type so obvezna." });
  }

  try {
    const newLocation = new Location({
      geometry: {
        type: "Point",
        coordinates: [longitude, latitude],
      },
    });

    await newLocation.save();

    const newAccident = new Accident({
      locationId: newLocation._id,
      typeOfAccident: type,
      locationFreq: locationFreq || 1440,
    });

    await newAccident.save();
    console.log(newAccident, newLocation.geometry.coordinates);
    return res.status(201).json({
      message: "Nesreča uspešno ustvarjena.",
      accident: {
        id: newAccident._id,
        typeOfAccident: newAccident.typeOfAccident,
        location: {
          id: newLocation._id,
          coordinates: newLocation.geometry.coordinates,
        },
        locationFreq: newAccident.locationFreq,
      },
    });
  } catch (error) {
    console.error("Napaka pri ustvarjanju nesreče:", error);
    return res
      .status(500)
      .json({ message: "Napaka na strežniku pri ustvarjanju nesreče." });
  }
};

exports.deleteAccident = async (req, res) => {
  const { accidentId } = req.params;
  if (!accidentId) {
    return res.status(400).json({ message: "Accident ID is required" });
  }
  try {
    const accident = await Accident.findByIdAndDelete(accidentId);
    if (!accident) {
      return res.status(404).json({ message: "Accident not found" });
    }
    return res.status(200).json({ message: "Accident deleted successfully" });
  } catch (error) {
    return res.status(500).json({ message: "Failed to delete accident" });
  }
};

exports.updateAccident = async (req, res) => {
  const { accidentId } = req.params;
  const { latitude, longitude, type, locationFreq } = req.body;

  if (
    typeof latitude !== "number" ||
    typeof longitude !== "number" ||
    !type
  ) {
    return res.status(400).json({ message: "All fields are required" });
  }

  try {
    const newLocation = new Location({
      geometry: {
        type: "Point",
        coordinates: [longitude, latitude]
      }
    });
    await newLocation.save();

    const accident = await Accident.findByIdAndUpdate(
      accidentId,
      {
        locationId: newLocation._id,
        type,
        locationFreq
      },
      { new: true }
    );

    if (!accident) {
      return res.status(404).json({ message: "Accident not found" });
    }

    return res.status(200).json({
      message: "Accident updated successfully",
      id: accident._id
    });

  } catch (error) {
    return res.status(500).json({ message: "Failed to update accident" });
  }
};


exports.getAll = async (req, res) => {
  try {
    const accidents = await Accident.find().populate("locationId");
    const mapped = accidents.map((a) => ({
      id: a._id,
      typeOfAccident: a.typeOfAccident,
      locationFreq: a.locationFreq,
      location: {
        id: a.locationId._id,
        coordinates: a.locationId.geometry.coordinates,
      },
    }));

    res.status(200).json(mapped);
  } catch (error) {
    return res.status(500).json({ message: "Failed to get all accidents" });
  }
};

exports.getByCertainType = async (req, res) => {
  const { typeOfAccident } = req.params; // to je misleno ce bomo filtirali po vrsti nesrece
  if (!typeOfAccident) {
    return res.status(400).json({ message: "Type of accident is required" });
  }
  try {
    const accidents = await Accident.find({ typeOfAccident }).populate(
      "locationId"
    );
    if (accidents.length === 0) {
      return res
        .status(404)
        .json({ message: "No accidents found for this type" });
    }
    return res
      .status(200)
      .json({ accidents, message: "Accidents found for this type" });
  } catch (error) {
    return res.status(500).json({ message: "Failed to get accidents by type" });
  }
};

exports.getByLocation = async (req, res) => {
  const { locationId } = req.params;
  if (!locationId) {
    return res.status(400).json({ message: "Location ID is required" });
  }

  try {
    const accidents = await Accident.find({ locationId }).populate(
      "locationId"
    );
    console.log(accidents);
    if (accidents.length === 0) {
      return res
        .status(404)
        .json({ message: "No accidents found for this location" });
    }
    return res.status(200).json({
      accidents,
      message: `Accidents found for this location ${locationId}`,
    });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Failed to get accidents by location" });
  }
};

exports.getRadnomId = async (req, res) => {
  try {
    const count = await Accident.countDocuments();
    if (count === 0) {
      return res.status(404).json({ message: "No accidents found" });
    }
    const random = Math.floor(Math.random() * count);
    const accident = await Accident.findOne().skip(random).select("_id");
    if (!accident) {
      return res.status(404).json({ message: "No accident found" });
    }
    return res.status(200).json({ id: accident._id });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Failed to get random accident ID" });
  }
};

exports.getAccidentById = async (req, res) => {
  const { accidentId } = req.params;

  if (!accidentId) {
    return res.status(400).json({ message: "Accident ID is required" });
  }

  try {
    const accident = await Accident.findById(accidentId).populate("locationId");

    if (!accident) {
      return res.status(404).json({ message: "Accident not found" });
    }

    return res.status(200).json({
      id: accident._id,
      typeOfAccident: accident.typeOfAccident,
      locationFreq: accident.locationFreq,
      location: {
        id: accident.locationId._id,
        coordinates: accident.locationId.geometry.coordinates,
      },
    });
  } catch (error) {
    console.error("Failed to fetch accident:", error);
    return res.status(500).json({ message: "Failed to fetch accident" });
  }
};

exports.generateRandomAccidentsInRadius = async (req, res) => {
  const { long, lat, radius, count, type } = req.body;
  if (!long || !lat || !radius || !count || !type) {
    return res.status(400).json({ message: "Missing parameters" });
  }

  function randomPointInRadius(centerLat, centerLong, radiusMeters) {
    const radiusInDegrees = radiusMeters / 111320;
    const u = Math.random();
    const v = Math.random();
    const w = radiusInDegrees * Math.sqrt(u);
    const t = 2 * Math.PI * v;
    const x = w * Math.cos(t);
    const y = w * Math.sin(t);
    return {
      latitude: centerLat + y,
      longitude: centerLong + x,
    };
  }

  try {
    const accidentIds = [];
    for (let i = 0; i < count; i++) {
      const { latitude, longitude } = randomPointInRadius(
        Number(lat),
        Number(long),
        Number(radius)
      );
      const newLocation = new Location({
        geometry: {
          type: "Point",
          coordinates: [longitude, latitude],
        },
      });
      await newLocation.save();

      const accident = new Accident({
        typeOfAccident: type,
        locationId: newLocation._id,
      });
      await accident.save();
      accidentIds.push(accident._id);
    }

    const accidentsPopulated = await Accident.find({
      _id: { $in: accidentIds },
    }).populate("locationId");

    res.status(200).json({ accidents: accidentsPopulated });
  } catch (error) {
    res.status(500).json({ message: "Failed to generate accidents" });
  }
};
