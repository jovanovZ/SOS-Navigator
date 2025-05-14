const express = require('express');
const router = express.Router();
const accidentController = require('../controllers/accidenceController');

router.get('/all', accidentController.getAll);
router.get('/type/:typeOfAccident', accidentController.getByCertainType);
router.get('/location/:locationId', accidentController.getByLocation);


router.post('/create', accidentController.createAccident);
router.put('/update/:accidentId', accidentController.updateAccident);

router.delete('/delete/:accidentId', accidentController.deleteAccident);


module.exports = router;