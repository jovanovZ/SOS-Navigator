var mongoose = require("mongoose");
var Schema = mongoose.Schema;

var SimulationSchema = new Schema({
  userId: { type: Schema.Types.ObjectId, ref: "user" },
  accidentName: { type: String, required: true },
  accidentId: { type: Schema.Types.ObjectId, ref: "accident" },
  typeOfServices: { type: String, default: 'typeOfServices'}, // če bomo naredili da lahko dadajaš poljubne postaje potem to spremeni in daj v String
  bestStationId: { type: Schema.Types.ObjectId, ref: "station" },
  bestPathId: { type: Schema.Types.ObjectId, ref: "path" },
  responseTime: Number, // v milisekundah
});

module.exports = mongoose.model("simulation", SimulationSchema);
