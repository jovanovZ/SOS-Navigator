const Simulation = require("../models/SimulationModel");
const Location = require("../models/LocationModel");
const Accident = require("../models/AccidenceModel");
const Path = require("../models/PathModel");
const User = require("../models/UserModel");
const Station = require("../models/StationModel");

exports.createSimulation = async (req, res) => {
    const {userId, accidentId, typeOfServices, bestStationId, bestPathId, responseTime} = req.body;
    if(!userId || !accidentId || !typeOfServices || !bestStationId ||!bestPathId || !responseTime){
        return res.status(400).json({message: "All fields are required"});
    }
    try {
        const user = await User.findById(userId);
        if(!user){
            return res.status(404).json({message: "User not found"});
        }
        const accident = await Accident.findById(accidentId);
        if(!accident){
            return res.status(404).json({message: "Accident not found"});
        }
        const station = await Station.findById(bestStationId);
        if(!station){
            return res.status(404).json({message: "Station not found"});
        }
        const path = await Path.findById(bestPathId);
        if(!path){
            return res.status(404).json({message: "Path not found"});
        }

        const newSimulation = new Simulation({
            userId,
            accidentId,
            typeOfServices,
            bestStationId,
            bestPathId,
            responseTime
        });
        console.log(newSimulation);
        await newSimulation.save();
        return res.status(201).json({
            simulation: {
                id: newSimulation._id,
                userId,
                accidentId,
                typeOfServices,
                bestStationId,
                bestPathId,
                responseTime
            },
            message: "Simulation created successfully"
        });
    }catch (error) {
        return res.status(500).json({message: "Failed to create simulation"});
    }
};


exports.deleteSimulation = async (req, res) => {
    const {simulationId} = req.params;
    if(!simulationId){
        return res.status(400).json({message: "Simulation ID is required"});
    }
    try {
        const simulation = await Simulation.findByIdAndDelete(simulationId);
        if(!simulation){
            return res.status(404).json({message: "Simulation not found"});
        }
        return res.status(200).json({message: "Simulation deleted successfully"});
    }catch (error) {
        return res.status(500).json({message: "Failed to delete simulation"});
    }
};

exports.updateSimulation = async (req, res) => {
    const {simulationId} = req.params;
    const {userId, accidentId, typeOfServices, bestStationId, bestPathId, responseTime} = req.body;
    if(!simulationId || !userId || !accidentId || !typeOfServices || !bestStationId ||!bestPathId || !responseTime){
        return res.status(400).json({message: "All fields are required"});
    }
    try {
        const simulation = await Simulation.findByIdAndUpdate(
            simulationId,
            {userId, accidentId, typeOfServices, bestStationId, bestPathId, responseTime},
            {new: true}
        );
        if(!simulation){
            return res.status(404).json({message: "Simulation not found"});
        }
        return res.status(200).json({
            simulation,
            message: "Simulation updated successfully"
        });
    }catch (error) {
        return res.status(500).json({message: "Failed to update simulation"});
    }
};

exports.getSimulationById = async (req, res) => {
    const {simulationId} = req.params;
    if(!simulationId){
        return res.status(400).json({message: "Simulation ID is required"});
    }
    try {
        const simulation = await Simulation.findById(simulationId).populate("userId").populate("accidentId").populate("bestStationId").populate("bestPathId");
        if(!simulation){
            return res.status(404).json({message: "Simulation not found"});
        }
        return res.status(200).json(simulation);
    }catch (error) {
        return res.status(500).json({message: "Failed to fetch simulation"});
    }
};

exports.getAllSimulations = async (req, res) => {
    try {
        const simulations = await Simulation.find().populate("userId").populate("accidentId").populate("bestStationId").populate("bestPathId").populate("typeOfServices");
        return res.status(200).json(simulations);
    }catch (error) {
        return res.status(500).json({message: "Failed to fetch all simulations"});
    }
};

exports.getSimulationsByUserId = async (req, res) => {
    const {userId} = req.params;
    if(!userId){
        return res.status(400).json({message: "User ID is required"});
    }
    try {
        const simulations = await Simulation.find({userId}).populate("userId").populate("accidentId").populate("bestStationId").populate("bestPathId");
        if(simulations.length === 0){
            return res.status(404).json({message: "No simulations found for this user"});
        }
        return res.status(200).json(simulations);
    }catch (error) {
        return res.status(500).json({message: "Failed to fetch simulations"});
    }
};

exports.getSimulationsByAccidentId = async (req, res) => {
    const {accidentId} = req.params;
    if(!accidentId){
        return res.status(400).json({message: "Accident ID is required"});
    }
    try {
        const simulations = await Simulation.find({accidentId}).populate("userId").populate("accidentId").populate("bestStationId").populate("bestPathId");
        if(simulations.length === 0){
            return res.status(404).json({message: "No simulations found for this accident"});
        }
        return res.status(200).json(simulations);
    }catch (error) {
        return res.status(500).json({message: "Failed to fetch simulations"});
    }
};

exports.getSimulationsByServiceType = async (req, res) => {
    const {typeOfServices} = req.params;
    if(!typeOfServices){
        return res.status(400).json({message: "Type of services is required"});
    }
    try {
        const simulations = await Simulation.find({typeOfServices}).populate("userId").populate("accidentId").populate("bestStationId").populate("bestPathId");
        if(simulations.length === 0){
            return res.status(404).json({message: "No simulations found for this type of services"});
        }
        return res.status(200).json(simulations);
    }catch (error) {
        return res.status(500).json({message: "Failed to fetch simulations"});
    }
};

