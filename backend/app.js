require("dotenv").config();
require("./mqtt/mqttClient");
const express = require("express");
const cors = require("cors");
const mongoose = require("mongoose");
const userRoutes = require("./routes/userRoutes");
const accidentRoutes = require("./routes/accidenceRoutes");
const pathRoutes = require("./routes/pathRoutes");
const locationRoutes = require("./routes/locationRoutes")
const stationRoutes = require("./routes/stationRoutes")
const simulationRoutes = require("./routes/simulationRoutes")
const vehicleRoutes = require("./routes/vehicleRoutes")
const trafficRoutes = require("./routes/trafficRoutes");




const cookieParser = require("cookie-parser");


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
app.use("/api/vehicle",vehicleRoutes)
app.use("/api/traffic", trafficRoutes);

const http = require("http");

app.post("/webhook", (req, res) => {
  const secret = req.headers["x-workflow-webhook-secret"];
  const req2 = http.request(
    {
      hostname: "host.docker.internal", 
      port: 4000,
      path: "/webhook",
      method: "POST",
      headers: {
        "x-workflow-webhook-secret" : secret,
      },
    },
    (res2) => {
      console.log(`Triggered host deploy, status: ${res2.statusCode}`);
    }
  );

  req2.on("error", (err) => {
    console.error("Error sending webhook to host:", err);
  });
  req2.end();

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
