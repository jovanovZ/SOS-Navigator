const Traffic = require("../models/TrafficModel");

exports.getAll = async (req, res) => {
  try {
    //const traffic = await Traffic.find();  ---> Nalaganje z sliko (bolj pocasi)
    const traffic = await Traffic.find().select("-image_base64").lean(); // Nalaganje brez sliko
    res.json({ traffic, message: "Successfully find all traffic data" });
  } catch (err) {
    return res.status(500).json({ message: "Failed to get all traffic data " });
  }
};

exports.getById = async (req, res) => {
  const { trafficId } = req.params;
  if (!trafficId) {
    return res.status(400).json({ message: "trafficId is null" });
  }
  try {
    const traffic = await Traffic.findById(trafficId);
    if (!traffic) {
      return res.status(404).json({ message: "Traffic not found" });
    }
    return res
      .status(200)
      .json({ traffic, message: "Successfully find traffic data by id" });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Error on get traffic by id", error });
  }
};

exports.deleteTraffic = async (req, res) => {
  const { trafficId } = req.params;
  if (!trafficId) {
    return res.status(400).json({ message: "trafficId is null" });
  }
  try {
    const traffic = await Traffic.findByIdAndDelete(trafficId);
    if (!traffic) {
      return res.status(404).json({ message: "Traffic not found" });
    }
    return res.status(200).json({ message: "Traffic deleted successfully" });
  } catch (error) {
    return res.status(500).json({ message: "Error on delete traffic", error });
  }
};
