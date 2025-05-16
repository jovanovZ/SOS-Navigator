require("dotenv").config();
const express = require("express");
const cors = require("cors");
const mongoose = require("mongoose");
const userRoutes = require("./routes/userRoutes");
const accidentRoutes = require("./routes/accidenceRoutes");
const pathRoutes = require("./routes/pathRoutes");
const locationRoutes = require("./routes/locationRoutes")
const cookieParser = require("cookie-parser");

// Uvoz route-ov
//const userRoutes = require('./routes/userRoutes');

const app = express();

// Middleware
app.use(
  cors({
    origin: "http://localhost:3000",
    credentials: true, // omogoči piškotke
  })
);
app.use(express.json()); // za JSON requeste
app.use(cookieParser());
app.use("/api", userRoutes);
app.use("/api/accident", accidentRoutes);
app.use("/api/path", pathRoutes);
app.use("/api/location", locationRoutes)

mongoose
  .connect(process.env.MONGO_URI)

  .then(() => console.log("MongoDB connected"))
  .catch((err) => console.error("MongoDB connection error:", err));

app.listen(3002, () => {
  console.log("Server is running on port 3002");
});
module.exports = app;
