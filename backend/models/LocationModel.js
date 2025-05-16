var mongoose = require("mongoose");
var Schema = mongoose.Schema;

var locationSchema = new Schema({
  geometry: {
    type: { type: String, enum: ['Point'], required: true, default: 'Point' },
    coordinates: { type: [Number], required: true } // [longitude, latitude]
  }
});
/* TAK SE BO DOSTOPALO DO LONG LAT

  const longitude = geometry.coordinates[0];
  const latitude = geometry.coordinates[1];

*/ 
locationSchema.index({ geometry: "2dsphere" });

module.exports = mongoose.model("location", locationSchema);
