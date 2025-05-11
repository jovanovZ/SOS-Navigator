const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');
const authMiddleware = require('../middlewares/authMiddleware');



router.get("/me", authMiddleware, (req, res) => {
    res.json({ _id: req.user._id });
});

router.post('/register', userController.register);

router.post('/login', userController.login);

router.post("/logout", (req, res) => {
  res.clearCookie("token", {
    httpOnly: true,
    sameSite: "Strict",
    secure: false, 
  });
  return res.json({ message: "Logged out" });
});



module.exports = router;