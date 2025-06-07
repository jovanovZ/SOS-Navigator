require("dotenv").config();
const express = require("express");
const cors = require("cors");
const mongoose = require("mongoose");
const userRoutes = require("./routes/userRoutes");
const accidentRoutes = require("./routes/accidenceRoutes");
const pathRoutes = require("./routes/pathRoutes");
const locationRoutes = require("./routes/locationRoutes")
const stationRoutes = require("./routes/stationRoutes")
const simulationRoutes = require("./routes/simulationRoutes")
const {exec} = require("child_process");

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
app.use(express.urlencoded({ extended: true })); // ne vpliva na multer, a dobro imeti
app.use(express.json()); // za JSON requeste
app.use(cookieParser());
app.use("/api/user", userRoutes);
app.use("/api/accident", accidentRoutes);
app.use("/api/path", pathRoutes);
app.use("/api/location", locationRoutes);
app.use("/api/station",stationRoutes);
app.use("/api/simulation",simulationRoutes);
app.post("/webhook", (req, res) => {
  /*const receivedSecret = req.headers['x-workflow-webhook-secret'];

  if (receivedSecret !== process.env.WEBHOOK_SECRET) {
    console.warn("🔒 Webhook rejected: Invalid secret");
    return res.status(403).send('Forbidden');
  }*/
  
  exec("bash /deploy.sh", (err, stdout, stderr) => {
    if (err) {
      console.error("Deployment failed:", stderr);
    } else {
      console.log("Deployment log:\n", stdout);
    }
  });

  console.log("Webhook received, deployment initiated.");
  res.sendStatus(200);
});



mongoose
  .connect(process.env.MONGO_URI)

  .then(() => console.log("MongoDB connected"))
  .catch((err) => console.error("MongoDB connection error:", err));

app.listen(3002, () => {
  console.log("Server is running on port 3002");
});
module.exports = app;
