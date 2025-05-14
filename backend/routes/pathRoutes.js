const express = require("express");
const router = express.Router();
const pathController = require("../controllers/pathController");

router.get("/getAll", pathController.getAllPaths);
router.get("/getByAccident/:accidentId", pathController.getByAccidentId);
router.get("/getByLocation/:locationId", pathController.getAllPathsByLocation);

router.post("/create", pathController.createPath);

router.put("/update/:pathId", pathController.updatePath);

router.delete("/delete/:pathId", pathController.deletePath);

module.exports = router;
