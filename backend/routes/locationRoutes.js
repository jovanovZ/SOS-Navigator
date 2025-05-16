const express = require("express");
const router = express.Router();
const locationController = require("../controllers/locationController");

router.get("/findAllLocationsInRadius", locationController.findAllLocationsInRadius);
//localhost:3002/api/location/findAllLocationsInRadius?long=40.5058&lat=46.0569&radius=5000

router.get(
  "/getNearestStation/:locationId",
  locationController.findNearestStation
); //localhost:3002/api/location/getNearestStation/68271432a0a899bd0a92861f

router.get("/getAllStationsInRadius", locationController.findAllStationsInRadius)
//localhost:3002/api/location/getAllStationsInRadius?long=40.5058&lat=46.0569&radius=5000

router.get("/getSomeStationsInRadius", locationController.findSomeStationsInRadius)
//localhost:3002/api/location/getSomeStationsInRadius?long=40.5058&lat=46.0569&radius=5000&n=2


router.get("/getAllAccidentsInRadius", locationController.findAllAccidentsInRadius)
//localhost:3002/api/location/getAllAccidentsInRadius?long=40.5058&lat=46.0569&radius=5000



module.exports = router;
