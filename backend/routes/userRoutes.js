const express = require("express");
const router = express.Router();
const userController = require("../controllers/userController");
const authMiddleware = require("../middlewares/authMiddleware");

const multer = require("multer");
const upload = multer({ dest: "uploads/" }); // to je upload!

router.post(
  "/update-profile-image",
  upload.single("image"),
  userController.updateProfilePhoto
);

module.exports = router;

router.get("/all", userController.getAll);
router.get("/me", authMiddleware, (req, res) => {
  res.json({ _id: req.user._id });
});
router.get("/info", userController.getProfile);

router.post("/register", userController.register);

router.post("/login", userController.login);
router.post("/changeUsername", userController.updateUsername);
router.post("/changeEmail", userController.updateEmail);

router.post("/changePassword", userController.updatePasswrord);

router.post("/logout", (req, res) => {
  res.clearCookie("token", {
    httpOnly: true,
    sameSite: "Strict",
    secure: false,
  });
  return res.json({ message: "Logged out" });
});

router.put("/update/:userId", userController.updateUser);

router.delete("/delete/:userId", userController.deleteUser);

module.exports = router;
