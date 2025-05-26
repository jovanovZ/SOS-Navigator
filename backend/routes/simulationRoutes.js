const express = require('express');
const router = express.Router();
const simulationController = require('../controllers/simulationController');

router.get('/all', simulationController.getAllSimulations);
router.get('/:simulationId', simulationController.getSimulationById);
router.get('/user/:userId', simulationController.getSimulationsByUserId);
router.get('/accident/:accidentId', simulationController.getSimulationsByAccidentId);
router.get('/type/:typeOfServices', simulationController.getSimulationsByServiceType);

router.post('/create', simulationController.createSimulation);
router.put('/update/:simulationId', simulationController.updateSimulation);

router.put('/change-name', simulationController.changeSimulationName);

router.delete('/delete/:simulationId', simulationController.deleteSimulation);

module.exports = router;