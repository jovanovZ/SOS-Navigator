const express = require('express');
const router = express.Router();
const vehicleController = require('../controllers/vehicleController');


router.get("/vehicles/:vehicleId", vehicleController.getVehicleById);
router.get("/vehicles", vehicleController.getAllVehicles);

router.post("/vehicles", vehicleController.createVehicle);

router.put("/vehicles/:vehicleId", vehicleController.updateVehicle);

router.delete("/vehicles/:vehicleId", vehicleController.deleteVehicle);


module.exports = router;