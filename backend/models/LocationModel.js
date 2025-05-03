var mongoose = require("mongoose");
var Schema = mongoose.Schema;

var locationSchema = new Schema({
  longitude: Number,
  latitude: Number,
});

module.exports = mongoose.model("location", locationSchema);
