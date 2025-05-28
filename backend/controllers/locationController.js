const Location = require("../models/LocationModel");
const Station = require("../models/StationModel");
const Accident = require("../models/AccidenceModel");
const cookieParser = require("cookie-parser");

exports.createLocation = async (req, res) => {
  const { long, lat } = req.body;
  if (
    long === undefined ||
    lat === undefined ||
    long === null ||
    lat === null
  ) {
    return res
      .status(400)
      .json({ message: "longitude or latitude is not given" });
  }

  try {
    const location = new Location({
      geometry: {
        type: "Point",
        coordinates: [Number(long), Number(lat)],
      },
    });

    await location.save();
    console.log(location);
    return res.status(200).json({
      location,
    });
  } catch (error) {
    return res.status(500).json({ message: "Error on create location", error });
  }
};

exports.updateLocation = async (req, res) => {
  const { locationId } = req.params;
  const { long, lat } = req.body;
  if (!locationId || long === undefined || lat === undefined) {
    return res
      .status(400)
      .json({ message: "locationId, longitude or latitude is not given" });
  }
  try {
    const location = await Location.findByIdAndUpdate(
      locationId,
      {
        geometry: {
          type: "Point",
          coordinates: [Number(long), Number(lat)],
        },
      },
      { new: true }
    );
    if (!location) {
      return res.status(404).json({ message: "Location not found" });
    }
    return res.status(200).json({
      location,
      message: "Location updated successfully",
    });
  } catch (error) {
    return res.status(500).json({ message: "Error on update location", error });
  }
};

exports.deleteLocation = async (req, res) => {
  const { locationId } = req.params;
  if (!locationId) {
    return res.status(400).json({ message: "locationId is null" });
  }
  try {
    const location = await Location.findByIdAndDelete(locationId);
    if (!location) {
      return res.status(404).json({ message: "Location not found" });
    }
    return res.status(200).json({ message: "Location deleted successfully" });
  } catch (error) {
    return res.status(500).json({ message: "Error on delete location", error });
  }
};

exports.getAll = async (req, res) => {
  try {
    const locations = await Location.find();
    if (locations.length != 0) {
      res.json({ locations, message: "Successfully find all locations" });
    }
  } catch (err) {
    return res.status(500).json({ message: "Failed to get all accidents" });
  }
};

exports.findAllLocationsInRadius = async (req, res) => {
  const { long, lat, radius } = req.query; // log, lat to je center point od kroga; radius je v metrih
  if (!long || !lat || !radius) {
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
    return res
      .status(200)
      .json({ locations, message: "Successfully find locations in radius" });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Failed to find locations in radius" });
  }
};

exports.findNearestStation = async (req, res) => {
  const { locationId } = req.params;
  if (!locationId) {
    return res.status(400).json({ message: "No locationId given" });
  }

  try {
    const location = await Location.findById(locationId);
    if (!location) {
      return res.status(404).json({ message: "Location not found" });
    }
    const [longitude, latitude] = location.geometry.coordinates;

    const nearestLocation = await Location.findOne({
      _id: { $ne: location._id },
      geometry: {
        $near: {
          $geometry: {
            type: "Point",
            coordinates: [longitude, latitude],
          },
        },
      },
    });

    if (!nearestLocation) {
      return res.status(404).json({ message: "No nearby location found" });
    }

    const nearestStation = await Station.findOne({
      locationId: nearestLocation._id,
    }).populate("locationId");

    if (!nearestStation) {
      return res.status(404).json({ message: "No nearby station found" });
    }

    return res.status(200).json({ nearestStation });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Failed to find stations for given location" });
  }
};

exports.findAllStationsInRadius = async (req, res) => {
  const { long, lat, radius } = req.query;
  // log, lat to je center point od kroga; radius je v metrih
  if (!long || !lat || !radius) {
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

exports.findAllAccidentsInRadius = async (req, res) => {
  const { long, lat, radius } = req.query;
  // log, lat to je center point od kroga; radius je v metrih
  if (!long || !lat || !radius) {
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
    const stations = await Accident.find({
      locationId: { $in: locationIds },
    }).populate("locationId");

    if (stations.length === 0) {
      return res.status(404).json({ message: "No accidents in this radius" });
    }

    return res
      .status(200)
      .json({ stations, message: "Successfully found accidents in radius" });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Failed to find locations in radius" });
  }
};

exports.findSomeStationsInRadius = async (req, res) => {
  const { long, lat, radius, n } = req.query;
  // log, lat to je center point od kroga; radius je v metrih, n je stevilo postaj ki jih želit imeti
  if (!long || !lat || !radius || !n) {
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
    }).limit(Number(n));
    if (locations.length === 0) {
      return res.status(404).json({ message: "No locations in this radius" });
    }

    const locationIds = locations.map((loc) => loc._id);
    const stations = await Station.find({
      locationId: { $in: locationIds },
    }).populate("locationId");

    if (stations.length != Number(n)) {
      return res
        .status(404)
        .json({ message: `Could not find ${n} stations in radius` });
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
