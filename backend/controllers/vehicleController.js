const Vehicle = require("../models/VehicleModel")

exports.createVehicle = async (req, res) => {
  try {
    const {
      locationStartId,
      locationEndId,
      type,
      acceleration,
      locationFreq,
      accelerationFreq,
    } = req.body;

    if (!locationStartId || !locationEndId || !type) {
      return res
        .status(400)
        .json({ message: "locationStartId, locationEndId and type are required" });
    }

    const newVehicle = new Vehicle({
      locationStartId,
      locationEndId,
      type,
      acceleration: acceleration || 0.0,
      locationFreq: locationFreq || 1,
      accelerationFreq: accelerationFreq || 1,
    });

    await newVehicle.save();

    return res.status(201).json({
      message: "Vehicle created successfully",
      vehicle: newVehicle,
    });
  } catch (error) {
    console.error("Failed to create vehicle:", error);
    return res.status(500).json({ message: "Failed to create vehicle" });
  }
};

exports.updateVehicle = async (req, res) => {
  const { vehicleId } = req.params;
  const {
    locationStartId,
    locationEndId,
    type,
    acceleration,
    locationFreq,
    accelerationFreq,
  } = req.body;

  if (!vehicleId) {
    return res.status(400).json({ message: "Vehicle ID is required" });
  }

  try {
    const updatedVehicle = await Vehicle.findByIdAndUpdate(
      vehicleId,
      {
        locationStartId,
        locationEndId,
        type,
        acceleration,
        locationFreq,
        accelerationFreq,
      },
      { new: true }
    );

    if (!updatedVehicle) {
      return res.status(404).json({ message: "Vehicle not found" });
    }

    return res.status(200).json({
      message: "Vehicle updated successfully",
      vehicle: updatedVehicle,
    });
  } catch (error) {
    console.error("Failed to update vehicle:", error);
    return res.status(500).json({ message: "Failed to update vehicle" });
  }
};

exports.deleteVehicle = async (req, res) => {
  const { vehicleId } = req.params;

  if (!vehicleId) {
    return res.status(400).json({ message: "Vehicle ID is required" });
  }

  try {
    const deletedVehicle = await Vehicle.findByIdAndDelete(vehicleId);
    if (!deletedVehicle) {
      return res.status(404).json({ message: "Vehicle not found" });
    }

    return res.status(200).json({ message: "Vehicle deleted successfully" });
  } catch (error) {
    console.error("Failed to delete vehicle:", error);
    return res.status(500).json({ message: "Failed to delete vehicle" });
  }
};

exports.getVehicleById = async (req, res) => {
  const { vehicleId } = req.params;

  if (!vehicleId) {
    return res.status(400).json({ message: "Vehicle ID is required" });
  }

  try {
    const vehicle = await Vehicle.findById(vehicleId)
      .populate("locationStartId")
      .populate("locationEndId");

    if (!vehicle) {
      return res.status(404).json({ message: "Vehicle not found" });
    }

    return res.status(200).json(vehicle);
  } catch (error) {
    console.error("Failed to fetch vehicle:", error);
    return res.status(500).json({ message: "Failed to fetch vehicle" });
  }
};

exports.getAllVehicles = async (req, res) => {
  try {
    const vehicles = await Vehicle.find()
      .populate("locationStartId")
      .populate("locationEndId");

    return res.status(200).json(vehicles);
  } catch (error) {
    console.error("Failed to fetch vehicles:", error);
    return res.status(500).json({ message: "Failed to fetch vehicles" });
  }
};