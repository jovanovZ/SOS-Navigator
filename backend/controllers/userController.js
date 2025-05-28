const User = require("../models/UserModel");
const Simulation = require("../models/SimulationModel");
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const JWT_SECRET = process.env.JWT_SECRET || "my_secret";

const { upload } = require("../config/cloudinary");
const cloudinary = require("cloudinary").v2;

const createToken = (userId) => {
  return jwt.sign({ id: userId }, JWT_SECRET, { expiresIn: "1d" });
};

exports.login = async (req, res) => {
  const { username, password } = req.body;
  console.log("Incoming login body:", req.body);
  try {
    const user = await User.findOne({ username });
    if (!user) return res.status(401).json({ message: "Invalid credentials" });

    console.log("User found:", user);
    const match = await bcrypt.compare(password, user.password);
    if (!match) return res.status(401).json({ message: "Invalid credentials" });

    const token = createToken(user._id);
    console.log("cookie");

    res.cookie("token", token, {
      secure: false,
      httpOnly: true,
      sameSite: "Strict",
      maxAge: 24 * 60 * 60 * 1000,
    });

    return res.json({
      user: {
        id: user._id,
        username: user.username,
        email: user.email,
        image: user.imageUrl || "",
      },
    });
  } catch (error) {
    return res.status(500).json({ message: "Server error" });
  }
};

exports.register = async (req, res) => {
  const { username, email, password } = req.body;
  console.log("Incoming register body:", req.body); // <== To dodaj
  if (!username || !email || !password) {
    return res.status(400).json({ message: "All fields are required" });
  }
  try {
    const existsEmail = await User.findOne({ email });
    console.log("Email check:", existsEmail); // <== To dodaj
    if (existsEmail)
      return res.status(400).json({ message: "Email already in use" });

    const existsUsername = await User.findOne({ username });
    console.log("Username check:", existsUsername); // <== To dodaj
    if (existsUsername)
      return res.status(400).json({ message: "Username already taken" });

    const hashedPassword = await bcrypt.hash(password, 10);
    const avatar = "https://api.dicebear.com/7.x/fun-emoji/svg?seed=1";

    const newUser = new User({
      username,
      email,
      password: hashedPassword,
      imageUrl: avatar,
    });
    await newUser.save();

    return res
      .status(201)
      .json({ user: { id: newUser._id, username, email, imageUrl: avatar } });
  } catch (error) {
    console.error("Registration error:", error);
    return res.status(500).json({ message: "Server error" });
  }
};

exports.getProfile = async (req, res) => {
  try {
    const user = await User.findById(req.user.id)
      .select("-password")
      .populate("historySimulations");
    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }
    return res.status(200).json(user);
  } catch (error) {
    return res.status(500).json({ message: "Server error" });
  }
};

exports.updateProfilePhoto = async (req, res) => {
  try {
    const { userId } = req.body;

    if (!req.file || !userId) {
      return res.status(400).json({ message: "Missing image or userId" });
    }

    const result = await cloudinary.uploader.upload(req.file.path);

    const user = await User.findByIdAndUpdate(
      userId,
      { imageUrl: result.secure_url },
      { new: true }
    );

    res.status(200).json({ message: "Image updated", image: user.imageUrl });
  } catch (error) {
    console.error("Error uploading profile image:", error);
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

exports.updateUsername = async (req, res) => {
  const { username, userId } = req.body;
  //   if (!req.user._id) {
  //     return res.status(400).json({ message: "All fields are required" });
  // }
  if (!username || !userId) {
    return res.status(400).json({ message: "All fields are required" });
  }
  try {
    const user = await User.findById(userId);
    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }
    user.username = username;
    await user.save();
    return res.status(200).json({
      message: "Username updated successfully",
      username: user.username,
    });
  } catch (error) {
    return res.status(500).json({ message: "Server error" });
  }
};
exports.updateEmail = async (req, res) => {
  const { email, userId } = req.body;
  if (!email || !userId) {
    return res.status(400).json({ message: "All fields are required" });
  }
  try {
    const user = await User.findById(userId);
    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }
    user.email = email;
    await user.save();
    return res.status(200).json({
      message: "Username updated successfully",
      email: user.email,
    });
  } catch (error) {
    return res.status(500).json({ message: "Server error" });
  }
};

exports.updatePasswrord = async (req, res) => {
  const { password, newPassword, userId } = req.body;
  if (!userId || !password || !newPassword) {
    return res.status(400).json({ message: "All fields are required" });
  }
  try {
    const user = await User.findById(userId);
    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }
    const match = await bcrypt.compare(password, user.password);
    if (match) {
      const hashedPassword = await bcrypt.hash(newPassword, 10);
      user.password = hashedPassword;
      await user.save();
      return res.status(200).json({ message: "Password updated successfully" });
    }
    return res.status(400).json({ message: "Wrong password" });
  } catch (error) {
    return res.status(500).json({ message: "Server error" });
  }
};

exports.getAll = async (req, res) => {
  try {
    const users = await User.find().select("-password");
    return res
      .status(200)
      .json({ users, message: "Successfully fetched all users" });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Failed to fetch users", error: error.message });
  }
};
exports.deleteUser = async (req, res) => {
  const { userId } = req.params;
  if (!userId) {
    return res.status(400).json({ message: "userId is required" });
  }
  try {
    const user = await User.findByIdAndDelete(userId);
    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }
    return res.status(200).json({ message: "User deleted successfully" });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Failed to delete user", error: error.message });
  }
};
exports.updateUser = async (req, res) => {
  const { userId } = req.params;
  const { username, email, imageUrl } = req.body;
  console.log(userId);
  if (!userId || !username || !email || !imageUrl) {
    return res.status(400).json({ message: "All fields are required" });
  }
  try {
    const user = await User.findByIdAndUpdate(
      userId,
      {
        username,
        email,
        imageUrl,
      },
      { new: true }
    ).select("-password");
    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }
    return res.status(200).json({ user, message: "User updated successfully" });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Failed to update user", error: error.message });
  }
};

exports.getRandomId = async (req, res) => {
  try {
    const count = await User.countDocuments();
    if (count === 0) {
      return res.status(404).json({ message: "No users found" });
    }
    const random = Math.floor(Math.random() * count);
    const user = await User.findOne().skip(random).select("_id");
    if (!user) {
      return res.status(404).json({ message: "No user found" });
    }
    return res.status(200).json({ id: user._id });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Failed to get random user ID" });
  }
};
