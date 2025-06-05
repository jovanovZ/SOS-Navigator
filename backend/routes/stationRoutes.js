const express = require("express");
const router = express.Router();
const stationController = require("../controllers/stationController");

router.get("/all", stationController.getAll);
router.get("/type/:typeOfStation", stationController.getByCertainType);
router.get("/location/:locationId", stationController.getByLocation);
router.get("/region/:region", stationController.getByRegion);
router.get("/permanent/:isPermanent", stationController.getByPermanence);
router.get("/randomId", stationController.getRadnomId);
router.get("/getFiveNearestStations/:long/:lat/:type", stationController.findNearestStationsByType);

router.post("/create", stationController.createStation);
router.put("/update/:stationId", stationController.updateStation);

router.delete("/delete/:stationId", stationController.deleteStation);

module.exports = router;
