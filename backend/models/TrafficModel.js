var mongoose = require("mongoose");
var Schema = mongoose.Schema;

var trafficSchema = new Schema({
  timestamp: { type: Date, default: Date.now },
  status: { type: String, default: "normal" },
  vehicle_count: { type: Number, default: 0 },
  geometry: {
    type: { type: String, enum: ["Point"], required: true, default: "Point" },
    coordinates: { type: [Number], required: true }, // [longitude, latitude]
  },
  image_base64: { type: String },
});

trafficSchema.index({ geometry: "2dsphere" });

module.exports = mongoose.model("traffic", trafficSchema, "traffic_data");
