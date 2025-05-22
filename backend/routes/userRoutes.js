const express = require("express");
const router = express.Router();
const userController = require("../controllers/userController");
const authMiddleware = require("../middlewares/authMiddleware");

router.get("/me", authMiddleware, (req, res) => {
  res.json({ _id: req.user._id });
});
router.get("/info", userController.getProfile);

router.post("/register", userController.register);

router.post("/login", userController.login);
router.post("/changeUsername", userController.updateUsername);
router.post("/changeEmail", userController.updateEmail);
router.post("/changePhoto", userController.updateProfilePhoto);
router.post("/changePassword", userController.updatePasswrord);

router.post("/logout", (req, res) => {
  res.clearCookie("token", {
    httpOnly: true,
    sameSite: "Strict",
    secure: false,
  });
  return res.json({ message: "Logged out" });
});

module.exports = router;
