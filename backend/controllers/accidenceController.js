const Accident = require("../models/AccidenceModel");
const Location = require("../models/LocationModel");

exports.createAccident = async (req, res) => {
  const { latitude, longitude, type } = req.body;

  if (typeof latitude !== "number" || typeof longitude !== "number" || !type) {
    return res.status(400).json({ message: "Polja latitude, longitude in type so obvezna." });
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
    });

    await newAccident.save();
    console.log(newAccident)
    return res.status(201).json({
      message: "Nesreča uspešno ustvarjena.",
      accident: {
        id: newAccident._id,
        typeOfAccident: newAccident.typeOfAccident,
        location: {
          id: newLocation._id,
          coordinates: newLocation.geometry.coordinates,
        },
      },
    });
  } catch (error) {
    console.error("Napaka pri ustvarjanju nesreče:", error);
    return res.status(500).json({ message: "Napaka na strežniku pri ustvarjanju nesreče." });
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
  const { locationId, typeOfAccident } = req.body;
  if (!accidentId || !locationId || !typeOfAccident) {
    return res.status(400).json({ message: "All fields are required" });
  }
  try {
    const accident = await Accident.findByIdAndUpdate(
      accidentId,
      { locationId, typeOfAccident },
      { new: true }
    );
    if (!accident) {
      return res.status(404).json({ message: "Accident not found" });
    }
    return res
      .status(200)
      .json({ accident, message: "Accident updated successfully" });
  } catch (error) {
    return res.status(500).json({ message: "Failed to update accident" });
  }
};

exports.getAll = async (req, res) => {
  try {
    const accidents = await Accident.find().populate("locationId");
    return res.status(200).json(accidents);
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
