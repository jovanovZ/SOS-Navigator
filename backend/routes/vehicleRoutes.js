const express = require('express');
const router = express.Router();
const vehicleController = require('../controllers/vehicleController');


router.get("/:vehicleId", vehicleController.getVehicleById);
router.get("/all", vehicleController.getAllVehicles);

router.post("/create", vehicleController.createVehicle);

router.put("/update/:vehicleId", vehicleController.updateVehicle);

router.delete("/delte/:vehicleId", vehicleController.deleteVehicle);


module.exports = router;
