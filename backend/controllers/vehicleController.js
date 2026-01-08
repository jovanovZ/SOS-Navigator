const Vehicle = require("../models/VehicleModel");
const Location = require("../models/LocationModel.js");

exports.createVehicle = async (req, res) => {
  try {
    const {
      latStart,
      latEnd,
      longStart,
      longEnd,
      type,
      acceleration,
      locationFreq,
      accelerationFreq,
    } = req.body;

    if (!latStart || !latEnd || !longStart || !longEnd) {
      return res.status(400).json({
        message: "latStart, latEnd, longStart, longEnd, type are required",
      });
    }

    const newStartLocation = new Location({
      geometry: {
        type: "Point",
        coordinates: [longStart, latStart],
      },
    });

    await newStartLocation.save();

    const newEndLocation = new Location({
      geometry: {
        type: "Point",
        coordinates: [longEnd, latEnd],
      },
    });

    await newEndLocation.save();

    const newVehicle = new Vehicle({
      locationStartId: newStartLocation._id,
      locationEndId: newEndLocation._id,
      type: type || "Police",
      acceleration: acceleration || 1.0,
      locationFreq: locationFreq || 1440,
      accelerationFreq: accelerationFreq || 1440,
    });

    await newVehicle.save();
    console.log(
      newVehicle,
      newStartLocation.geometry.coordinates,
      newEndLocation.geometry.coordinates
    );
    return res.status(201).json({
      message: "Vehicle created successfully",
      vehicle: {
        id: newVehicle._id,
        locationFreq: newVehicle.locationFreq,
        accelerationFreq: newVehicle.accelerationFreq,
        acceleration: newVehicle.acceleration,
        type: newVehicle.type,
        locationStart: {
          id: newStartLocation._id,
          coordinates: newStartLocation.geometry.coordinates,
        },
        locationEnd: {
          id: newEndLocation._id,
          coordinates: newEndLocation.geometry.coordinates,
        },
      },
    });
  } catch (error) {
    console.error("Failed to create vehicle:", error);
    return res.status(500).json({ message: "Failed to create vehicle" });
  }
};

exports.updateVehicle = async (req, res) => {
  const { vehicleId } = req.params;
  const {
    latStart,
    longStart,
    latEnd,
    longEnd,
    type,
    acceleration,
    locationFreq,
    accelerationFreq,
  } = req.body;

  try {
    const newStartLocation = new Location({
      geometry: {
        type: "Point",
        coordinates: [longStart, latStart],
      },
    });
    await newStartLocation.save();

    const newEndLocation = new Location({
      geometry: {
        type: "Point",
        coordinates: [longEnd, latEnd],
      },
    });
    await newEndLocation.save();

    const updatedVehicle = await Vehicle.findByIdAndUpdate(
      vehicleId,
      {
        locationStartId: newStartLocation._id,
        locationEndId: newEndLocation._id,
        type,
        acceleration,
        locationFreq,
        accelerationFreq,
      },
      { new: true }
    );

    res.status(200).json({
      message: "Vehicle updated successfully",
      id: updatedVehicle._id,
    });
  } catch (error) {
    res.status(500).json({ message: "Failed to update vehicle" });
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

    return res.status(200).json({
      id: vehicle._id,
      type: vehicle.type,
      acceleration: vehicle.acceleration,
      locationFreq: vehicle.locationFreq,
      accelerationFreq: vehicle.accelerationFreq,
      locationStart: {
        id: vehicle.locationStartId._id,
        coordinates: vehicle.locationStartId.geometry.coordinates,
      },
      locationEnd: {
        id: vehicle.locationEndId._id,
        coordinates: vehicle.locationEndId.geometry.coordinates,
      },
    });
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

    const mapped = vehicles.map((v) => ({
      id: v._id,
      type: v.type,
      acceleration: v.acceleration,
      locationFreq: v.locationFreq,
      accelerationFreq: v.accelerationFreq,
      timeStamp: v.timeStamp,
      locationStart: {
        id: v.locationStartId._id,
        coordinates: v.locationStartId.geometry.coordinates,
      },
      locationEnd: {
        id: v.locationEndId._id,
        coordinates: v.locationEndId.geometry.coordinates,
      },
    }));

    res.status(200).json(mapped);
  } catch (error) {
    console.error("Failed to fetch vehicles:", error);
    return res.status(500).json({ message: "Failed to fetch vehicles" });
  }
};
