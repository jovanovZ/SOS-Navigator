var mongoose = require("mongoose");
var Schema = mongoose.Schema;

var MessageSchema = new Schema({
  deviceId: { type: String, required: true },
  message: { type: String, required: true },
  timestamp: { type: Date, default: Date.now },
});

module.exports = mongoose.model("message", MessageSchema);