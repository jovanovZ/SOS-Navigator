const express = require("express");
const router = express.Router();
const accidentController = require("../controllers/accidenceController");

router.get("/all", accidentController.getAll);
router.get("/:accidentId", accidentController.getAccidentById);
router.get("/type/:typeOfAccident", accidentController.getByCertainType);
router.get("/location/:locationId", accidentController.getByLocation);
router.get("/randomId", accidentController.getRadnomId);

router.post("/create", accidentController.createAccident);
router.post(
  "/generateRandomInRadius",
  accidentController.generateRandomAccidentsInRadius
);

router.put("/update/:accidentId", accidentController.updateAccident);

router.delete("/delete/:accidentId", accidentController.deleteAccident);

module.exports = router;
