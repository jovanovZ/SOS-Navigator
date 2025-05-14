const Path = require("../models/PathModel");
const Location = require("../models/LocationModel");
const { path } = require("../app");

exports.createPath = async (req, res) => {
  const { accidentId, locationPoints } = req.body;
  if (!accidentId || !locationPoints) {
    return res.status(400).json({ message: "All fields are required " });
  }
  try {
    const foundLocations = await Location.find({
      _id: { $in: locationPoints },
    });

    if (foundLocations.length !== locationPoints.length) {
      return res
        .status(404)
        .json({ message: "One or more locations do not exist" });
    }

    const newPath = new Path({ accidentId, locationPoints });
    newPath.save();
    return res
      .status(200)
      .json({ path: newPath, message: "Path created successfully" });
  } catch (error) {
    return res.status(500).json({ message: "Failed to create Path" });
  }
};

exports.updatePath = async (req, res) => {
  const { pathId } = req.params;
  const { accidentId, locationPoints } = req.body;
  if (!pathId || !accidentId || !locationPoints) {
    return res.status(400).json({ message: "All fields are required" });
  }
  try {
    const path = await Path.findByIdAndUpdate(
      pathId,
      { accidentId, locationPoints },
      { new: true }
    );
    if (!path) {
      return res.status(404).json({ message: "Filed to update Path" });
    }
    return res.status(200).json({ path, message: "Path updated successfully" });
  } catch (error) {
    return res.status(500).json({ message: "Failed to update path" });
  }
};

exports.deletePath = async (req, res) => {
  const { pathId } = req.params;
  if (!pathId) {
    return res.status(400).json({ message: "No path id" });
  }
  try {
    const path = await Path.findByIdAndDelete(pathId);

    if (!path) {
      return res.status(404).json({ message: "Filed to delete Path" });
    }
    return res.status(200).json({ message: "Path deleted successfully" });
  } catch (error) {
    return res.status(500).json({ message: "Failed to delete path" });
  }
};

exports.getAllPaths = async (req, res) => {
  try {
    const paths = await Path.find()
      .populate("accidentId")
      .populate("locationPoints");
    if (paths.length === 0) {
      return res.status(404).json({ message: "There are no paths" });
    }
    return res.status(200).json({ paths });
  } catch (error) {
    return res.status(500).json({ message: "Failed to get all paths" });
  }
};

exports.getByAccidentId = async (req, res) => {
  const { accidentId } = req.params;
  if (!accidentId) {
    return res.status(400).json({ message: "No accident id to find a path" });
  }
  try {
    const path = await Path.find({ accidentId })
      .populate("accidentId")
      .populate("locationPoints");

    if (!path) {
      return res
        .status(404)
        .json({ message: " Could not find path with accident id" });
    }
    return res
      .status(200)
      .json({ path, message: `Path with accidentId=${accidentId} was found` });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Failed to find a path by ceratin accident id" });
  }
};

exports.getAllPathsByLocation = async (req, res) => {
  const { locationId } = req.params;
  if (!locationId) {
    return res.status(400).json({ message: "No path with this location" });
  }
  try {
    const paths = await Path.find({ locationPoints: locationId })
      .populate("accidentId")
      .populate("locationPoints");

    if (paths.length === 0) {
      return res
        .status(404)
        .json({ message: "No paths found for this location" });
    }
    return res.status(200).json({ paths });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Failed to get a path by ceratin location" });
  }
};
