const express = require("express");
const router = express.Router();
const trafficController = require("../controllers/trafficController");

router.get("/all", trafficController.getAll);
router.get("/:trafficId", trafficController.getById);
router.delete("/delete/:trafficId", trafficController.deleteTraffic);

module.exports = router;
