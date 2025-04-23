require("dotenv").config();
const express = require("express");
const cors = require("cors");
//const mongoose = require("mongoose");
const path = require("path");

// Uvoz route-ov
//const userRoutes = require('./routes/userRoutes');

// Inicializacija app-a
const app = express();

// Middleware
app.use(cors());
app.use(express.json()); // za JSON requeste

// MongoDB povezava
// mongoose
//   .connect(process.env.MONGO_URI, {
//     useNewUrlParser: true,
//     useUnifiedTopology: true,
//   })
//   .then(() => console.log("✅ MongoDB connected"))
//   .catch((err) => console.error("❌ MongoDB connection error:", err));
// Export app-a za `bin/www`
app.listen(3002, () => {
  console.log("Server is running on port 3002");
});
module.exports = app;
