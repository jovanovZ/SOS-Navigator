var mongoose = require("mongoose");
var Schema = mongoose.Schema;

var SimulationSchema = new Schema({
  userId: { type: Schema.Types.ObjectId, ref: "user" },
  accidentId: { type: Schema.Types.ObjectId, ref: "accident" },
  typeOfServices: { type: String, enum: ["Policijska", "Bolnica", "Gasilci"] }, // če bomo naredili da lahko dadajaš poljubne postaje potem to spremeni in daj v String
  bestStationId: { type: Schema.Types.ObjectId, ref: "station" },
  bestPathId: { type: Schema.Types.ObjectId, ref: "path" },
  responseTime: Number, // v milisekundah
});

module.exports = mongoose.model("simulation", SimulationSchema);
