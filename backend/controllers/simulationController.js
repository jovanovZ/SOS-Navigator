const Simulation = require("../models/SimulationModel");
const Location = require("../models/LocationModel");
const Accident = require("../models/AccidenceModel");
const Path = require("../models/PathModel");
const User = require("../models/UserModel");
const Station = require("../models/StationModel");

exports.createSimulation = async (req, res) => {
  try {
    const {
      userId,
      simulationName,
      accidentId,
      bestStationId,
      bestPathId,
      responseTime,
      typeOfServices,
      locationFrom,
      locationTo,
    } = req.body;

    //accident je to
    //station je from
    const responseFrom = await fetch(
      `https://nominatim.openstreetmap.org/reverse?lat=${station.locationId.geometry.coordinates[1]}&lon=${station.locationId.geometry.coordinates[0]}&format=json`
    );
    const data = await responseFrom.json();
    const addr = data.address;
    const locationFromApi = `${addr.road || "Cesta"} ${addr.house_number || ""}, ${
      addr.city || addr.town || addr.village || ""
    }`;

    const responseTo = await fetch(
      `https://nominatim.openstreetmap.org/reverse?lat=${accident.locationId.geometry.coordinates[1]}&lon=${accident.locationId.geometry.coordinates[0]}&format=json`
    );
    const dataTo = await responseTo.json();
    const addrTo = dataTo.address;
    const locationToApi = `${addrTo.road || "Cesta"} ${
      addrTo.house_number || ""
    }, ${addrTo.city || addrTo.town || addrTo.village || ""}`;

    const newSimulation = new Simulation({
      userId,
      simulationName,
      accidentId,
      bestStationId,
      bestPathId,
      responseTime,
      typeOfServices,
      locationFrom: locationFromApi,
      locationTo : locationToApi,
    });
    await newSimulation.save();
    return res.status(201).json({
      message: "Simulation created successfully",
    });
  } catch (error) {
    console.error("Failed to create simulation:", error);
    res.status(500).json({ message: "Failed to create simulation" });
  }
};

exports.deleteSimulation = async (req, res) => {
  const { simulationId } = req.params;
  if (!simulationId) {
    return res.status(400).json({ message: "Simulation ID is required" });
  }
  try {
    const simulation = await Simulation.findByIdAndDelete(simulationId);
    if (!simulation) {
      return res.status(404).json({ message: "Simulation not found" });
    }
    return res.status(200).json({ message: "Simulation deleted successfully" });
  } catch (error) {
    return res.status(500).json({ message: "Failed to delete simulation" });
  }
};

exports.updateSimulation = async (req, res) => {
  const { simulationId } = req.params;
  const {
    userId,
    accidentId,
    typeOfServices,
    bestStationId,
    bestPathId,
    responseTime,
  } = req.body;
  if (
    !simulationId ||
    !userId ||
    !accidentId ||
    !typeOfServices ||
    !bestStationId ||
    !bestPathId ||
    !responseTime
  ) {
    return res.status(400).json({ message: "All fields are required" });
  }
  try {
    const simulation = await Simulation.findByIdAndUpdate(
      simulationId,
      {
        userId,
        accidentId,
        typeOfServices,
        bestStationId,
        bestPathId,
        responseTime,
      },
      { new: true }
    );
    if (!simulation) {
      return res.status(404).json({ message: "Simulation not found" });
    }
    return res.status(200).json({
      simulation,
      message: "Simulation updated successfully",
    });
  } catch (error) {
    return res.status(500).json({ message: "Failed to update simulation" });
  }
};

exports.getSimulationById = async (req, res) => {
  const { simulationId } = req.params;
  if (!simulationId) {
    return res.status(400).json({ message: "Simulation ID is required" });
  }
  try {
    const simulation = await Simulation.findById(simulationId)
      .populate("userId")
      .populate("accidentId")
      .populate("bestStationId")
      .populate("bestPathId");
    if (!simulation) {
      return res.status(404).json({ message: "Simulation not found" });
    }
    return res.status(200).json(simulation);
  } catch (error) {
    return res.status(500).json({ message: "Failed to fetch simulation" });
  }
};

exports.getAllSimulations = async (req, res) => {
  try {
    const simulations = await Simulation.find()
      .populate("userId")
      .populate("accidentId")
      .populate("bestStationId")
      .populate("bestPathId")
      .populate("typeOfServices");
    return res.status(200).json(simulations);
  } catch (error) {
    return res.status(500).json({ message: "Failed to fetch all simulations" });
  }
};

exports.getSimulationsByUserId = async (req, res) => {
  const { userId } = req.params;
  if (!userId) {
    return res.status(400).json({ message: "User ID is required" });
  }
  try {
    const simulations = await Simulation.find({ userId })
      .populate("userId")
      .populate({
        path: "accidentId",
        populate: {
          path: "locationId",
        },
      })
      .populate({
        path: "bestStationId",
        populate: {
          path: "locationId",
        },
      })
      .populate("bestPathId");
    if (simulations.length === 0) {
      return res
        .status(200)
        .json({ message: "No simulations found for this user" });
    }
    return res.status(200).json(simulations);
  } catch (error) {
    return res.status(500).json({ message: "Failed to fetch simulations" });
  }
};

exports.getSimulationsByAccidentId = async (req, res) => {
  const { accidentId } = req.params;
  if (!accidentId) {
    return res.status(400).json({ message: "Accident ID is required" });
  }
  try {
    const simulations = await Simulation.find({ accidentId })
      .populate("userId")
      .populate("accidentId")
      .populate("bestStationId")
      .populate("bestPathId");
    if (simulations.length === 0) {
      return res
        .status(404)
        .json({ message: "No simulations found for this accident" });
    }
    return res.status(200).json(simulations);
  } catch (error) {
    return res.status(500).json({ message: "Failed to fetch simulations" });
  }
};

exports.getSimulationsByServiceType = async (req, res) => {
  const { typeOfServices } = req.params;
  if (!typeOfServices) {
    return res.status(400).json({ message: "Type of services is required" });
  }
  try {
    const simulations = await Simulation.find({ typeOfServices })
      .populate("userId")
      .populate("accidentId")
      .populate("bestStationId")
      .populate("bestPathId");
    if (simulations.length === 0) {
      return res
        .status(404)
        .json({ message: "No simulations found for this type of services" });
    }
    return res.status(200).json(simulations);
  } catch (error) {
    return res.status(500).json({ message: "Failed to fetch simulations" });
  }
};
