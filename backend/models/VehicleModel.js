const mongoose = require("mongoose");
const Schema = mongoose.Schema;

const vehicleSchema = new Schema({
    locationEndId: { type: Schema.Types.ObjectId, ref: "location" },
    locationStartId: { type: Schema.Types.ObjectId, ref: "location" },
    type: { type: String, default: "Police" },
    acceleration: { type: Number, default: 0.0 },
    locationFreq: { type: Number, default: 1 },
    accelerationFreq: { type: Number, default: 1 },
    timeStamp: { type: Date, default: Date.now },
});

module.exports = mongoose.model("vehicle", vehicleSchema);
